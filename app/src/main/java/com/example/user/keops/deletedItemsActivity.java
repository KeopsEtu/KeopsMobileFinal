package com.example.user.keops;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class deletedItemsActivity extends AppCompatActivity {

    ListView deletedItemsListView;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<String> listItemFromFB;
    ArrayList<String> delete;
    ArrayList<String> temp;
    ArrayList<Integer> counts;
    private FirebaseAuth mAuth;
    ArrayList<HashMap<String, String>> hashMapsOfItems = new ArrayList<>();
    ArrayList<Date> dates;
    ArrayList<Integer> deletes;
    ArrayList<Date> dates2;
    ArrayList<Integer> added;

    private ArrayAdapter<String> existingListAdapter;
    private TextView totalClassmates;
    private SwipeLayout swipeLayout;

    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinflater = getMenuInflater();
        menuinflater.inflate(R.menu.listener, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.listener) {
            Intent intent = new Intent(getApplicationContext(), listenActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.list) {
            Intent intent = new Intent(getApplicationContext(), addItemViaKeyboardActivity.class);
            intent.putExtra("s", "activity");
            startActivity(intent);
        } else if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_items);
        handleIntent(getIntent());

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        deletedItemsListView = findViewById(R.id.deletedItemsListView);
        listItemFromFB = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        counts = new ArrayList<>();
        delete = new ArrayList<>();
        temp = new ArrayList<>();
        deletes = new ArrayList<>();
        added = new ArrayList<>();
        dates = new ArrayList<>();
        dates2 = new ArrayList<>();

        getDataFromFirebase();
        setListViewHeader();
        setListViewAdapter();

    }

    private void setListViewHeader() {
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.header_listview, deletedItemsListView, false);
        totalClassmates = (TextView) header.findViewById(R.id.total);
        swipeLayout = (SwipeLayout) header.findViewById(R.id.swipe_layout);
        setSwipeViewFeatures();
        deletedItemsListView.addHeaderView(header);
    }

    private void setSwipeViewFeatures() {
        //set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, findViewById(R.id.bottom_wrapper));

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                Log.i(TAG, "onClose");
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                Log.i(TAG, "on swiping");
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
                Log.i(TAG, "on start open");
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                Log.i(TAG, "the BottomView totally show");
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                Log.i(TAG, "the BottomView totally close");
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });
    }

    private void setListViewAdapter() {
        existingListAdapter = new DeletedListViewAdapter(this, R.layout.item_listview, listItemFromFB);
        deletedItemsListView.setAdapter(existingListAdapter);

        totalClassmates.setText("(" + listItemFromFB.size() + ")");
    }

    public void updateAdapter() {
        existingListAdapter.notifyDataSetChanged(); //update adapter
        totalClassmates.setText("(" + listItemFromFB.size() + ")"); //update total items in list
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (existingListAdapter != null)
                        existingListAdapter.getFilter().filter(query);
                }
            }, 10);

        }
    }

    public void getDataFromFirebase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) ds.getValue();

                    hashMapsOfItems.add(hashMap);

                    if (mAuth.getCurrentUser().getEmail().equals(hashMap.get("userEmail"))) {
                        if (hashMap.get("amountOfItem") != null) {
                            if (Integer.parseInt(hashMap.get("amountOfItem")) == 0) {
                                listItemFromFB.add(hashMap.get("item"));
                                counts.add(Integer.parseInt(String.valueOf(hashMap.get("amountOfItem"))));
                            } else {
                                for (String key : hashMap.keySet()) {
                                    if(key.startsWith("removed")) {
                                        String date = key.substring(key.indexOf(" ")+1,key.indexOf(" ")+11);
                                        deletes.add(Integer.parseInt(hashMap.get(key)));
                                        Date date1 = null;
                                        try {
                                            date1 = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                                        } catch (ParseException e) { }
                                        dates.add(date1);
                                    } else if(key.startsWith("added")) {
                                        String date = key.substring(key.indexOf(" ")+1,key.indexOf(" ")+11);
                                        added.add(Integer.parseInt(hashMap.get(key)));
                                        Date date1 = null;
                                        try {
                                            date1 = new SimpleDateFormat("dd-MM-yyyy").parse(date);
                                        } catch (ParseException e) { }
                                        dates2.add(date1);
                                    }
                                }
                                for(int i=0; i < dates.size(); i++){
                                    for(int j=1; j < (dates.size()-i); j++){
                                        if(dates.get(j-1).compareTo(dates.get(j))>= 0){
                                            Date temp = dates.get(j-1);
                                            dates.set(j-1, dates.get(j));
                                            dates.set(j,temp);
                                            int temp2 = deletes.get(j-1);
                                            deletes.set(j-1, deletes.get(j));
                                            deletes.set(j,temp2);
                                        }

                                    }
                                }
                                float temp3 = 0;
                                for(int i=1; i < dates.size(); i++) {
                                    temp3 = temp3 + (dates.get(i).getTime() - dates.get(i-1).getTime()) / 3600000 / (float)(deletes.get(i));
                                }
                                int temp6 = (int)(temp3/(dates.size()-1));
                                for(int i=0; i < dates2.size(); i++){
                                    for(int j=1; j < (dates2.size()-i); j++){
                                        if(dates2.get(j-1).compareTo(dates2.get(j))>= 0){
                                            Date temp = dates2.get(j-1);
                                            dates2.set(j-1, dates2.get(j));
                                            dates2.set(j,temp);
                                            int temp2 = added.get(j-1);
                                            added.set(j-1, added.get(j));
                                            added.set(j,temp2);
                                        }

                                    }
                                }
                                float temp4 = 0;
                                for(int i=1; i < dates2.size(); i++) {
                                    temp4 = temp4 + (dates2.get(i).getTime() - dates2.get(i-1).getTime()) / 3600000 / (float)(added.get(i));
                                }
                                int temp5 = (int)(temp4/(dates2.size()-1));
                                if (Integer.parseInt(hashMap.get("amountOfItem"))*temp6<temp5) {
                                    listItemFromFB.add(hashMap.get("item"));
                                    counts.add(Integer.parseInt(String.valueOf(hashMap.get("amountOfItem"))));
                                }
                            }
                        }
                    }
                }

                for (int i = 0; i < counts.size(); i++) {
                    for (int j = 1; j < (counts.size() - i); j++) {
                        if (counts.get(j - 1) < counts.get(j)) {
                            int temp = counts.get(j - 1);
                            counts.set(j - 1, counts.get(j));
                            counts.set(j, temp);
                            String temp2 = listItemFromFB.get(j - 1);
                            listItemFromFB.set(j - 1, listItemFromFB.get(j));
                            listItemFromFB.set(j, temp2);
                        }
                    }
                }

                existingListAdapter.notifyDataSetChanged();
                totalClassmates.setText("(" + listItemFromFB.size() + ")"); //update total items in list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    
}

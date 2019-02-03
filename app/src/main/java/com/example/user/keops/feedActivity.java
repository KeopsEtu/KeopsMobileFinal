package com.example.user.keops;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class feedActivity extends AppCompatActivity {

    ListView listView;
    postClass adapter;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<String> listItemFromFB;
    ArrayList<String> delete;
    ArrayList<String> temp;
    ArrayList<Integer> counts;
    private FirebaseAuth mAuth;
    ArrayList<HashMap<String, String>> hashMapsOfItems = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinflater = getMenuInflater();
        menuinflater.inflate(R.menu.listener, menu);
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
        setContentView(R.layout.activity_feed);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        listView = findViewById(R.id.listView);
        listItemFromFB = new ArrayList<>();
        adapter = new postClass(listItemFromFB, listItemFromFB, this);
        listView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();
        counts = new ArrayList<>();
        delete = new ArrayList<>();
        temp = new ArrayList<>();
        getDataFromFirebase();
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
                            if (Integer.parseInt(hashMap.get("amountOfItem")) > 0) {
                                listItemFromFB.add(hashMap.get("item"));
                                counts.add(Integer.parseInt(String.valueOf(hashMap.get("amountOfItem"))));
                            } else {
                                if (!(hashMap.get("amountOfItem").equals(hashMap.get("removedCount")))) {
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeButton(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        String userEmail = user.getEmail();
        ListView list = findViewById(R.id.listView);

        for (int i = 0; i < list.getChildCount(); i++) {
            View view2 = list.getChildAt(i);
            CheckBox cv = view2.findViewById(R.id.list_item);
            if (cv.isChecked())
                delete.add(cv.getText().toString());
        }

        for (String s : delete)
            for (int i = 0; i < counts.size(); i++)
                if (listItemFromFB.get(i) != null && listItemFromFB.get(i).equals(s))
                    temp.add(counts.get(i).toString());

        for (int s = 0; s < delete.size(); s++)
            for (int i = 0; i < hashMapsOfItems.size(); i++)
                if (hashMapsOfItems.get(i).get("item") != null && hashMapsOfItems.get(i).get("item").equals(delete.get(s)) &&
                        hashMapsOfItems.get(i).get("userEmail") != null && hashMapsOfItems.get(i).get("userEmail").equals(userEmail)) {
                    int newValue = Integer.parseInt(hashMapsOfItems.get(i).get("amountOfItem")) - 1;

                    myRef.child(delete.get(s) + userID).child("removed " + getCurrentDate()).setValue("" + 1);
                    myRef.child(delete.get(s) + userID).child("amountOfItem").setValue("" + newValue);
                }

        Intent intent = new Intent(getApplicationContext(), feedActivity.class);
        startActivity(intent);
    }

    public void addButton(View view) {
        Intent intent = new Intent(getApplicationContext(), addItemViaKeyboardActivity.class);
        intent.putExtra("s", "activity");
        startActivity(intent);
    }


    public String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-mm-yyyy-HH:mm");
        String dateTime = df.format(c);

        return dateTime;
    }

}

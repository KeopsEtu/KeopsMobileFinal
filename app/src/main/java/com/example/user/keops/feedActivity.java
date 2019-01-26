package com.example.user.keops;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class feedActivity extends AppCompatActivity {

    ListView listView;
    postClass adapter;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<String> listItemFromFB;
    ArrayList<Integer> counts;
    private FirebaseAuth mAuth;
    Button button;

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
        getDataFromFirebase();
    }

    public void getDataFromFirebase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) ds.getValue();
                    if (mAuth.getCurrentUser().getEmail().equals(hashMap.get("userEmail"))) {
                        listItemFromFB.add(hashMap.get("item"));
                        if (hashMap.get("amountOfItem") != null)
                            counts.add(Integer.parseInt(String.valueOf(hashMap.get("amountOfItem"))));
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
               /* for (int position=0;position<counts.size();position++) {
                button = findViewById(R.id.analysis);
                final String temp = listItemFromFB.get(position);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), analysisActivity.class);
                        i.putExtra("send_string",temp);
                        startActivity(i);
                    }
                });}*/
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeButton(View view) {

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) ds.getValue();
                    if (mAuth.getCurrentUser().getEmail().equals(hashMap.get("userEmail"))) {
                        if (hashMap.get("item") != null && hashMap.get("item").equals("erik")) {
                            myRef.child("erik" + userID).child("item").removeValue();
                            myRef.child("erik" + userID).child("amountOfItem").removeValue();
                            myRef.child("erik" + userID).child("userEmail").removeValue();
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

    public void addButton(View view) {
        Intent intent = new Intent(getApplicationContext(), addItemViaKeyboardActivity.class);
        intent.putExtra("s", "activity");
        startActivity(intent);
    }
}

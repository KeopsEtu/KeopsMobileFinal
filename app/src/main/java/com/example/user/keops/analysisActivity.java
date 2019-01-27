package com.example.user.keops;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class analysisActivity extends AppCompatActivity {
    ListView listView;
    postClass adapter;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinflater = getMenuInflater();
        menuinflater.inflate(R.menu.listener,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.listener) {
            Intent intent =  new Intent(getApplicationContext(), listenActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.list) {
            Intent intent =  new Intent(getApplicationContext(), addItemViaKeyboardActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.main) {
            Intent intent =  new Intent(getApplicationContext(), feedActivity.class);
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

        listView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();

        Bundle extras = getIntent().getExtras();
        ArrayList<String> value = extras.getStringArrayList("send_string");
    }
}

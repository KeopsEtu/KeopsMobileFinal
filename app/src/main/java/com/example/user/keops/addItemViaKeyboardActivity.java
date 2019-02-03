package com.example.user.keops;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

public class addItemViaKeyboardActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    EditText item, amountOfItem;
    Button addItemButton;

    ArrayList<String> listItemFromFB;
    ArrayList<Integer> counts;
    postClass adapter;

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
        } else if (item.getItemId() == R.id.main) {
            Intent intent = new Intent(getApplicationContext(), feedActivity.class);
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
        setContentView(R.layout.activity_add_item_via_keyboard);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        item = findViewById(R.id.add_item_edit_text);
        amountOfItem = findViewById(R.id.add_amount_of_item);

        addItemButton = findViewById(R.id.add_item_button);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDatabase(view);
            }
        });

        counts = new ArrayList<>();
        listItemFromFB = new ArrayList<>();
        adapter = new postClass(listItemFromFB, listItemFromFB, this);

    }

    protected void addToDatabase(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        String mail = user.getEmail();
        String userID = user.getUid();
        String itemName = item.getText().toString().toLowerCase();

        String databaseListName = itemName + userID;
        if (amountOfItem.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Lütfen ürün miktarını giriniz ...", Toast.LENGTH_LONG).show();
        } else if (item.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Lütfen ürün ismini giriniz ...", Toast.LENGTH_LONG).show();
        } else {
            myRef.child(databaseListName).child("userEmail").setValue(mail);
            myRef.child(databaseListName).child("item").setValue(itemName);
            myRef.child(databaseListName).child("amountOfItem").setValue(amountOfItem.getText().toString());
            myRef.child(databaseListName).child("add " + getCurrentDate()).setValue( amountOfItem.getText().toString());

            Toast.makeText(getApplicationContext(), amountOfItem.getText().toString() + " " +
                    itemName + "  Başarıyla eklendi ...", Toast.LENGTH_LONG).show();

            item.setText("");
            amountOfItem.setText("");

            View view5 = this.getCurrentFocus();
            if (view5 != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public String getCurrentDate() {
        Date date = new Date();
        String dateTime = date.toString().substring(0, date.toString().indexOf("GMT")) +
                date.toString().substring(date.toString().indexOf("GMT"));
        dateTime = dateTime.replace(" ", "_");

        return dateTime;
    }

    public void onBackPressed() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getString("s").equals("activity")) {
            Intent intent = new Intent(this, feedActivity.class);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }
}

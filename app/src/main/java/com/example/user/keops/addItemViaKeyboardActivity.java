package com.example.user.keops;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class addItemViaKeyboardActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    EditText sentence;
    Button addItemButton;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinflater = getMenuInflater();
        menuinflater.inflate(R.menu.listener, menu);

        addItemButton = findViewById(R.id.add_item_button);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDatabase(view);
                Toast.makeText(getApplicationContext(), sentence.getText().toString() + "  Başarıyla eklendi ...", Toast.LENGTH_LONG).show();
            }
        });
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

        sentence = findViewById(R.id.add_item_edit_text);
    }

    protected void addToDatabase(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        String mail = user.getEmail();
        UUID uuid = UUID.randomUUID();
        myRef.child("list" + uuid).child("userEmail").setValue(mail);
        myRef.child("list" + uuid).child("item").setValue(sentence.getText().toString());
    }
}

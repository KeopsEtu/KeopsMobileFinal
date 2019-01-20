package com.example.user.keops;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText email;
    EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Intent intent = new Intent(getApplicationContext(),feedActivity.class);
            startActivity(intent);
        }
    }

    public void signIn(View view) {
        mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(),feedActivity.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void signUp(View view) {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "user created!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),feedActivity.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // DATABASE
        /*
        ParseObject object = new ParseObject("Fruits");

        object.put("name", "banana");
        object.put("calories", 150);
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Object Saved", Toast.LENGTH_LONG).show();
                }
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Fruits");
        query.getInBackground("upNe5kraHL", new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e != null){
                    e.printStackTrace();
                }
                else{
                    String objectName = object.getString("name");
                    int objectCalories = object.getInt("calories");

                    System.out.println("object name: " + objectName);
                    System.out.println("object calories: " + objectCalories);
                }
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Fruits");
        //query.whereEqualTo("name", "banana");
        //query.whereLessThan("calories", 130);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    if (objects.size() > 0) {
                        for (ParseObject object : objects
                        ) {
                            String objectName = object.getString("name");
                            int objectCalorie = object.getInt("calories");

                            System.out.println("object name: " + objectName);
                            System.out.println("object calorie: " + objectCalorie);
                        }
                    }
                }
            }
        });
    */

    // USER

        /*
        // Kullanici olusturma
        ParseUser user = new ParseUser();
        user.setUsername("James");
        user.setPassword("123456");

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    Toast.makeText(MainActivity.this, "User Signed Up!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */

}

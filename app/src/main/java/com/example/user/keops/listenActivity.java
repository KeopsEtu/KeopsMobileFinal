package com.example.user.keops;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class listenActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    String text="";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinflater = getMenuInflater();
        menuinflater.inflate(R.menu.listener,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.main) {
            Intent intent =  new Intent(getApplicationContext(), feedActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.list) {
            Intent intent =  new Intent(getApplicationContext(), addItemViaKeyboardActivity.class);
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
        setContentView(R.layout.activity_listen);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        checkPermission();

        final EditText editText = findViewById(R.id.editText);

        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Button addItem=(Button) findViewById(R.id.buttonAdder);

        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());


        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);


                if (matches != null) {
                    text=matches.get(0);
                    editText.setText(matches.get(0));
                    addItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String mail = user.getEmail();
                            if(text.matches("[a-zA-Z]+")){
                                String databaseListName = text.toLowerCase() + user.getUid();
                                myRef.child(databaseListName).child("userEmail").setValue(mail);
                                myRef.child(databaseListName).child("item").setValue(text);
                                myRef.child(databaseListName).child("amountOfItem").setValue("1");
                                AlertDialog.Builder theBuild = new AlertDialog.Builder(listenActivity.this);
                                theBuild.setMessage(text+" eklendi");
                                theBuild.show();

                                int timeout = 3000;

                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {

                                    @Override
                                    public void run() {
                                        finish();
                                        Intent feedPage = new Intent(listenActivity.this, feedActivity.class);
                                        startActivity(feedPage);
                                    }
                                }, timeout);
                            }
                            else{
                                String databaseListName = text.substring(text.indexOf(" ")+1).toLowerCase() + user.getUid();
                                myRef.child(databaseListName).child("userEmail").setValue(mail);
                                myRef.child(databaseListName).child("item").setValue(text.substring(text.indexOf(" ")+1));
                                myRef.child(databaseListName).child("amountOfItem").setValue(text.substring(0,text.indexOf(" ")));
                                AlertDialog.Builder theBuild = new AlertDialog.Builder(listenActivity.this);
                                theBuild.setMessage(text+" eklendi");
                                theBuild.show();

                                int timeout = 3000;

                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {

                                    @Override
                                    public void run() {
                                        finish();
                                        Intent feedPage = new Intent(listenActivity.this, feedActivity.class);
                                        startActivity(feedPage);
                                    }
                                }, timeout);
                            }


                        }
                    });

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        findViewById(R.id.button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        editText.setHint("Girdiniz..");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        editText.setText("");
                        editText.setHint("Dinliyor...");
                        break;
                }
                return false;
            }
        });
    }



    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1);
            }
        }
    }
}

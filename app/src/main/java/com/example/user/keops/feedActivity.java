package com.example.user.keops;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class feedActivity extends AppCompatActivity {

    Button deletedItemsButton;
    ListView listView;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<String> listItemFromFB;
    ArrayList<String> delete;
    HashMap<String, String> hashMap;
    ArrayList<String> temp;
    ArrayList<Integer> counts;
    private FirebaseAuth mAuth;
    private TextToSpeech mTTS;
    ArrayList<HashMap<String, String>> hashMapsOfItems = new ArrayList<>();

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
        setContentView(R.layout.activity_feed);
        handleIntent(getIntent());

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        listView = findViewById(R.id.listView);
        listItemFromFB = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        counts = new ArrayList<>();
        delete = new ArrayList<>();
        temp = new ArrayList<>();
        deletedItemsButton = findViewById(R.id.deletedItemsButton);

        getDataFromFirebase();
        setListViewHeader();
        setListViewAdapter();

    }

    public void deletedItems(View view) {
        Intent intent = new Intent(getApplicationContext(), deletedItemsActivity.class);
        startActivity(intent);
    }

    private void setListViewHeader() {
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.header_listview, listView, false);
        totalClassmates = (TextView) header.findViewById(R.id.total);
        swipeLayout = (SwipeLayout) header.findViewById(R.id.swipe_layout);
        setSwipeViewFeatures();
        listView.addHeaderView(header);
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
        existingListAdapter = new ListViewAdapter(this, R.layout.item_listview, listItemFromFB);
        listView.setAdapter(existingListAdapter);

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
                    hashMap = (HashMap<String, String>) ds.getValue();

                    hashMapsOfItems.add(hashMap);

                    if (mAuth.getCurrentUser().getEmail().equals(hashMap.get("userEmail"))) {
                        if (hashMap.get("amountOfItem") != null) {
                            if (Integer.parseInt(hashMap.get("amountOfItem")) > 0) {
                                listItemFromFB.add(hashMap.get("amountOfItem") + " " + hashMap.get("item"));
                                counts.add(Integer.parseInt(String.valueOf(hashMap.get("amountOfItem"))));
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

    protected void addToDatabase(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        String mail = user.getEmail();
        String userID = user.getUid();
        EditText item = findViewById(R.id.editText2);
        String itemName = "";
        String amountOfItem = "";

        while (item.getText().toString().indexOf(" ") == 0 && item.getText().toString().length() >= 1)
            item.setText(item.getText().toString().substring(1));

        if (item.getText().toString().equals("") || !item.getText().toString().contains(" ")) {
            Toast.makeText(getApplicationContext(), "Lütfen ürün ismini, ardından miktarını aralarında boşluk bırakarak giriniz ...", Toast.LENGTH_LONG).show();
        } else {
            itemName = item.getText().toString().toLowerCase().substring(0, item.getText().toString().toLowerCase().indexOf(" "));
            item.setText(item.getText().toString().substring(item.getText().toString().indexOf(" ")));

            while (item.getText().toString().indexOf(" ") == 0 && item.getText().toString().length() >= 1)
                item.setText(item.getText().toString().substring(1));

            if (item.getText().length() == 0) {
                Toast.makeText(getApplicationContext(), "Lütfen ürün ismini ve miktarını da aralarında boşluk bırakarak giriniz ...", Toast.LENGTH_LONG).show();
            } else {
                amountOfItem = item.getText().toString().toLowerCase();
                String databaseListName = itemName + userID;

                if (amountOfItem.equals("")) {
                    Toast.makeText(getApplicationContext(), "Lütfen ürün miktarını giriniz ...", Toast.LENGTH_LONG).show();
                } else if (item.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Lütfen ürün ismini giriniz ...", Toast.LENGTH_LONG).show();
                } else {
                    String temp = "";
                    for (int i =0; i<hashMapsOfItems.size();i++) {
                        if (hashMapsOfItems.get(i).get("item").equals(itemName) && mAuth.getCurrentUser().getEmail().equals(hashMapsOfItems.get(i).get("userEmail"))) {
                            temp = hashMapsOfItems.get(i).get("amountOfItem");
                        }
                    }
                    myRef.child(databaseListName).child("userEmail").setValue(mail);
                    myRef.child(databaseListName).child("item").setValue(itemName);
                    int temp2 = Integer.parseInt(amountOfItem) + Integer.parseInt(temp);
                    myRef.child(databaseListName).child("amountOfItem").setValue(temp2+"");
                    myRef.child(databaseListName).child("added " + getCurrentDate()).setValue(amountOfItem);

                    Toast.makeText(getApplicationContext(), amountOfItem + " " +
                            itemName + "  Başarıyla eklendi ...", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(this, feedActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    protected void addToDatabaseListen(View view) {

        checkPermission();
        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

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
                    String text = matches.get(0);
                    System.out.println(text);
                    if(text.contains("ekle")) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        String mail = user.getEmail();
                        if (text.matches("[a-zA-Z]+")) {
                            String added=text.substring(text.lastIndexOf(" ")+1);
                            String addedItem=text.substring(0,text.indexOf(" "));
                            String addedAmount="1";
                            String databaseListName = addedItem.toLowerCase() + user.getUid();
                            myRef.child(databaseListName).child("userEmail").setValue(mail);
                            myRef.child(databaseListName).child("item").setValue(addedItem);
                            myRef.child(databaseListName).child("amountOfItem").setValue(1);
                            myRef.child(databaseListName).child("added " + getCurrentDate()).setValue(1);
                            AlertDialog.Builder theBuild = new AlertDialog.Builder(feedActivity.this);
                            theBuild.setMessage(addedItem + " eklendi");
                            theBuild.show();
                            Intent intent = new Intent(getApplicationContext(), feedActivity.class);
                            startActivity(intent);
                        } else {
                            String added=text.substring(text.lastIndexOf(" ")+1);
                            String addedItem=text.substring(text.indexOf(" ")+1,text.lastIndexOf(" "));
                            String addedAmount=text.substring(0,text.indexOf(" "));

                            String databaseListName = addedItem.toLowerCase() + user.getUid();
                            myRef.child(databaseListName).child("userEmail").setValue(mail);
                            myRef.child(databaseListName).child("item").setValue(addedItem);
                            myRef.child(databaseListName).child("amountOfItem").setValue(addedAmount);
                            myRef.child(databaseListName).child("added " + getCurrentDate()).setValue(addedAmount);
                            AlertDialog.Builder theBuild = new AlertDialog.Builder(feedActivity.this);
                            theBuild.setMessage(addedAmount+" "+ addedItem+" "+ "eklendi");
                            theBuild.show();

                            Intent intent = new Intent(getApplicationContext(), feedActivity.class);
                            startActivity(intent);
                        }

                    }
                    else if(text.contains("sil")){


                        FirebaseUser user = mAuth.getCurrentUser();
                        String mail = user.getEmail();
                        String userEmail = user.getEmail();
                        String userID = user.getUid();
                        ListView list = findViewById(R.id.listView);

                        if(text.matches("[a-zA-Z]+")){
                            String databaseListName = text.substring(0,text.indexOf(" ")) + userID;
                            String deleted=text.substring(text.lastIndexOf(" ")+1);
                            String deletedItem=text.substring(0,text.indexOf(" "));
                            String deletedAmount="1";

                            for (int i = 0; i < hashMapsOfItems.size(); i++) {
                                if (hashMapsOfItems.get(i).get("item") != null && hashMapsOfItems.get(i).get("item").equals(deletedItem) &&
                                        hashMapsOfItems.get(i).get("userEmail") != null && hashMapsOfItems.get(i).get("userEmail").equals(userEmail)) {
                                    int newValue = Integer.parseInt(hashMapsOfItems.get(i).get("amountOfItem")) - Integer.parseInt(deletedAmount);
                                    if(newValue<0){
                                        newValue=0;
                                        deletedAmount=hashMapsOfItems.get(i).get("amountOfItem");
                                    }
                                    myRef.child(databaseListName).child("removed " + getCurrentDate()).setValue("" + deletedAmount);
                                    myRef.child(databaseListName).child("amountOfItem").setValue("" + newValue);
                                }
                            }


                            AlertDialog.Builder theBuild = new AlertDialog.Builder(feedActivity.this);
                            theBuild.setMessage(deletedAmount+" "+deletedItem+" "+"silindi");
                            theBuild.show();

                            Intent intent = new Intent(getApplicationContext(), feedActivity.class);
                            startActivity(intent);
                        }
                        else{
                            String deleted=text.substring(text.lastIndexOf(" ")+1);
                            String deletedItem=text.substring(text.indexOf(" ")+1,text.lastIndexOf(" "));
                            String deletedAmount=text.substring(0,text.indexOf(" "));
                            String databaseListName = deletedItem + userID;

                            for (int i = 0; i < hashMapsOfItems.size(); i++)
                                if (hashMapsOfItems.get(i).get("item") != null && hashMapsOfItems.get(i).get("item").equals(deletedItem) &&
                                        hashMapsOfItems.get(i).get("userEmail") != null && hashMapsOfItems.get(i).get("userEmail").equals(userEmail)) {
                                    int newValue = Integer.parseInt(hashMapsOfItems.get(i).get("amountOfItem")) - Integer.parseInt(deletedAmount);
                                    if(newValue<0){
                                        newValue=0;
                                        deletedAmount=hashMapsOfItems.get(i).get("amountOfItem");
                                    }

                                    myRef.child(databaseListName).child("removed " + getCurrentDate()).setValue("" + deletedAmount);
                                    myRef.child(databaseListName).child("amountOfItem").setValue("" + newValue);
                                }


                            AlertDialog.Builder theBuild = new AlertDialog.Builder(feedActivity.this);
                            theBuild.setMessage(deletedAmount+" "+deletedItem+" "+"silindi");
                            theBuild.show();

                            Intent intent = new Intent(getApplicationContext(), feedActivity.class);
                            startActivity(intent);

                        }

                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        findViewById(R.id.view_a).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        break;
                }
                return false;
            }
        });


    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }

    public void read(View view) {
        final Button reader = findViewById(R.id.button2);
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Locale locale = new Locale("tr", "TR");

                    int result = mTTS.setLanguage(locale);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        reader.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        reader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });



    }

    private void speak() {
        String str = "";
        for (int i =0;i<hashMapsOfItems.size();i++) {
            if (hashMapsOfItems.get(i).get("userEmail").equals(mAuth.getCurrentUser().getEmail())) {
                if(Integer.parseInt(hashMapsOfItems.get(i).get("amountOfItem"))==0){
                    str += hashMapsOfItems.get(i).get("item")+" yok\n" ;
                }
                else{
                    str += hashMapsOfItems.get(i).get("amountOfItem")+" "+hashMapsOfItems.get(i).get("item")+"\n" ;
                }
                
            }
         }
        float speed = 0.7f;
        mTTS.setSpeechRate(speed);
        mTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }

    public String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH:mm");
        String dateTime = df.format(c);

        return dateTime;
    }

}

package com.example.user.keops;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;


public class analysisActivity extends AppCompatActivity {
    ArrayList<Date> dates;
    ArrayList<Integer> deletes;
    ArrayList<Date> dates2;
    ArrayList<Integer> added;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series2;
    GraphView graph;

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
        setContentView(R.layout.activity_analysis);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        dates = new ArrayList<>();
        dates2 = new ArrayList<>();
        series = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        deletes = new ArrayList<>();
        added = new ArrayList<>();
        graph = findViewById(R.id.graph);

        Bundle extras = getIntent().getExtras();
        final String value = extras.getString("send_string");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) ds.getValue();
                    if (mAuth.getCurrentUser().getEmail().equals(hashMap.get("userEmail"))) {
                        if (hashMap.get("item").equals(value))
                            for (String key : hashMap.keySet()) {
                                if(key.startsWith("removed")) {
                                    String date = key.substring(key.indexOf(" ")+1,key.indexOf(" ")+11);
                                    deletes.add(Integer.parseInt(hashMap.get(key)));
                                    Date date1 = null;
                                    try {
                                        date1 = new SimpleDateFormat("MM-dd-yyyy").parse(date);
                                    } catch (ParseException e) { }
                                    dates.add(date1);
                                } else if(key.startsWith("added")) {
                                    String date = key.substring(key.indexOf(" ")+1,key.indexOf(" ")+11);
                                    added.add(Integer.parseInt(hashMap.get(key)));
                                    Date date1 = null;
                                    try {
                                        date1 = new SimpleDateFormat("MM-dd-yyyy").parse(date);
                                    } catch (ParseException e) { }
                                    dates2.add(date1);
                                }
                        }else continue;
                    }
                }
                for(int i=0; i < dates.size(); i++){
                    for(int j=1; j < (dates.size()-i); j++){
                        if(dates.get(j-1).compareTo(dates.get(j))>= 0){
                            Date temp = dates.get(j-1);
                            dates.set(j-1, dates.get(j));
                            dates.set(j,temp);
                        }

                    }
                }
                for(int i=0; i < dates2.size(); i++){
                    for(int j=1; j < (dates2.size()-i); j++){
                        if(dates2.get(j-1).compareTo(dates2.get(j))>= 0){
                            Date temp = dates2.get(j-1);
                            dates2.set(j-1, dates2.get(j));
                            dates2.set(j,temp);
                        }

                    }
                }
                dates.get(0).compareTo(dates.get(1));
                while (dates.size()!=0) {
                    series.appendData(new DataPoint(dates.get(0), deletes.get(0)), true, 5);
                    dates.remove(0);
                    deletes.remove(0);
                }
                series.setColor(Color.RED);
                graph.addSeries(series);
                while (dates2.size()!=0) {
                    series2.appendData(new DataPoint(dates2.get(0), added.get(0)), true, 5);
                    dates2.remove(0);
                    added.remove(0);
                }
                graph.addSeries(series2);
                graph.getGridLabelRenderer().setNumVerticalLabels(5);
                graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(analysisActivity.this));
            }
            @Override public void onCancelled(DatabaseError error) { }
        });
    }
}

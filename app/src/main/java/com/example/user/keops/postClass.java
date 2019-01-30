package com.example.user.keops;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class postClass extends ArrayAdapter<String> {
    private final ArrayList<String> useremail;
    private final ArrayList<String> listitem;
    private final Activity context;

    public postClass(ArrayList<String> useremail, ArrayList<String> listitem, Activity context) {
        super(context,R.layout.custom_view,useremail);
        this.useremail = useremail;
        this.listitem = listitem;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View customView = layoutInflater.inflate(R.layout.custom_view,null,true);
        TextView list = customView.findViewById(R.id.list_item);
        Button button = customView.findViewById(R.id.view_a);
        list.setText(listitem.get(position));
        final String temp = listitem.get(position);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context.getApplicationContext(), analysisActivity.class);
                i.putExtra("send_string",temp);
                context.startActivity(i);
            }
        });
        return customView;
    }


}

package com.example.user.keops;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<String> {

    private feedActivity activity;
    private List<String> items;

    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;


    public ListViewAdapter(feedActivity context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.item_listview, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(getItem(position));

        //handling buttons event
        holder.btnEdit.setOnClickListener(onEditListener(position, holder));
        holder.btnDelete.setOnClickListener(onDeleteListener(position, holder));
        final String temp = getItem(position);
        holder.btnAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity.getApplicationContext(), analysisActivity.class);
                i.putExtra("send_string", temp);
                activity.startActivity(i);
            }
        });

        return convertView;
    }

    private View.OnClickListener onEditListener(final int position, final ViewHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(position, holder);
            }
        };
    }

    /**
     * Editting confirm dialog
     *
     * @param position
     * @param holder
     */
    private void showEditDialog(final int position, final ViewHolder holder) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        alertDialogBuilder.setTitle("EDIT ELEMENT");
        final EditText input = new EditText(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setText(items.get(position));
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result edit text

                                FirebaseUser user = mAuth.getCurrentUser();
                                String userID = user.getUid();

                                String itemName = items.get(position);
                                //System.out.println("item before edit: " + itemName);

                                String databaseListName = itemName + userID;

                                items.set(position, input.getText().toString().trim());
                                itemName = items.get(position);

                                //System.out.println("item after edit: " + itemName);
                                myRef.child(databaseListName).child("item").setValue(itemName);

                                //notify data set changed
                                activity.updateAdapter();
                                holder.swipeLayout.close();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog and show it
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Deleting confirm dialog
     *
     * @param position
     * @param holder
     */
    private void showDeleteDialog(final int position, final ViewHolder holder) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        alertDialogBuilder.setTitle("DELETE ELEMENT");
        final EditText input = new EditText(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setText("1");
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                FirebaseUser user = mAuth.getCurrentUser();
                                String userID = user.getUid();

                                String itemName = items.get(position);
                                String databaseListName = itemName.substring(itemName.indexOf(" ") + 1) + userID;

                                //System.out.println("aaaa databaseListName: " + databaseListName);

                                String newAmaountOfItem = itemName.substring(0, itemName.indexOf(" "));
                                Integer newAmount = Integer.parseInt(newAmaountOfItem);

                                //System.out.println("aaaa newAmaountOfItem: " + newAmaountOfItem);

                                String removedAmaountOfItem = input.getText().toString();
                                Integer removedAmount = Integer.parseInt(removedAmaountOfItem);

                                //System.out.println("aaaa removedAmaountOfItem: " + removedAmaountOfItem);

                                String updatedAmount = "0";
                                if ((newAmount - removedAmount) > 0)
                                    updatedAmount = "" + (newAmount - removedAmount);

                                //System.out.println("aaaa updatedAmount: " + updatedAmount);

                                myRef.child(databaseListName).child("removed " + getCurrentDate()).setValue("" + input.getText().toString());
                                myRef.child(databaseListName).child("amountOfItem").setValue(updatedAmount);

                                if ((newAmount - removedAmount) == 0){
                                    items.remove(position);

                                Log.d("itemcount" , ""+(newAmount - removedAmount));}

                                //notify data set changed
                                holder.swipeLayout.close();
                                activity.updateAdapter();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog and show it
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private View.OnClickListener onDeleteListener(final int position, final ViewHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(position, holder);
            }
        };
    }

    private class ViewHolder {
        private TextView name;
        private View btnDelete;
        private View btnAnalyse;
        private View btnEdit;
        private SwipeLayout swipeLayout;

        public ViewHolder(View v) {
            swipeLayout = (SwipeLayout) v.findViewById(R.id.swipe_layout);
            btnDelete = v.findViewById(R.id.delete);
            btnEdit = v.findViewById(R.id.edit_query);
            btnAnalyse = v.findViewById(R.id.analyse);
            name = (TextView) v.findViewById(R.id.name);

            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        }
    }

    public String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH:mm");
        String dateTime = df.format(c);

        return dateTime;
    }
}

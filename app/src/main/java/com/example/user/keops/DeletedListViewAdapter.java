package com.example.user.keops;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.util.List;

public class DeletedListViewAdapter extends ArrayAdapter<String> {

    private deletedItemsActivity activity;
    private List<String> items;


    public DeletedListViewAdapter(deletedItemsActivity context, int resource, List<String> objects) {
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
                i.putExtra("send_string",temp);
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
                                items.set(position, input.getText().toString().trim());

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

    private View.OnClickListener onDeleteListener(final int position, final ViewHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.remove(position);
                holder.swipeLayout.close();
                activity.updateAdapter();
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
}

package com.example.dailyshoppinglistapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dailyshoppinglistapp.Data.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Type;
import java.sql.DatabaseMetaData;
import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    //View instance varriables
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private TextView totalAmountText;
    
    //Firebase instance varriables
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    //update item varriables
    String type;
    int amount;
    String note;
    String postKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        totalAmountText = (TextView) findViewById(R.id.total_amountText);

        //implementing the top toolbar
        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Shopping App");

        //implementing firebase database access and user tools
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userID = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(userID);
        databaseReference.keepSynced(true);

        //implementing recycler view data loader
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalAmount = 0;
                for (DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    Data data = snapShot.getValue(Data.class);
                    totalAmount += data.getAmount();
                    totalAmountText.setText(String.valueOf(totalAmount).concat(".00"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //implementing the floating action button
        floatingActionButton = findViewById(R.id.favButon);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customInputDialog();
            }
        });
    }

    //implementing the input popup dialog
    private void customInputDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);

        View myView = inflater.inflate(R.layout.input_data, null);
        final AlertDialog dialog = alertDialog.create();
        dialog.setView(myView);

        //instantiating inputform layout components
        final EditText typeText = myView.findViewById(R.id.typeField);
        final EditText amountText = myView.findViewById(R.id.amountField);
        final EditText noteText = myView.findViewById((R.id.noteField));
        Button saveButton = myView.findViewById(R.id.saveButton);

        //save button event h andler
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = typeText.getText().toString().trim();
                String amountString = amountText.getText().toString().trim();
                String note = noteText.getText().toString().trim();
                int amount = 0;

                if(TextUtils.isEmpty(type)){
                    typeText.setError("Required field");
                    return;
                }

                if(TextUtils.isEmpty(amountString)){
                    typeText.setError("Required field");
                    return;
                }else{
                    amount = Integer.parseInt(amountString);
                }

                if(TextUtils.isEmpty(note)){
                    typeText.setError("Required field");
                    return;
                }

                String id = databaseReference.push().getKey();
                String date = DateFormat.getInstance().format(new Date());

                Data data = new Data(type, amount, note, date, id);

                Log.i("Input Data", "type: "+data.getType()+" amount: "+data.getAmount()+" note: "+data.getNote()+" Date: "+data.getDate());
                databaseReference.child(id).setValue(data);

                Toast.makeText(HomeActivity.this, "Data successfully added", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //implementing the update popup dialog
    public void customUpdateDialog(){

        //creating the popup dialog containing the update form
        AlertDialog.Builder alertDBuilder = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View updateView = inflater.inflate(R.layout.update_input_data, null);

        final AlertDialog dialog = alertDBuilder.create();
        dialog.setView(updateView);
        dialog.show();

        //calling the update form view elements
        final EditText typeText = updateView.findViewById(R.id.edit_typeField);
        final EditText amountText = updateView.findViewById(R.id.edit_amountField);
        final EditText noteText = updateView.findViewById((R.id.edit_noteField));

        //setting the values of the selected item into the update form
        typeText.setText(type);
        typeText.setSelection(type.length());

        amountText.setText(String.valueOf(amount));
        amountText.setSelection(String.valueOf(amount).length());

        noteText.setText(note);
        noteText.setSelection(note.length());

        //calling the update form buttons
        Button updateButton = updateView.findViewById(R.id.updateButton);
        Button deleteButton = updateView.findViewById(R.id.deleteButton);

        //creating an update onclick listener for the item update button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = typeText.getText().toString().trim();
                int amount = Integer.parseInt(amountText.getText().toString().trim());
                String note = noteText.getText().toString().trim();
                String date = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(type, amount, note, date, postKey);

                databaseReference.child(postKey).setValue(data);

                Toast.makeText(HomeActivity.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        //creating a delete onclick listener for the item delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(postKey).removeValue();

                Toast.makeText(HomeActivity.this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data, MyViewHolder> recyclerAdapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (Data.class, R.layout.item_data, MyViewHolder.class, databaseReference){
            @Override
            protected void populateViewHolder(MyViewHolder myViewHolder, final Data data, final int position) {
                myViewHolder.setDate(data.getDate());
                myViewHolder.setType(data.getType());
                myViewHolder.setAmount(data.getAmount());
                myViewHolder.setNote(data.getNote());

                //setting an onclick listener on each item
                myViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        postKey = getRef(position).getKey();
                        type = data.getType();
                        amount = data.getAmount();
                        note = data.getNote();

                        customUpdateDialog();
                    }
                });
            }
        };
        recyclerView.setAdapter(recyclerAdapter);
    }

    //overriding the onCreateOptionsMenu method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //overrdiding the onOptionsItemSelected method on menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout:{
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //creating custom recycler class
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View view;
        public MyViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        private void setType(String type){
            TextView typeView = view.findViewById(R.id.type_text);
            typeView.setText(type);
        }

        private void setDate(String date){
            TextView dateView = view.findViewById(R.id.date_text);
            dateView.setText(date);
        }

        private void setAmount(int value){
            TextView amountText = view.findViewById(R.id.amount_text);
            String amount = String.valueOf(value);
            amountText.setText(amount);
        }
        private void setNote(String note){
            TextView noteText = view.findViewById(R.id.note_text);
            noteText.setText(note);
        }
    }
}

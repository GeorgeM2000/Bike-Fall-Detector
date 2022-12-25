package com.example.bikefalldetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Contacts extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView imageViewEmpty;
    TextView textViewEmptyText;
    FloatingActionButton add_button;
    CustomAdapter customAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        add_button = findViewById(R.id.add_button);
        recyclerView = findViewById(R.id.recyclerView);
        imageViewEmpty = findViewById(R.id.empty);
        textViewEmptyText = findViewById(R.id.no_data);


        // When user clicks the "Add" button, redirect the user to the AddContact page.
        add_button.setOnClickListener(view -> {
            Intent intent = new Intent(Contacts.this, AddContact.class);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(Contacts.this));

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {

                        DataSnapshot dataSnapshot = task.getResult();

                        // If there is a "contacts" field in the user information, retrieve the "contacts" information.
                        if(dataSnapshot.hasChild("contacts")) {

                            FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                                    .setQuery(FirebaseDatabase.getInstance().getReference()
                                            .child("Users")
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                            .child("contacts"), Contact.class).build();


                            customAdapter = new CustomAdapter(options, Contacts.this, Contacts.this);
                            customAdapter.startListening();
                            recyclerView.setAdapter(customAdapter);


                            // Don't show no data icon and text.
                            imageViewEmpty.setVisibility(View.GONE);
                            textViewEmptyText.setVisibility(View.GONE);
                        } else {
                            // Show no data icon and text.
                            imageViewEmpty.setVisibility(View.VISIBLE);
                            textViewEmptyText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Show no data icon and text.
                        imageViewEmpty.setVisibility(View.VISIBLE);
                        textViewEmptyText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "Contacts retrieval failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.delete_all) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete All?");
            builder.setMessage("Are you sure you want to delete all contacts?");
            builder.setPositiveButton("Yes", (dialogInterface, i) -> FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .child("contacts")
                    .setValue(null)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Toast.makeText(this, "All contacts have been deleted", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Contacts.this, Contacts.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Deletion failed", Toast.LENGTH_SHORT).show();
                        }
                    }));
            builder.setNegativeButton("No", (dialogInterface, i) -> Toast.makeText(Contacts.this, "Deletion cancelled.", Toast.LENGTH_LONG).show());

            builder.create().show();

        }
        return super.onOptionsItemSelected(item);
    }

    /*
        Function to tell the app to start getting
        data from database on starting of the activity.
    */
    @Override protected void onStart()
    {
        super.onStart();
    }

    /*
        Function to tell the app to stop getting
        data from database on stopping of the activity.
    */
    @Override protected void onStop()
    {
        super.onStop();
    }

    @Override protected void onDestroy()
    {
        super.onDestroy();
        //customAdapter.stopListening();
    }
}
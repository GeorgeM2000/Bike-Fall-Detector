package com.example.bikefalldetection;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class UpdateContact extends AppCompatActivity {

    EditText editTextFullName, editTextPhone;
    Button update_button, delete_button;
    String full_name, phone, contact_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

        // Text fields.
        editTextFullName = findViewById(R.id.full_name_u);
        editTextPhone = findViewById(R.id.phone_number_u);

        // Update and delete buttons.
        update_button = findViewById(R.id.update);
        delete_button = findViewById(R.id.delete);


        // Get data from indent and set the data to text fields.
        getSetIndentData();

        // Set action bar title.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(full_name);
        }

        // When user clicks the update contact button.
        update_button.setOnClickListener(view -> {
            // Prepare update data.
            Contact contact = new Contact();
            contact.setFull_name(editTextFullName.getText().toString());
            contact.setPhone(editTextPhone.getText().toString());

            // Update data to database.
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .child("contacts")
                    .child(contact_id)
                    .setValue(contact)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Toast.makeText(this, "Contact updated.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Delete contact in database.
        delete_button.setOnClickListener(view -> deletePrompt());

    }

    public void getSetIndentData() {
        if(getIntent().hasExtra("full_name") && getIntent().hasExtra("phone") && getIntent().hasExtra("contact_id")) {

            // Get data from indent.
            contact_id = getIntent().getStringExtra("contact_id");
            full_name = getIntent().getStringExtra("full_name");
            phone = getIntent().getStringExtra("phone");

            // Set data to text fields.
            editTextFullName.setText(full_name);
            editTextPhone.setText(phone);
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteContact() {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("contacts")
                .child(contact_id)
                .setValue(null)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete contact", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void deletePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete " + full_name + "?");
        builder.setMessage("Are you sure you want to delete " + full_name + " contact?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> deleteContact());
        builder.setNegativeButton("No", (dialogInterface, i) -> Toast.makeText(UpdateContact.this, "Delete cancelled.", Toast.LENGTH_LONG).show());

        builder.create().show();
    }
}
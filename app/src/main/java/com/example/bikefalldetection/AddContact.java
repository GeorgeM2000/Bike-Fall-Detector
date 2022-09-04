package com.example.bikefalldetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AddContact extends AppCompatActivity {


    EditText person_full_name, person_phone_number;
    Button buttonAddContact;
    DatabaseReference reference;
    ValueEventListener valueEventListener;
    long children_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        // Text Views
        person_full_name = findViewById(R.id.full_name);
        person_phone_number = findViewById(R.id.phone_number);

        // Button
        buttonAddContact = findViewById(R.id.add_contact);

        // Initialize database reference
        reference = FirebaseDatabase.getInstance().getReference()
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("contacts");

        // Set value event listener
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    children_count = snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };



        // Clear the text view when user clicks on it
        person_full_name.setOnClickListener(view -> person_full_name.getText().clear());
        person_phone_number.setOnClickListener(view -> person_phone_number.getText().clear());

        buttonAddContact.setOnClickListener(view -> {

            // Get full name and phone number from text views
            String full_name, phone;
            full_name = String.valueOf(person_full_name.getText());
            phone = String.valueOf(person_phone_number.getText()).replaceAll(" ", "");

            if(!full_name.equals("") && !phone.equals("")) {

                // Create contact object
                Contact contact = new Contact();
                contact.setFull_name(full_name);
                contact.setPhone(phone);

                reference
                        .child(String.valueOf(children_count))
                        .setValue(contact)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Toast.makeText(AddContact.this, "Contact Added.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddContact.this, "Failed To Add Contact", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Both Fields Must Be Filled.", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        reference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        reference.addValueEventListener(valueEventListener);
    }
}
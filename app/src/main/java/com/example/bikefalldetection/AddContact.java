package com.example.bikefalldetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    DatabaseReference databaseReference;
    long children_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        // Text Views.
        person_full_name = findViewById(R.id.full_name);
        person_phone_number = findViewById(R.id.phone_number);

        // "AddContact" Button.
        buttonAddContact = findViewById(R.id.add_contact);

        // Initialize database reference.
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("contacts");


        // Clear the text view when user clicks on it.
        person_full_name.setOnClickListener(view -> person_full_name.getText().clear());
        person_phone_number.setOnClickListener(view -> person_phone_number.getText().clear());

        buttonAddContact.setOnClickListener(view -> {

            // Get full name and phone number from text views.
            String full_name = String.valueOf(person_full_name.getText());
            String phone = String.valueOf(person_phone_number.getText()).replaceAll(" ", "");

            // If the input fields are not empty.
            if(!full_name.equals("") && !phone.equals("")) {

                // Create contact object.
                Contact contact = new Contact();
                contact.setFull_name(full_name);
                contact.setPhone(phone);

                databaseReference.get().addOnCompleteListener(task -> {
                    DataSnapshot dataSnapshot = task.getResult();
                    if(dataSnapshot.exists()) {
                        children_count = dataSnapshot.getChildrenCount();

                        dataSnapshot.getRef().child(String.valueOf(children_count))
                                .setValue(contact)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        Toast.makeText(AddContact.this, "Contact added", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddContact.this, "Failed to add contact", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(AddContact.this, "Failed to add contact", Toast.LENGTH_LONG).show();
                    }
                });



                databaseReference.child(String.valueOf(children_count))
                        .setValue(contact)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Toast.makeText(AddContact.this, "Contact added", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddContact.this, "Failed to add contact", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Both fields must be filled", Toast.LENGTH_LONG).show();
            }
        });
    }

}
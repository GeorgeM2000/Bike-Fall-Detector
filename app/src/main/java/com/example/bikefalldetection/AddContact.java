package com.example.bikefalldetection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class AddContact extends AppCompatActivity {


    EditText person_full_name, person_phone_number;
    Button buttonAddContact;
    long max_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        person_full_name = findViewById(R.id.full_name);
        person_phone_number = findViewById(R.id.phone_number);
        buttonAddContact = findViewById(R.id.add_contact);


        person_full_name.setOnClickListener(view -> person_full_name.getText().clear());
        person_phone_number.setOnClickListener(view -> person_phone_number.getText().clear());

        buttonAddContact.setOnClickListener(view -> {
            String full_name, phone;
            full_name = String.valueOf(person_full_name.getText());
            phone = String.valueOf(person_phone_number.getText()).replaceAll(" ", "");;

            HashMap<String, Object> userContacts = new HashMap<String, Object>(){{
                put("full_name", full_name);
                put("phone", phone);
            }};

            if(!full_name.equals("") && !phone.equals("")) {
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .child("contacts")
                        .child(full_name)
                        .setValue(userContacts)
                        .addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                Toast.makeText(this, "Contact Added.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed To Add Contact", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Both Fields Must Be Filled.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
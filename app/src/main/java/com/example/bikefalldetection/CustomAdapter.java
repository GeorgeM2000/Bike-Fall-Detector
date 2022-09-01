package com.example.bikefalldetection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class CustomAdapter extends FirebaseRecyclerAdapter<Contact, CustomAdapter.contactsViewholder> {

    Context context;
    Activity activity;
    Animation translate_anim;

    public CustomAdapter(@NonNull FirebaseRecyclerOptions<Contact> options, Context context, Activity activity) {
        super(options);
        this.activity = activity;
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull contactsViewholder holder, int position, @NonNull Contact model) {
        holder.contact_id_text.setText(String.valueOf(position+1));
        holder.full_name_text.setText(model.getFull_name());
        holder.phone_text.setText(model.getPhone());

        // When user clicks a recycler view row.
        holder.mainLayout.setOnClickListener(view -> {

            // Create a new intent and start a new activity.
            Intent intent = new Intent(context, UpdateContact.class);
            intent.putExtra("full_name", String.valueOf(model.getFull_name()));
            intent.putExtra("phone", String.valueOf(model.getPhone()));
            activity.startActivityForResult(intent, 1);
        });
    }


    @NonNull
    @Override
    public contactsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_row, parent, false);
        return new contactsViewholder(view);
    }

    class contactsViewholder extends RecyclerView.ViewHolder {

        TextView contact_id_text, full_name_text, phone_text;
        LinearLayout mainLayout;

        public contactsViewholder(@NonNull View itemView) {
            super(itemView);
            contact_id_text = itemView.findViewById(R.id.contact_id_text);
            full_name_text = itemView.findViewById(R.id.full_name_text);
            phone_text = itemView.findViewById(R.id.phone_text);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            mainLayout.setAnimation(translate_anim);

        }
    }
}

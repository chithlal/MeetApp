package com.divinedev.meetapp.Adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.divinedev.meetapp.Helper.Contacts;
import com.divinedev.meetapp.R;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {
    private List<Contacts> contacts;
    private Context context;

    public ContactAdapter(List<Contacts> contacts,Context context){

        this.contacts = contacts;
        this.context = context;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone, url;
        ImageView profilePic;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.contact_name);
            phone = (TextView) view.findViewById(R.id.contact_phone);
            profilePic = (ImageView) view.findViewById(R.id.contact_pic);
        }
    }

    @NonNull
    @Override
    public ContactAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.MyViewHolder holder, int position) {
       Contacts contact = contacts.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());
        if(contacts.get(position).isActive()){
              holder.name.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
        else {
            holder.name.setTextColor(context.getResources().getColor(R.color.dark));
        }
        //holder.profilePic.setImageDrawable(R.drawable.common_full_open_on_phone);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}

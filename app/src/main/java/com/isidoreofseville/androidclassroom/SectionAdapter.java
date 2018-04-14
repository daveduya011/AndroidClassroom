package com.isidoreofseville.androidclassroom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by Dave on 3/21/2018.
 */

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {
    private Context context;

    private List<User> list;
    private LayoutInflater inflater;

    public SectionAdapter(Context context, List<User> list) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.section_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = list.get(position);

        holder.getProfilepicture().setImageURI(user.getPicture());
        holder.getFirstname().setText(user.getFirstname());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private SimpleDraweeView profilepicture;
        private TextView firstname;
        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);
            profilepicture = view.findViewById(R.id.profilepicture);
            firstname = view.findViewById(R.id.firstname);
        }

        public SimpleDraweeView getProfilepicture() {
            return profilepicture;
        }

        public TextView getFirstname() {
            return firstname;
        }

        @Override
        public void onClick(View view) {
            viewProfile(list.get(getAdapterPosition()), list.get(getAdapterPosition()).getKey());
        }

        public void viewProfile(User currentPostAuthor, String currentAuthorUID) {
            Intent intent = new Intent(context, Profile.class);
            Bundle bundle = new Bundle();
            intent.putExtra("isOwned", false);
            intent.putExtra("UID", currentAuthorUID);
            intent.putExtra("user", currentPostAuthor);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}

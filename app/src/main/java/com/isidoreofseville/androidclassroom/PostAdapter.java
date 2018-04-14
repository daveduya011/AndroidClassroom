package com.isidoreofseville.androidclassroom;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dave on 12/28/2017.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    private List<Post> list;
    private Activity context;

    private int lastPosition = -1;

    public PostAdapter(List<Post> list, Activity context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rowView;

        switch (viewType){
            case VIEW_TYPES.NORMAL:
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row, parent, false);
                break;
            case VIEW_TYPES.HEADER:
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row_header, parent, false);
                break;
            case VIEW_TYPES.FOOTER:
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row_footer, parent, false);
                break;
            default:
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row, parent, false);
                break;

        }

        return new PostViewHolder(rowView, viewType);
    }

    @Override
    public void onBindViewHolder(final PostViewHolder holder, final int position) {

        Post post = list.get(position);
        int type = getItemViewType(position);

        if (type == VIEW_TYPES.HEADER){
            holder.txtHeader.setText(post.getText());
        }
        else if (type == VIEW_TYPES.FOOTER){
            holder.txtFooter.setText(post.getText());
        }
        else  {

            holder.title.setText(post.getTitle());
            int maxLength = 30;
            if (list.get(position).getMessage().length() > maxLength){
                holder.message.setText(post.getMessage().substring(0,maxLength) + "...more");
            } else {
                holder.message.setText(post.getMessage());
            }
            holder.authorName.setText("by " + post.getAuthorName());
            holder.dateCreated.setText(post.getDateCreated() + " " + post.getTimeCreated());

        }
        setAnimation(holder.itemView, position);

    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.abc_grow_fade_in_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void openActivity(int position){
        Intent intent = new Intent(context, PostContent.class);
        intent.putExtra("key", list.get(position).getKey());
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
    }

    public void deletePost(final int position){
        AlertDialog.Builder alertBuilder;
        alertBuilder  = new AlertDialog.Builder(context);


        alertBuilder.setMessage("Are you sure you want to delete this post?").setTitle("Delete");
        alertBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DateRetriever.isConnected()){
                    Database.ref_posts.child(list.get(position).getKey()).removeValue();
                    list.remove(position);
                    notifyItemRemoved(position);
                } else {
                    Toast.makeText(context, "No internet connection. Please try again", Toast.LENGTH_SHORT).show();
                }
            }

        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //DO NOTHING
            }
        });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();

    }


    public void reportPost(final int position) {
        AlertDialog.Builder alertBuilder;
        alertBuilder  = new AlertDialog.Builder(context);


        alertBuilder.setMessage("Report post?").setTitle("Report");
        alertBuilder.setPositiveButton("Report", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DateRetriever.isConnected()){

                    DatabaseReference directory = Database.ref_reports.push();
                    Map<String, Object> map = new HashMap<>();
                    map.put("dateReported", DateRetriever.getDateToday());
                    map.put("timeReported", DateRetriever.getTime());
                    map.put("user", MainActivity.getUser());
                    map.put("key", list.get(position).getKey());
                    map.put("title", list.get(position).getTitle());
                    map.put("author", list.get(position).getAuthorName());
                    map.put("message", list.get(position).getMessage());
                    map.put("Section", MainActivity.userInfo.getSection());
                    map.put("user_name", MainActivity.userInfo.getFirstname() + MainActivity.userInfo.getLastname());

                    directory.setValue(map);
                    directory.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Toast.makeText(context, "Report has been delivered to the admin. Thank you for your action.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } else {
                    Toast.makeText(context, "No internet connection. Please try again", Toast.LENGTH_SHORT).show();
                }
            }

        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //DO NOTHING
            }
        });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void editPost(int position){
        Intent intent = new Intent(context, EditPostActivity.class);
        intent.putExtra("key", list.get(position).getKey());
        context.startActivity(intent);
    }

    @Override
    public void onViewDetachedFromWindow(PostViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).isHeader()){
            return VIEW_TYPES.HEADER;
        }
        else if (list.get(position).isFooter()){
            return VIEW_TYPES.FOOTER;
        }
        else {
            return VIEW_TYPES.NORMAL;
        }
    }

    public class VIEW_TYPES {
        public static final int HEADER = 1;
        public static final int NORMAL = 2;
        public static final int FOOTER = 3;
    }

    public class MENU {
        public static final int VIEW = 0;
        public static final int DELETE = 1;
        public static final int EDIT = 2;
        public static final int FAV = 3;
        public static final int REPORT = 4;
    }

    class PostViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

        FontCustomizer fontCustomizer;
        TextView title, message, authorName, dateCreated;
        TextView txtHeader, txtFooter;


        public PostViewHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == VIEW_TYPES.HEADER)
                this.txtHeader = itemView.findViewById(R.id.txtHeader);
            else if (viewType == VIEW_TYPES.FOOTER)
                this.txtFooter = itemView.findViewById(R.id.txtFooter);
            else
            {
                this.title = itemView.findViewById(R.id.postTitle);
                this.message = itemView.findViewById(R.id.postMessage);
                this.authorName = itemView.findViewById(R.id.postAuthorName);
                this.dateCreated = itemView.findViewById(R.id.postDate);
            }


            itemView.setOnClickListener(onClickListener);

            itemView.setOnCreateContextMenuListener(this);
            setFonts(itemView, viewType);

        }

        public View.OnClickListener onClickListener = new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                openActivity(getAdapterPosition());
            }
        };

        private void setFonts(View view, int viewType){
            if (viewType == VIEW_TYPES.NORMAL){
                TextView[] tv1 = {this.message, this.authorName, this.dateCreated};
                TextView[] tv2 = {this.title};

                fontCustomizer = new FontCustomizer(view.getContext());
                fontCustomizer.setToQuickSand(tv1);
                fontCustomizer.setToQuickSandBold(tv2);
            }
            else if (viewType == VIEW_TYPES.HEADER){
                fontCustomizer = new FontCustomizer(view.getContext());
                fontCustomizer.setToQuickSandBold(txtHeader);
            }

            else if (viewType == VIEW_TYPES.FOOTER){
                fontCustomizer = new FontCustomizer(view.getContext());
                fontCustomizer.setToQuickSandBold(txtFooter);
                txtFooter.setTextColor(view.getResources().getColor(R.color.lightred));
            }

        }


        public void clearAnimation()
        {
            itemView.clearAnimation();
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

            contextMenu.setHeaderTitle("Post");
            contextMenu.add(getAdapterPosition(), PostAdapter.MENU.VIEW, 0, "View");

            if (list.get(getAdapterPosition()).isOwnedPost() || MainActivity.user_hasDeletePermission){

                contextMenu.add(getAdapterPosition(), PostAdapter.MENU.DELETE, 0, "Delete");

            }
            if (list.get(getAdapterPosition()).isOwnedPost() || MainActivity.user_hasEditPermission){

                contextMenu.add(getAdapterPosition(), PostAdapter.MENU.EDIT, 0, "Edit");
            }

            if (!list.get(getAdapterPosition()).isOwnedPost()){
                contextMenu.add(getAdapterPosition(), PostAdapter.MENU.REPORT, 0, "Report");
            }

        }


    }

}

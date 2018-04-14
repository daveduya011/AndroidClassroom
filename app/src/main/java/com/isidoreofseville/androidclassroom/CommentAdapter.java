package com.isidoreofseville.androidclassroom;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Dave on 1/23/2018.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{

    private List<Comment> list;
    private Context context;

    private int lastPosition = -1;

    public CommentAdapter(List<Comment> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row, parent, false);
        return new CommentViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(final CommentViewHolder holder, int position) {
        final Comment comment = list.get(position);
        holder.authorName.setText(comment.getAuthorName());
        holder.message.setText(comment.getMessage());
        holder.dateCreated.setText(comment.getDateCreated() + " " + comment.getTimeCreated());

        final User[] user = new User[1];
        Database.ref_users.child(comment.getAuthor()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user[0] = dataSnapshot.getValue(User.class);
                holder.draweeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.viewProfile(user[0], comment.getAuthor());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (comment.getAuthorPictureUri() != null)
            holder.setAuthorPicture(comment.getAuthorPictureUri()
            );
    }

    @Override
    public void onViewDetachedFromWindow(CommentViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.clearAnimation();
    }


    public void deletePost(final int position, final String postKey, final Activity activity){
        AlertDialog.Builder alertBuilder;
        alertBuilder  = new AlertDialog.Builder(context);


        alertBuilder.setMessage("Are you sure you want to delete this comment?").setTitle("Delete");
        alertBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Database.ref_posts.child(postKey).child("comments").child(list.get(position).getKey()).removeValue();
                activity.recreate();
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

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

        FontCustomizer fontCustomizer;
        TextView message, authorName, dateCreated;
        SimpleDraweeView draweeView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            this.message = itemView.findViewById(R.id.postMessage);
            this.authorName = itemView.findViewById(R.id.authorName);
            this.dateCreated = itemView.findViewById(R.id.postDate);
            this.draweeView = itemView.findViewById(R.id.authorProfile);

            itemView.setOnCreateContextMenuListener(this);
            setFonts(itemView);
        }

        private void setFonts(View view){
                TextView[] tv1 = {this.message, this.authorName, this.dateCreated};
                TextView[] tv2 = {this.authorName};

                fontCustomizer = new FontCustomizer(view.getContext());
                fontCustomizer.setToQuickSand(tv1);
                fontCustomizer.setToQuickSandBold(tv2);

        }

        public void setAuthorPicture(Uri uri){
            draweeView = itemView.findViewById(R.id.authorProfile);
            draweeView.setImageURI(uri);
        }

        public void clearAnimation()
        {
            itemView.clearAnimation();
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

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

            if (list.get(getAdapterPosition()).isOwnedPost() || MainActivity.user_hasDeletePermission){
                contextMenu.add(getAdapterPosition(), PostAdapter.MENU.DELETE, 0, "Delete Post");
            }

        }
    }
}

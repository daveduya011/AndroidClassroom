package com.isidoreofseville.androidclassroom;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Dave on 3/4/2018.
 */

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder>{

    public List<FileUploaded> fileNameList;

    public UploadListAdapter(List<FileUploaded> fileNameList) {
        this.fileNameList = fileNameList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String fileName = fileNameList.get(position).getKey();
        holder.txtFilename.setText(fileName);

        if (fileNameList.get(position).isUploaded() == true){
            holder.progressFiledone.setVisibility(View.GONE);
            holder.imgUploaded.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        public TextView txtFilename;
        public ProgressBar progressFiledone;
        public ImageView imgUploaded;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            txtFilename = view.findViewById(R.id.txtFileName);
            progressFiledone = view.findViewById(R.id.progressBar_upload);
            imgUploaded = view.findViewById(R.id.img_uploaded);
        }

    }

}

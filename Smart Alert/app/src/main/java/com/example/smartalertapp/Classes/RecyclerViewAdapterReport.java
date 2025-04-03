package com.example.smartalertapp.Classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartalertapp.ImageViewerActivity;
import com.example.smartalertapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecyclerViewAdapterReport extends RecyclerView.Adapter<RecyclerViewAdapterReport.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<Report> ReportsList;

    StorageReference storageReference;


    public RecyclerViewAdapterReport(RecyclerViewInterface recyclerViewInterface, Context context, ArrayList<Report> ReportsList, StorageReference storageReference) {
        this.recyclerViewInterface = recyclerViewInterface;
        this.context = context;
        this.ReportsList = ReportsList;
        this.storageReference = storageReference;
    }


    @NonNull
    @Override
    public RecyclerViewAdapterReport.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_holder_report, parent, false);
        return new RecyclerViewAdapterReport.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterReport.MyViewHolder holder, int position) {

        holder.textViewUsername.setText(ReportsList.get(position).Username);
        holder.textViewTimestamp.setText(ReportsList.get(position).timestamp);
        holder.textViewLatitude.setText(context.getString(R.string.recycler_view_latitude_label, ReportsList.get(position).latitude));
        holder.textViewLongitude.setText(context.getString(R.string.recycler_view_longitude_label, ReportsList.get(position).longitude));
        holder.textViewDangerScore.setText(context.getString(R.string.recycler_view_danger_score_label, ReportsList.get(position).danger_score));
        if (!ReportsList.get(position).comment.equals("")){
            holder.textViewComment.setText(context.getString(R.string.recycler_view_comment, ReportsList.get(position).comment));
        }
        else{
            holder.textViewComment.setText("\n");
        }

        if (ReportsList.get(position).containsImage){

            holder.cardView.setVisibility(View.VISIBLE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintLayout);
            constraintSet.connect(R.id.textViewComment, ConstraintSet.TOP, R.id.cardView, ConstraintSet.BOTTOM, 8);
            constraintSet.applyTo(holder.constraintLayout);

            if (ReportsList.get(position).bitmap != null){
                //  If image was found before
                holder.imageViewPhoto.setImageBitmap(ReportsList.get(position).bitmap);
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            else{
                try {
                    File file = File.createTempFile("temp", "jpeg");
                    StorageReference currentStorageRef = storageReference.child(ReportsList.get(position).ReportID);

                    currentStorageRef.getFile(file)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    ReportsList.get(position).bitmap = bitmap;
                                    ReportsList.get(position).bitmap_Uri = Uri.fromFile(file);

                                    holder.imageViewPhoto.setImageBitmap(bitmap);
                                    holder.cardView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(context, ImageViewerActivity.class);
                                            intent.putExtra(ImageViewerActivity.ARG_PARAM_BITMAP_Uri, ReportsList.get(position).bitmap_Uri.toString());
                                            context.startActivity(intent);
                                        }
                                    });
                                }
                            });


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


        }
        else {
            holder.cardView.setVisibility(View.INVISIBLE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintLayout);
            constraintSet.connect(R.id.textViewComment, ConstraintSet.TOP, R.id.textViewDangerScore, ConstraintSet.BOTTOM, 8);
            constraintSet.applyTo(holder.constraintLayout);
        }




    }

    @Override
    public int getItemCount() {
        return ReportsList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout constraintLayout;
        CardView cardView;
        ImageView imageViewPhoto;
        TextView textViewUsername, textViewTimestamp, textViewLongitude, textViewLatitude, textViewDangerScore, textViewComment;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            cardView = itemView.findViewById(R.id.cardView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewLongitude  = itemView.findViewById(R.id.textViewLongitude);
            textViewLatitude = itemView.findViewById(R.id.textViewLatitude);
            textViewDangerScore = itemView.findViewById(R.id.textViewDangerScore);
            textViewComment  = itemView.findViewById(R.id.textViewComment);

        }
    }

}

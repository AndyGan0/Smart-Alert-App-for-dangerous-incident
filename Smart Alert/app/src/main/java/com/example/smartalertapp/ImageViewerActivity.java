package com.example.smartalertapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String ARG_PARAM_BITMAP_Uri = "bitmap_Uri";

    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        imageView = findViewById(R.id.imageView8);

        Bitmap bitmap = null;

        String bitmap_Uri = getIntent().getStringExtra(ARG_PARAM_BITMAP_Uri);
        if (bitmap_Uri != null) {
            Uri imageUri = Uri.parse(bitmap_Uri);
            File file = new File(imageUri.getPath());
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }


        }

    }



    public void close(View view){
        finish();
    }
}
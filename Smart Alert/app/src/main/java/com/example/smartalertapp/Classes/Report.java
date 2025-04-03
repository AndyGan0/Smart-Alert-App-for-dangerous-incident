package com.example.smartalertapp.Classes;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class Report implements Serializable {

    public String ReportID;

    public String Username;


    public double longitude;
    public double latitude;

    public String timestamp;

    public String comment;

    public int danger_score;


    //  imageFileName Is Only Available If user uploaded a photo
    public boolean containsImage;




    //  Stores the bitmap to avoid downloading the image in recycler viewer every time it recycles the views
    //  Its used mainly for recycler viewer
    public Bitmap bitmap;
    public Uri bitmap_Uri;


}

package com.example.smartalertapp.Classes;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.smartalertapp.R;
import com.example.smartalertapp.UserMainMenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {

    FirebaseAuth auth;
    FirebaseUser user;

    FirebaseDatabase database;
    DatabaseReference ref;


    LocationManager locationManager;


    @Override
    public void onCreate() {
        super.onCreate();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        user = auth.getCurrentUser();
        if (user == null) {
            if (ref != null) {
                //  User logged out
                ref.setValue(null);
            }
        } else {
            //  User is logged in
            ref = database.getReference("Users").child(user.getUid()).child("FirebaseMessagingToken");
            ref.setValue(token);
        }

    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);


        if (message.getData().size() > 0) {

            // Extract data
            String incident_ID = message.getData().get("incident_id");

            String incident_type_id = message.getData().get("incident_type");
            IncidentType incidentType = IncidentType.getIncidentTypeWithID(incident_type_id);

            double Center_latitude = Double.parseDouble(message.getData().get("incident_center_latitude"));
            double Center_longitude = Double.parseDouble(message.getData().get("incident_center_longitude"));
            double Radius = Double.parseDouble(message.getData().get("incident_radius"));

            String timestamp = "";
            if (message.getData().containsKey("incident_timestamp")){
                timestamp = message.getData().get("incident_timestamp");
            }


            String Message = message.getData().get("incident_message");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            try{

                String finalTimestamp = timestamp;
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {

                        double user_longitude = location.getLongitude();
                        double user_latitude = location.getLatitude();

                        double diff_x = Center_longitude - user_longitude;
                        double diff_y = Center_latitude - user_latitude;

                        double distance = Math.sqrt( Math.pow(diff_x, 2) + Math.pow(diff_y, 2) );

                        if (distance < Radius + incidentType.get_Extra_radius()) {

                            String title = "";
                            int icon_id = 0;
                            if (incidentType == IncidentType.FIRE){
                                title = getString(R.string.notification_title , getString(R.string.incident_type_fire));
                                icon_id = R.drawable.fire_icon;
                            }
                            else if (incidentType == IncidentType.EARTHQUAKE){
                                title = getString(R.string.notification_title , getString(R.string.incident_type_earthquake));
                                icon_id = R.drawable.earthquake_icon;
                            }
                            else if (incidentType == IncidentType.FLOOD){
                                title = getString(R.string.notification_title , getString(R.string.incident_type_flood));
                                icon_id = R.drawable.flood_icon;
                            }
                            else if (incidentType == IncidentType.TORNADO){
                                title = getString(R.string.notification_title , getString(R.string.incident_type_tornado));
                                icon_id = R.drawable.tornado_icon;
                            }
                            else if (incidentType == IncidentType.TSUNAMI){
                                title = getString(R.string.notification_title , getString(R.string.incident_type_tsunami));
                                icon_id = R.drawable.tsunami_icon;
                            }
                            else if (incidentType == IncidentType.AVALANCHE){
                                title = getString(R.string.notification_title , getString(R.string.incident_type_avalanche));
                                icon_id = R.drawable.avalanche_icon;
                            }

                            String Body = "Latitude: " + Center_latitude + "\n" +
                                    "Longitude: " + Center_longitude + "\n" +
                                    "Radius: " + (Radius+ + incidentType.get_Extra_radius()) + "\n" +
                                    finalTimestamp + "\n\n" +
                                    getString(R.string.message_from_employee) +
                                    Message;

                            showNotification(title, Body, icon_id);
                            saveIncidentInDatabase(incident_ID, incidentType, finalTimestamp);


                        }


                    }
                },Looper.getMainLooper());

            }
            catch (SecurityException e) {
                e.printStackTrace();
            }


        }

    }









    public void showNotification(String title, String body, int icon_id) {
            //  Show notification

            String ChanelID = "SMART_ALERT_NOTIFICATION_CHANEL_ID";
            String ChanelName = "Smart Alert Notifications";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ChanelID);
            builder.setSmallIcon(icon_id)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            Intent intent = new Intent(getApplicationContext(), UserMainMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            builder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(ChanelID);


                if (notificationChannel == null){
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    notificationChannel = new NotificationChannel(ChanelID, ChanelName, importance);
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }

            }

            notificationManager.notify(0, builder.build());


    }





    private void saveIncidentInDatabase(String incident_ID, IncidentType Type, String timestamp ){

        SQLiteDatabase SqlDatabase = openOrCreateDatabase("smartalert.db", MODE_PRIVATE, null);
        SqlDatabase.execSQL("Create table if not exists NotifiedIncidents(" +
                            "IncidentID Text Primary Key," +
                            "TypeID Text," +
                            "timestamp Text)");

        String query = "Insert or ignore into NotifiedIncidents Values( '" + incident_ID + "','" + Type.getTypeID() + "','" + timestamp + "')";

        SqlDatabase.execSQL(query);

    }




}

package com.example.smartalertapp.Classes;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.firebase.FirebaseOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PushNotificationSender {

    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    private static String getAccessToken(){

        try {
            String JSONString = "{\n" +
                    " \"type\": \"service_account\",\n" +
                    " \"project_id\": \"smart-alert-app-44f1f\",\n" +
                    " \"private_key_id\": \"2aef7840cfbee026976f878078cbe8ad50cf3865\",\n" +
                    " \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDTvWesxrXp4Ptr\\nFiuU+ydit+SwIwqNfYnTjF3LhKLesWMVDE/weNZT650okbaOkXy6BA/au22Iv2RI\\nxMTY68cdtNUvj4cfsHtNinqNkG+tARmwpkB8DIrPCvoNeryqI0kdyCwPXu1nlkhn\\nJyg0S0MuZPmOQOTM5GpVCGMaEhoj9ZBk05vIvS38bN2mM5BkX0t19GBxNQNIl4ud\\nfRfKxotYzgjMt0oijaNM2ZeJ7jpn/arI2VcHj4P7T1kEWnh6LrJ0TUC0X6fsyfhL\\nNCnkls07VQr06PjcYO2sF7Ir4opoGsif4yBKop+vBDpz3a3x5uUecAW6mcJwXRe0\\nWzBXa5FhAgMBAAECggEAAOVqcsphAvlFKyP8AXyKI0JyvPWbAfQL8SmlDHtWWqGu\\n0QwdKwAB7TdAMieiuYn/WISzofn82dEwjhfKGv5teHBXXiUJ/f332aFKST7ZICby\\n6kLb30KI1F1AJ24zbfdqA+6g1BxTXia+TfR2GLmQUloSTdIzvbPCSPOZAsdclbYe\\ngaBdlp4yPqaht9EF+Dxlf1WX9OAMITss67cZvtp1PGNDEVEKyCFWMFuhis8JnZoi\\nt7ByUjNVcnDRdehcjTepQ29UE/KdAkfagcjckOFZVxLjaOmDFW/AzJnuHnKZlmnP\\n64bZFQGtbnbvV7Nn78iJ/YUaLc4SP8a34ABQH9uZSQKBgQD4Z4FFRjx9z05cy+dd\\nKdbcgd/Ahi7dKTsJ73Zje5gBV7SPP9sN14MutaVvtDPft0ay2hOTFA4t6qKZprR0\\n1G2SV+1fvAXdpC+ARzfnAA0gu51FZB4JViUiH7sqm6AT0BxFEwQ83cy8trPywXKg\\niLzK8UzB0LnVn5BrUtqmSo9QuQKBgQDaNuSKcLREy4Rg1BNn8WX0OGjOGAN9qPGJ\\nR49zZmne8RC/gXP31i4KReuuhiqPoV6l8+2OvxnUt0BHLOWeg0s83S8RDd0AuCzV\\nzAnIJkKiiIFy6oFVyYckoX4g/9k3Pq1Vb8gPg6tfjQepSap+Ljv2W6kjdzMOERbU\\njo11tA1h6QKBgQD25kpDBNDTa95Dthg9LXz2p9AymCy0fmrEMVPHBe3MR9ScHagp\\nVfB52Oqa4M4+qviGhOO36NM/KFx9xIjRJuPR9btm0Ig0CSF9q0wROqfRxSlI8343\\nYXx/MnLiotS1XMD8ly47IWqCmEa8HzNEBSVBwh289y3++x6czjmt16YjYQKBgCoe\\nslpuir1Es1alWOGVXOKZCNHPHM1ikMy4vV2/BOx9h4gPirzp4JUnDejwCMMUmNpb\\njR0ike/XN1cFuYSxPxKOdwlJrcD3y9+wtKqnh/ErfCEcVr6iWa2eZmQkPl9Ff9uf\\n6fPkrG2TIUlx6LDWZ8iTxMANwBMVBV0d7vwrNvIRAoGBAIf8zbn75RVl47loK2SH\\nLYHXJ7CuHsU11T2dX3B0UjWsBIT0usUKQ1xeXFtzRshspKBv09dq2F0q1B+IgviD\\nW7mJwLqEZNPbYVDYYmXJDqfKyWlQdWOQqLVYEHj5BqvyGgePiloqkk1mhdVnEnRD\\nU6u1vkr+sRcnX2i7lBxSE6m5\\n-----END PRIVATE KEY-----\\n\",\n" +
                    " \"client_email\": \"firebase-adminsdk-3630i@smart-alert-app-44f1f.iam.gserviceaccount.com\",\n" +
                    " \"client_id\": \"118154494789862627033\",\n" +
                    " \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    " \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    " \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    " \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-3630i%40smart-alert-app-44f1f.iam.gserviceaccount.com\",\n" +
                    " \"universe_domain\": \"googleapis.com\"\n" +
                    " }\n";

            InputStream stream = new ByteArrayInputStream(JSONString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList(firebaseMessagingScope));

            googleCredentials.refresh();

            return googleCredentials.getAccessToken().getTokenValue();

        }
        catch (IOException e){
            Log.e("error", e.getMessage());
            return null;
        }
    }







    private static final String postURL = "https://fcm.googleapis.com/v1/projects/smart-alert-app-44f1f/messages:send";

    public static void sendPushNotification(List<String> UserTokens, String Message, Incident incident, Context context){

        String AccessToken = getAccessToken();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        for (String token: UserTokens){
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject mainObj = new JSONObject();

            try {

                //JSONObject notificationObject = new JSONObject();
                //notificationObject.put("title", title);
                //notificationObject.put("body", body);


                JSONObject DataObject = new JSONObject();
                DataObject.put("incident_id", incident.IncidentID);
                DataObject.put("incident_type", incident.Type.getTypeID());
                DataObject.put("incident_center_latitude", String.valueOf(incident.Center_latitude));
                DataObject.put("incident_center_longitude", String.valueOf(incident.Center_longitude));
                DataObject.put("incident_radius", String.valueOf(incident.Radius));
                DataObject.put("incident_message", Message);

                Optional<Report> latestObject = incident.ReportList.stream().max(Comparator.comparing( obj -> LocalDateTime.parse(obj.timestamp, formatter) ) );
                if (latestObject.isPresent()){
                    DataObject.put("incident_timestamp", latestObject.get().timestamp);
                }

                JSONObject messageObject = new JSONObject();
                messageObject.put("token", token);
                messageObject.put("data", DataObject);
                //messageObject.put("notification", notificationObject);

                mainObj.put("message", messageObject);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postURL, mainObj, response -> {
                    //  Code run
                }, VolleyError -> {
                    //   Error
                }) {
                    @NonNull
                    @Override
                    public Map<String , String> getHeaders(){
                        String accessToken = getAccessToken();
                        Map<String, String> header = new HashMap<>();
                        header.put("content-type", "application/json");
                        header.put("authorization", "Bearer " + accessToken);
                        return header;

                    }
                };

                requestQueue.add(request);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }







}

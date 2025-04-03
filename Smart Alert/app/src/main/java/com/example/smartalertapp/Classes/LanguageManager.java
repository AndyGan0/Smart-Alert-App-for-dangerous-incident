package com.example.smartalertapp.Classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LanguageManager {


    public static final String Greek_langCode = "el";
    public static final String English_langCode = "en";



    private static  void setLocal(Activity activity, String langCode, boolean isStartUp){
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();

        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        if (!isStartUp){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("LangCode", langCode);
            editor.apply();

            Intent intent = activity.getIntent();
            activity.finish();
            activity.startActivity(intent);
        }


    }


    public static void setLocal(Activity activity, String langCode){
        setLocal(activity, langCode, false);
    }


    public static void loadSelectedLanguage(Activity activity){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String langCode = preferences.getString("LangCode","en");

        setLocal(activity, langCode, true);
    }


}

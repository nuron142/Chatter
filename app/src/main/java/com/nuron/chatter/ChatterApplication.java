package com.nuron.chatter;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class ChatterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, getResources().getString(R.string.PARSE_APPLICATION_ID),
                getResources().getString(R.string.PARSE_CLIENT_ID));
        ParseFacebookUtils.initialize(this);
    }

}
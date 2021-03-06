package com.nuron.chatter;

import android.app.Application;

import com.nuron.chatter.Model.ChatGroup;
import com.nuron.chatter.Model.ChatGroupMessage;
import com.nuron.chatter.Model.ChatSingleMessage;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.Model.ParseFriendRequest;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

public class ChatterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(ChatSingleMessage.class);
        ParseObject.registerSubclass(ChatGroupMessage.class);
        ParseObject.registerSubclass(ChatGroup.class);
        ParseObject.registerSubclass(ParseFriend.class);
        ParseObject.registerSubclass(ParseFriendRequest.class);

        Parse.initialize(this, getResources().getString(R.string.PARSE_APPLICATION_ID),
                getResources().getString(R.string.PARSE_CLIENT_ID));
        ParseFacebookUtils.initialize(this);
    }

}
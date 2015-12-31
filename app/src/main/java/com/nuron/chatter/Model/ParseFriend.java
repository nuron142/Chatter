package com.nuron.chatter.Model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by nuron on 26/12/15.
 */

@ParseClassName("ParseFriend")
public class ParseFriend extends ParseObject {

    public static final String OBJECT_ID = "objectId";
    public static final String USER_ID = "userId";
    public static final String USER_NAME_LOWER_CASE = "userNameLowercase";
    public static final String FRIEND_NAME_LOWER_CASE = "friendNameLowerCase";
    public static final String FRIEND_ID = "friendId";
    public static final String UPDATED_AT = "updatedAt";

    public static final String STRING_TRUE = "true";
    public static final String STRING_FALSE = "false";

    private String userId;
    private String userName;
    private String userNameLowercase;
    private String friendId;
    private String friendName;
    private String friendNameLowerCase;
    private String requestSent;
    private String requestAccepted;

    public ParseFriend(){
    }

    public String getRequestSent() {
        return getString("requestSent");
    }

    public void setRequestSent(String requestSent) {
        put("requestSent", requestSent);
    }

    public String getRequestAccepted() {
        return getString("requestAccepted");
    }

    public void setRequestAccepted(String requestAccepted) {
        put("requestAccepted", requestAccepted);
    }

    public String getFriendNameLowerCase() {
        return getString("friendNameLowerCase");
    }

    public void setFriendNameLowerCase(String friendNameLowerCase) {
        put("friendNameLowerCase", friendNameLowerCase);
    }

    public String getUserNameLowercase() {
        return getString("userNameLowercase");
    }

    public void setUserNameLowercase(String userNameLowercase) {
        put("userNameLowercase", userNameLowercase);
    }


    public String getUserName() {
        return getString("userName");
    }

    public void setUserName(String userName) {
        put("userName", userName);
    }

    public String getFriendName() {
        return getString("friendName");
    }

    public void setFriendName(String friendName) {
        put("friendName", friendName);
    }

    public String getFriendId() {
        return getString("friendId");
    }

    public void setFriendId(String friendId) {
        put("friendId", friendId);
    }

    public String getUserId() {
        return getString("userId");
    }

    public void setUserId(String userId) {
        put("userId", userId);
    }


}

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

    private String userId;
    private String userName;
    private String userNameLowercase;
    private String friendId;
    private String friendName;
    private String friendNameLowerCase;
    private String friendEmail;


    public ParseFriend() {
    }

    public ParseFriend(ParseFriendRequest parseFriendRequest, boolean isUser) {

        if (isUser) {

            setUserId(parseFriendRequest.getUserId());
            setUserName(parseFriendRequest.getUserName());
            setUserNameLowercase(parseFriendRequest.getUserNameLowercase());
            setFriendId(parseFriendRequest.getFriendId());
            setFriendName(parseFriendRequest.getFriendName());
            setFriendNameLowerCase(parseFriendRequest.getFriendNameLowerCase());
            setFriendEmail(parseFriendRequest.getFriendEmail());

        } else {

            setUserId(parseFriendRequest.getFriendId());
            setUserName(parseFriendRequest.getFriendName());
            setUserNameLowercase(parseFriendRequest.getFriendNameLowerCase());
            setFriendId(parseFriendRequest.getUserId());
            setFriendName(parseFriendRequest.getUserName());
            setFriendNameLowerCase(parseFriendRequest.getUserNameLowercase());
            setFriendEmail(parseFriendRequest.getUserEmail());

        }

    }

    public String getUserId() {
        return getString("userId");
    }

    public void setUserId(String userId) {
        put("userId", userId);
    }

    public String getUserName() {
        return getString("userName");
    }

    public void setUserName(String userName) {
        put("userName", userName);
    }

    public String getUserNameLowercase() {
        return getString("userNameLowercase");
    }

    public void setUserNameLowercase(String userNameLowercase) {
        put("userNameLowercase", userNameLowercase);
    }


    public String getFriendId() {
        return getString("friendId");
    }

    public void setFriendId(String friendId) {
        put("friendId", friendId);
    }

    public String getFriendName() {
        return getString("friendName");
    }

    public void setFriendName(String friendName) {
        put("friendName", friendName);
    }

    public String getFriendNameLowerCase() {
        return getString("friendNameLowerCase");
    }

    public void setFriendNameLowerCase(String friendNameLowerCase) {
        put("friendNameLowerCase", friendNameLowerCase);
    }

    public String getFriendEmail() {
        return getString("friendEmail");
    }

    public void setFriendEmail(String friendEmail) {
        put("friendEmail", friendEmail);
    }
}

package com.nuron.chatter.LocalModel;

import com.nuron.chatter.Model.ParseFriend;

/**
 * Created by nuron on 02/01/16.
 */
public class LocalFriend {
    ParseFriend parseFriend;
    String friendName;
    String friendId;
    String friendNameLowerCase;

    public LocalFriend(ParseFriend parseFriend){
        this.parseFriend = parseFriend;
    }

    public ParseFriend getParseFriend() {
        return parseFriend;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendNameLowerCase() {
        return friendNameLowerCase;
    }

    public void setFriendNameLowerCase(String friendNameLowerCase) {
        this.friendNameLowerCase = friendNameLowerCase;
    }
}

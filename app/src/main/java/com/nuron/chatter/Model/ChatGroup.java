package com.nuron.chatter.Model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by nuron on 24/12/15.
 */

@ParseClassName("ChatGroup")
public class ChatGroup extends ParseObject {

    private String chatText;
    private String sentDate;
    private String senderId;
    private String groupId;

    public String getChatText() {
        return getString("chatText");
    }

    public void setChatText(String chatText) {
        put("chatText", chatText);
    }

    public String getSentDate() {
        return getString("sentDate");
    }

    public void setSentDate(String sentDate) {
        put("sentDate", sentDate);
    }

    public String getGroupId() {
        return getString("groupId");
    }

    public void setGroupId(String groupId) {
        put("groupId", groupId);
    }

}
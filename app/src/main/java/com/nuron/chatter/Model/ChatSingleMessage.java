package com.nuron.chatter.Model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by nuron on 24/12/15.
 */


@ParseClassName("ChatSingleMessage")
public class ChatSingleMessage extends ParseObject {

    public static final String SENDER_ID = "senderId";
    public static final String RECEIVER_ID = "receiverId";

    private String chatText;
    private String senderId;
    private String receiverId;
    private String imageId;
    private String senderName;

    public String getSenderName() {
        return getString("senderName");
    }

    public void setSenderName(String senderName) {
        put("senderName", senderName);
    }

    public String getChatText() {
        return getString("chatText");
    }

    public void setChatText(String chatText) {
        put("chatText", chatText);
    }

    public String getSenderId() {
        return getString("senderId");
    }

    public void setSenderId(String senderId) {
        put("senderId", senderId);
    }

    public String getReceiverId() {
        return getString("receiverId");
    }

    public void setReceiverId(String receiverId) {
        put("receiverId", receiverId);
    }

    public String getImageId() {
        return getString("imageId");
    }

    public void setImageId(String imageId) {
        put("imageId", imageId);
    }

}

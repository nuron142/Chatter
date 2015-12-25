package com.nuron.chatter.Model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by nuron on 24/12/15.
 */


@ParseClassName("ChatSingle")
public class ChatSingle extends ParseObject {

    public static final String SENDER_ID = "senderId";
    public static final String RECEIVER_ID = "receiverId";

    private String chatText;
    private String sentDate;
    private String senderId;
    private String receiverId;
    private String receiverName;

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

    public String getReceiverName() {
        return getString("receiverName");
    }

    public void setReceiverName(String receiverName) {
        put("receiverName", receiverName);
    }

}

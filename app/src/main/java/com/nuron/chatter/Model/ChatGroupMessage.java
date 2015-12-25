package com.nuron.chatter.Model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by nuron on 24/12/15.
 */

@ParseClassName("ChatGroupMessage")
public class ChatGroupMessage extends ParseObject {

    public static final String SENDER_ID = "senderId";
    public static final String RECEIVER_ID = "receiverId";
    public static final String GROUP_NAME = "groupName";
    public static final String GROUP_ID = "groupId";
    public static final String UPDATED_AT = "updatedAt";

    private String imageId;
    private String chatText;
    private String senderId;
    private String senderName;
    private String groupId;
    private String groupName;


    public String getImageId() {
        return getString("imageId");
    }

    public void setImageId(String imageId) {
        put("imageId", imageId);
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

    public String getSenderName() {
        return getString("senderName");
    }

    public void setSenderName(String senderName) {
        put("senderName", senderName);
    }

    public String getGroupName() {
        return getString("groupName");
    }

    public void setGroupName(String groupName) {
        put("groupName", groupName);
    }

    public String getGroupId() {
        return getString("groupId");
    }

    public void setGroupId(String groupId) {
        put("groupId", groupId);
    }

}
package com.nuron.chatter.Model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by nuron on 26/12/15.
 */

@ParseClassName("ChatGroup")
public class ChatGroup extends ParseObject {

    public static final String GROUP_NAME = "groupName";
    public static final String GROUP_ID = "groupId";
    public static final String UPDATED_AT = "updatedAt";

    private String groupId;
    private String groupName;

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

package com.nuron.chatter.Model;

import com.parse.ParseObject;

/**
 * Created by nuron on 26/12/15.
 */
public class Friends extends ParseObject{

    public static final String GROUP_NAME = "groupName";
    public static final String GROUP_ID = "groupId";
    public static final String UPDATED_AT = "updatedAt";

    private String userId;
    private String friendId;
}

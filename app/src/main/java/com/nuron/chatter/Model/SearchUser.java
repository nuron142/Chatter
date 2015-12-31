package com.nuron.chatter.Model;

import com.parse.ParseUser;

/**
 * Created by nuron on 31/12/15.
 */

public class SearchUser {

    public static final String STRING_TRUE = "true";
    public static final String STRING_FALSE = "false";

    ParseUser parseUser;
    String isRequestSent;
    String isRequestAccepted;

    public SearchUser(ParseUser parseUser) {
        this.parseUser = parseUser;
        this.isRequestSent = STRING_FALSE;
        this.isRequestAccepted = STRING_FALSE;
    }

    public ParseUser getParseUser() {
        return parseUser;
    }

    public String getIsRequestSent() {
        return isRequestSent;
    }

    public void setIsRequestSent(String isRequestSent) {
        this.isRequestSent = isRequestSent;
    }

    public String getIsRequestAccepted() {
        return isRequestAccepted;
    }

    public void setIsRequestAccepted(String isRequestAccepted) {
        this.isRequestAccepted = isRequestAccepted;
    }

}

package com.rcslabs.click2call;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="stat_log_clients")
public final class ClientLogEntry implements Serializable {

    public static final long serialVersionUID = 1L;

    public static String PARAM_REFERRER = "ref";
    public static String PARAM_STAT_COOKIE = "sc";
    public static String PARAM_BUTTON_ID = "b";
    public static String PARAM_CALL_ID = "c";
    public static String PARAM_TIMESTAMP = "ts";
    public static String PARAM_EVENT = "e";
    public static String PARAM_DETAILS = "details";

    public ClientLogEntry(){}

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="remote_addr")
    private String remoteAddr;

    @Column(name="user_agent")
    private String userAgent;

    @Column(name="referrer")
    private String referrer;

    @Column(name="button_id")
    private String buttonId;

    @Column(name="call_id")
    private String callId;

    @Column(name="stat_cookie")
    private String statCookie;

    @Column(name="type")
    private String type;

    @Column(name="details")
    private String details;

    @Column(name="timestamp")
    private Date timestamp;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    public String getStatCookie() {
        return statCookie;
    }

    public void setStatCookie(String statCookie) {
        this.statCookie = statCookie;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

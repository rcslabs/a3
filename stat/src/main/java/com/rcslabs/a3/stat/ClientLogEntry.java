package com.rcslabs.a3.stat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="stat_client_log")
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

    @Column(name="event")
    private String event;

    @Column(name="details")
    private String details;

    @Column(name="client_date")
    private Date clientDate;

    @Column(name="server_date")
    private Date serverDate;

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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getClientDate() {
        return clientDate;
    }

    public void setClientDate(Date clientDate) {
        this.clientDate = clientDate;
    }

    public Date getServerDate() {
        return serverDate;
    }

    public void setServerDate(Date serverDate) {
        this.serverDate = serverDate;
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
}

package com.rcslabs.click2call;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="stat_consolidated_calls")
public class CallConsolidatedEntry implements Serializable {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="button_id")
    private String buttonId;

    @Column(name="call_id")
    private String callId;

    @Column(name="sip_id")
    private String sipId;

    @Column(name="a")
    private String a;

    @Column(name="b")
    private String b;

    @Column(name="start_ts")
    private Date start;

    @Column(name="started_ts")
    private Date started;

    @Column(name="failed_ts")
    private Date failed;

    @Column(name="finished_ts")
    private Date finished;

    @Column(name="details")
    private String details;

    private static Map<String, String> buttons;

    public CallConsolidatedEntry(List<CallLogEntry> callLogEntries)
    {
        for(CallLogEntry e : callLogEntries)
        {
            // try to lazy set evt properties
            if(getButtonId() == null){ this.setButtonId(e.getButtonId()); }
            if(getSipId() == null){ this.setSipId(e.getSipId()); }
            if(getCallId() == null){ this.setCallId(e.getCallId()); }
            if(getA() == null){ this.setA(e.getA()); }
            if(getB() == null){ this.setB(e.getB()); }

            if("START_CALL".equals(e.getType())){
                this.setStart(e.getTimestamp());
            }else if("CALL_STARTED".equals(e.getType())){
                this.setStarted(e.getTimestamp());
            }else if("CALL_FINISHED".equals(e.getType())){
                this.setFinished(e.getTimestamp());
            }else if("CALL_FAILED".equals(e.getType())){
                this.setFailed(e.getTimestamp());
                this.setDetails(e.getDetails());
            }
        }
    }

    public CallConsolidatedEntry(){}

    // UGLY: bad way to use relations. You should use ORM, but who cares?
    public static void setButtons(Map<String, String> buttons) {
        CallConsolidatedEntry.buttons = buttons;
    }

    public String getExplicitTitle(){
        if(buttons.containsKey(buttonId)){
            return buttons.get(buttonId);
        }else{
            return buttonId;
        }
    }

    public boolean isStarted(){
        return (null != getStarted());
    }

    public boolean isCompleted(){
        return (null != getFailed() || null != getFinished());
    }

    public String getFailedDetails(){
        if (null != getFailed()){
            return getDetails();
        }
        return null;
    }

    public int getWaitDuration(){
        if(!isStarted()) return 0;
        return (int) Math.ceil(0.001*(getStarted().getTime()-getStart().getTime()));
    }

    public int getTalkDuration(){
        if(!isStarted()) return 0;
        return (int) Math.ceil(0.001*(getFinished().getTime()-getStarted().getTime()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getSipId() {
        return sipId;
    }

    public void setSipId(String sipId) {
        this.sipId = sipId;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}

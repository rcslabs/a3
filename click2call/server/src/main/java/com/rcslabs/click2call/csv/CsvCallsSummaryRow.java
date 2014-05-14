package com.rcslabs.click2call.csv;

import com.rcslabs.click2call.entity.CallConsolidatedEntry;

public class CsvCallsSummaryRow {

    protected String dayOfMonth;
    protected int callbackForm = 0;
    protected int count = 0;
    protected float summaryWait = 0F;
    protected float meanWait = 0F;
    protected float summaryDuration = 0F;
    protected float meanDuration = 0F;

    public String getDayOfMonth(){
        return this.dayOfMonth;
    }

    public int getCount() {
        return count;
    }

    public int getSummaryDuration() {
        return (int)(Math.ceil(summaryDuration));
    }

    public int getMeanDuration() {
        return (int)(Math.ceil(meanDuration));
    }

    public int getSummaryWait() {
        return (int)(Math.ceil(summaryWait));
    }

    public int getMeanWait() {
        return (int)(Math.ceil(meanWait));
    }

    public CsvCallsSummaryRow(String dayOfMonth){
        this.dayOfMonth = dayOfMonth;
    }

    public void addCallEntry(CallConsolidatedEntry entry){
        count++;
        summaryDuration += entry.getTalkDuration();
        meanDuration = summaryDuration/count;
        summaryWait += entry.getWaitDuration();
        meanWait = summaryWait/count;
    }

    public void setCallbackFormCount(int cnt){
        this.callbackForm = cnt;
    }

    public int getCallbackFormCount(){
        return callbackForm;
    }
}

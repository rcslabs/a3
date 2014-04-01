package com.rcslabs.a3.stat;

public class CsvCallsSummaryRow {

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

    private String dayOfMonth;
    private int count = 0;
    private float summaryWait = 0F;
    private float meanWait = 0F;
    private float summaryDuration = 0F;
    private float meanDuration = 0F;

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
}

package com.rcslabs.click2call.csv;

import com.rcslabs.click2call.entity.CallConsolidatedEntry;

/**
 * Created by sx on 14.05.14.
 */
public class CsvCallsSummaryRowTotal extends CsvCallsSummaryRow {

    public CsvCallsSummaryRowTotal() {
        super("Total");
    }

    public void addCsvCallsSummaryRow(CsvCallsSummaryRow row){
        count += row.getCount();
        summaryDuration += row.getSummaryDuration();
        meanDuration += row.getMeanDuration();
        summaryWait += row.getSummaryWait();
        meanWait += row.getMeanWait();
        callbackForm += row.getCallbackFormCount();
    }

    public void addCallEntry(CallConsolidatedEntry entry){
        throw new RuntimeException("Unable to call method");
    }
}

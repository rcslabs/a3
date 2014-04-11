package com.rcslabs.a3.stat;


import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvBuilder {

    private final char separator;
    private final char nl = '\n';
    private Map<String, String> columns;
    private Map<String, Method> getters;
    private StringBuilder sb;
    private SimpleDateFormat sdf;
    private String filename;

    public CsvBuilder(){
        this(',');
    }

    public CsvBuilder(char separator){
        this.separator = separator;
        this.columns = new LinkedHashMap<String, String>();
        this.getters = new LinkedHashMap<String, Method>();
        this.sb = new StringBuilder();
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public String getFilename(){
        return this.filename;
    }

    public void setFilename(String value){
        this.filename = value;
    }

    public void addColumn(String label, String getter){
        columns.put(label, getter);
    }

    public void buildFromList(List source){
        buildFirstLine();
        if(0 == source.size()) return;
        explicitGetters(source.get(0));
        for(Object item : source){
            buildEntryLine(item);
        }
    }

    public void explicitGetters(Object item){
        Method[] methods = item.getClass().getDeclaredMethods();
        for(int i=0; i<methods.length; ++i){
            String name = methods[i].getName();
            for(String p : columns.keySet()){
                if(columns.get(p).equals(name)){
                    getters.put(p, methods[i]);
                    break;
                }
            }
        }
    }

    public void buildFirstLine(){
        for(String p : columns.keySet()){
            sb.append('"').append(p).append('"').append(separator);
        }
        // truncate last separator
        sb.setLength(sb.length()-1);
        sb.append(nl);
    }

    public void buildEntryLine(Object item){
        for(String p : columns.keySet()){
            try{
                Object value = getters.get(p).invoke(item);
                if(value instanceof Date){ value = sdf.format(value); }
                if(value == null){ value = ""; }
                sb.append('"').append(value).append('"').append(separator);
            }catch(Exception e){
                System.out.println("Invalid access " + e);
            }
        }
        // truncate last separator
        sb.setLength(sb.length()-1);
        sb.append(nl);
    }

    public String getResult(){
        return sb.toString();
    }


}




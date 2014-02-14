package com.rcslabs.rcl.telephony.entity;

public class CallParameterSipTo extends AbstractCallParameter implements ICallParameter {

    public CallParameterSipTo(String name, String value){
        super.name = name;
        super.value = value;
    }
}

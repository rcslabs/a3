package com.rcslabs.rcl.telephony.entity;

public class CallParameterSipHeader extends AbstractCallParameter implements ICallParameter
{
    public CallParameterSipHeader(String name, String value){
        super.name = name;
        super.value = value;
    }
}

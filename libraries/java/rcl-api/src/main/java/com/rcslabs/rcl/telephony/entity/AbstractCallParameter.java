package com.rcslabs.rcl.telephony.entity;

public abstract class AbstractCallParameter implements ICallParameter {

    protected String name;
    protected String value;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public ICallParameter clone() {
        ICallParameter copy = null;
        try {
            copy = (ICallParameter) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return copy;
    }
}

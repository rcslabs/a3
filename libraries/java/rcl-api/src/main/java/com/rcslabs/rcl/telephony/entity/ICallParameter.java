package com.rcslabs.rcl.telephony.entity;

public interface ICallParameter extends Cloneable {

    String getName();

    String getValue();

    ICallParameter clone();
}

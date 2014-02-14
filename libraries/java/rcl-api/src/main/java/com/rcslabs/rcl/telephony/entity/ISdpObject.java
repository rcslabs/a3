package com.rcslabs.rcl.telephony.entity;

/**
 * Created with IntelliJ IDEA.
 * User: sx
 * Date: 01.11.13
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public interface ISdpObject extends Cloneable{

    String getOfferer();

    void setOfferer(String value);

    String getAnswerer();

    void setAnswerer(String value);

    ISdpObject clone();
}

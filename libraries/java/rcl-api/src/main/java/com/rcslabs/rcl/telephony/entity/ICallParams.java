package com.rcslabs.rcl.telephony.entity;

import java.util.List;

/**
 * Parameters of a call.
 * 
 * These include:
 * <ul>
 *  <li>to - phone number of a callee</li>
 *  <li>from - user name of a caller</li>
 *  <li>callType - type of a call</li>
 * </ul>
 */
public interface ICallParams extends Cloneable {

	String getTo();

    void setTo(String value);

    void setTo(String name, String uri);

	String getFrom();

    void setFrom(String value);

    void setFrom(String name, String uri);

    CallType getCallType();

    void setCallType(CallType type);

    ISdpObject getSdpObject();

    void setSdpObject(ISdpObject value);

    void addParameter(ICallParameter param);

    List<ICallParameter> getSipXHeaders();

    List<ICallParameter> getSipToParams();

    ICallParams clone();

}
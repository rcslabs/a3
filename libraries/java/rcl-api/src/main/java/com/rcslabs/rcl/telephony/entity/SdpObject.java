package com.rcslabs.rcl.telephony.entity;

public class SdpObject implements ISdpObject {

    private String offerer;
    private String answerer;

    @Override
    public String getOfferer() {
        return offerer;
    }

    @Override
    public void setOfferer(String value) {
       offerer = value;
    }

    @Override
    public String getAnswerer() {
        return answerer;
    }

    @Override
    public void setAnswerer(String value) {
        answerer = value;
    }

    @Override
    public ISdpObject clone() {
        try {
            ISdpObject ret = (ISdpObject) super.clone();
            ret.setOfferer(getOfferer());
            ret.setAnswerer(getAnswerer());
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

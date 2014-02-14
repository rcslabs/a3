package com.rcslabs.rcl.telephony.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object, that represents ICallParams.
 * 
 */
public class CallParams implements Cloneable, ICallParams {
    private String from = "";
	private String to = "";
    private ISdpObject sdp = null;
	private CallType callType = CallType.AUDIO;
	private List<ICallParameter> params = new ArrayList<ICallParameter>();

    public CallParams() {
        super();
    }

    public CallParams(String from, String to) {
        super();
        this.from = from;
        this.to = to;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public void setTo(String value) {
        to = value;
    }

    @Override
    public void setTo(String name, String uri) {
        this.setTo("\"" + name + "\" <" + uri + ">");
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public void setFrom(String value) {
        from = value;
    }

    @Override
    public void setFrom(String name, String uri) {
        this.setFrom("\"" + name + "\" <" + uri + ">");
    }

    public CallType getCallType() {
		return callType;
	}

    @Override
    public void setCallType(CallType type) {
        callType = type;
    }

    @Override
    public ISdpObject getSdpObject() {
        return sdp;
    }

    @Override
    public void setSdpObject(ISdpObject value) {
        sdp = value;
    }

    @Override
    public void addParameter(ICallParameter param) {
        params.add(param);
    }

    @Override
	public CallParams clone() {
		try {
			CallParams ret = (CallParams) super.clone();
            ret.sdp = sdp.clone();
			ret.params = new ArrayList<ICallParameter>(params.size());
			for (ICallParameter p : params) {
				ret.params.add(p.clone());
			}
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public List<ICallParameter> getSipXHeaders() {
        List<ICallParameter> _params = new ArrayList<ICallParameter>();
        for(ICallParameter p : params){
            if(p instanceof CallParameterSipHeader) _params.add(p);
        }
        return _params;
    }

    @Override
    public List<ICallParameter> getSipToParams() {
        List<ICallParameter> _params = new ArrayList<ICallParameter>();
        for(ICallParameter p : params){
            if(p instanceof CallParameterSipTo) _params.add(p);
        }
        return _params;
    }

    @Override
	public String toString() {
		return String
				.format("CallParams [%s]", "TODO:");
	}
}

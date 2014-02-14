package com.rcslabs.rcl.telephony.media;

import com.rcslabs.rcl.core.IParametrizable;
import com.rcslabs.rcl.telephony.entity.IMediaParams;
import com.rcslabs.rcl.telephony.entity.IModifiableMediaParams;

public interface IMediaCall extends IParametrizable<IMediaParams> {

	void startIncoming(IModifiableMediaParams mediaParams);
	
	void startOutgoing(IModifiableMediaParams mediaParams);
	
	void stop();
	
	void changeIncoming(IModifiableMediaParams newParams);
	
	void changeOutgoing(IModifiableMediaParams newParams);

	IModifiableMediaParams getParamsCopy();
	
	void dtmf(String digits);
}

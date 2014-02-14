package com.rcslabs.rcl.telephony.entity;

import java.util.List;

import com.rcslabs.rcl.core.IAddressUri;


/**
 * Modifiable call params.
 *
 */
public interface IModifiableCallParams extends ICallParams {
	void setToPhoneNumber(String phoneNumber);
	void setToUserName(String userName);
	void setFromPhoneNumber(String phoneNumber);
	void setFromUserName(String userName);
	void setFromUri(IAddressUri fromUri);
	void setCallType(CallType callType);
	IModifiableMediaParams getMediaParams();
	void setMediaParams(IMediaParams params);
	
	void setToPhoneNumbersList(List<String> toPhoneNumbersList);
}

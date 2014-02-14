package ru.rcslabs.webcall.server.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class WebcallNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("sip-call-engine", new SipCallEngineBeanDefinitionParser());
	}

}

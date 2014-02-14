package ru.rcslabs.webcall.server.schema;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rcslabs.rcl.impl.JainSipGlobalParams;
import com.rcslabs.rcl.impl.JainSipRclFactory;
import com.rcslabs.rcl.telephony.media.stub.DefaultRclMediaFactory;

public class SipCallEngineBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return JainSipRclFactory.class;
	}
	
	@Override
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
		JainSipGlobalParams sipParams = new JainSipGlobalParams();
		
		NodeList childNodes = element.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element)item;
				if("server-address".equals(elem.getLocalName())) {
					sipParams.setSipServerAddress(readText(elem));
				}
				else if("proxy-address".equals(elem.getLocalName())) {
					sipParams.setSipProxyAddress(readText(elem));
				}
				//TODO: automate this via reflection
			}
		}
		
		builder.addConstructorArgValue(sipParams);
		builder.addConstructorArgValue(new DefaultRclMediaFactory()); //TODO: actual factory!
		builder.setDestroyMethodName("dispose");
	}

	private String readText(Element elem) {
		StringBuilder ret = new StringBuilder();
		NodeList childNodes = elem.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if(item.getNodeType() == Node.TEXT_NODE) {
				ret.append(item.getNodeValue().trim());
			}
		}
		
		return ret.toString();
	}
}

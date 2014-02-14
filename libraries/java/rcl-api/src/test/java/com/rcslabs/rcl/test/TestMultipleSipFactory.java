package com.rcslabs.rcl.test;

import java.lang.reflect.Constructor;

import javax.sip.SipFactory;

import junit.framework.Assert;

import org.junit.Test;

public class TestMultipleSipFactory {

	@Test
	public void test() throws Exception {
		
		SipFactory f0 = SipFactory.getInstance();
		Constructor<?> c[] = SipFactory.class.getDeclaredConstructors();
		c[0].setAccessible(true);
		Object f1 = c[0].newInstance((Object[])null);
		Object f2 = c[0].newInstance((Object[])null);
		Assert.assertNotSame(f1, f2);
		Assert.assertSame(f1.getClass(), f0.getClass());
	}

}

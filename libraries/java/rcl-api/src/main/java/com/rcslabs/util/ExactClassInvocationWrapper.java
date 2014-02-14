package com.rcslabs.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ClassUtils;

/**
 * Ensures that the arguments in this invocation are of EXACTLY the
 * same class as declared in the method signature.
 * 
 * Does not consider primitive, primitive-wrapper arguments.
 * 
 * Throws exception in case of interface-type arguments.
 * 
 * In other cases, if an argument is of a derived class, tries to copy
 * it into a new instance of a class, that is declared in the method
 * signature. The copying is made using the detected bean properties 
 * (getters-setters).
 *
 */
public class ExactClassInvocationWrapper implements MethodInvocation {
	
	private final MethodInvocation invocation;
	private Object[] args;

	public ExactClassInvocationWrapper(MethodInvocation invocation) {
		this.invocation = invocation;
		this.args = invocation.getArguments();
		
		init();
	}
	
	private void init() {
		try {
			Class<?>[] paramTypes = getMethod().getParameterTypes();

			for(int i = 0; i < args.length; i++) {
				Object arg = args[i];
				Class<?> type = paramTypes[i];
				
				if(!arg.getClass().equals(type) && !isPrimitive(type)) {
					//handle inheritance
					if(type.isInterface()) {
						throw new IllegalStateException(getClass().getSimpleName() + " cannot handle interface parameters!");
					}
					
					args[i] = copyByClass(arg, type); //set the actual argument
				}
			}
		} catch(Exception e) {
			throw new IllegalStateException("Failed to wrap invocation " + invocation, e);
		}
	}

	private Object copyByClass(Object arg, Class<?> type) throws Exception {
		Object ret = type.newInstance();
		BeanUtils.copyProperties(ret, arg);
		return ret;
	}

	private boolean isPrimitive(Class<?> type) {
		return type.isPrimitive() || 
				ClassUtils.wrapperToPrimitive(type) != null
				;
	}

	@Override
	public Object[] getArguments() {
		return args;
	}

	@Override
	public Object proceed() throws Throwable {
		return invocation.proceed();
	}

	@Override
	public Object getThis() {
		return invocation.getThis();
	}

	@Override
	public AccessibleObject getStaticPart() {
		return invocation.getStaticPart();
	}

	@Override
	public Method getMethod() {
		return invocation.getMethod();
	}

}

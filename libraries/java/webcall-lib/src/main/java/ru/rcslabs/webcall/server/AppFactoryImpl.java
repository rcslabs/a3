package ru.rcslabs.webcall.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ru.rcslabs.webcall.server.app.IAppFactory;
import ru.rcslabs.webcall.server.app.IWebcallApplication;
import ru.rcslabs.webcall.server.model.Application;

public class AppFactoryImpl implements IAppFactory, ApplicationContextAware {
	
	private Map<String, ClassPathXmlApplicationContext> contextMap = 
			new ConcurrentHashMap<String, ClassPathXmlApplicationContext>();
	
	private ApplicationContext applicationContext;
	private String contextConfigLocation = "/webcall-app.xml";
	private Properties defaultProperties;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public void setContextConfigLocationClasspath(String contextConfigLocationClasspath) {
		this.contextConfigLocation = contextConfigLocationClasspath;
	}
	
	public void setDefaultPropertiesClasspath(String defaultPropertiesClasspath) {
		defaultProperties = new Properties();
		InputStream resourceStream = getClass().getResourceAsStream(defaultPropertiesClasspath);
		try {
			defaultProperties.load(resourceStream);
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
		finally {
			try { resourceStream.close(); } catch(IOException e) {}
		}
	}

	@Override
	public ClassPathXmlApplicationContext loadAppContext(final Application app) {
		if(contextMap.containsKey(app.getName())) 
			throw new IllegalArgumentException("Application with name \"" + app.getName() + "\" already present.");
		
		//create new child application context
		ClassPathXmlApplicationContext newAppContext = new ClassPathXmlApplicationContext(
				new String[] { contextConfigLocation },
				applicationContext
		) {
			@Override
			protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
				super.prepareBeanFactory(beanFactory);
				
				//define application properties
				beanFactory.registerSingleton(
						AppFactoryImpl.this.getClass().getSimpleName() + "ApplicationConfig", 
						new ApplicationConfigPropertyPlaceholderConfigurer(app, defaultProperties)
				);
			}
		};
		
		//start context
		newAppContext.start();
		
		//save context to map
		contextMap.put(app.getName(), newAppContext);
		return newAppContext;
	}

	@Override
	public void unloadAppContext(String appName) {
		if(!contextMap.containsKey(appName)) 
			throw new IllegalArgumentException("Application with name \"" + appName + "\" not found.");
		
		contextMap.remove(appName).close();
	}
	
	@Override
	public IWebcallApplication getAppContext(String appName) {
		ClassPathXmlApplicationContext context = contextMap.get(appName);
		if(context == null) 
			throw new NoSuchElementException("Application \"" + appName + "\" not found in context map.");
		
		return context.getBean(IWebcallApplication.class);
	}

}

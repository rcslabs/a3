package ru.rcslabs.webcall.server;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import ru.rcslabs.webcall.server.model.Application;
import ru.rcslabs.webcall.server.model.ApplicationParameter;

/**
 * Override property placeholders with values from Application object.
 *
 */
public class ApplicationConfigPropertyPlaceholderConfigurer extends	PropertyPlaceholderConfigurer {
	private static Logger log = LoggerFactory.getLogger(ApplicationConfigPropertyPlaceholderConfigurer.class);
	
	private static final String APP_NAME_PROPERTY = "app.name";
	private final Application application;

	public ApplicationConfigPropertyPlaceholderConfigurer(Application application) {
		this.application = application;
	}
	
	public ApplicationConfigPropertyPlaceholderConfigurer(
			Application application, 
			Properties defaultProperties) 
	{
		this(application);
		setProperties(defaultProperties);
	}

	@Override
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactoryToProcess,
			Properties props) throws BeansException 
	{
		log.debug("Application {}: adding parameters to context...", application.getName());
		for(ApplicationParameter param : application.getParameters()) {
			log.debug("Adding parameter {}", param);
			props.put(param.getName(), param.getValue());
		}
		props.put(APP_NAME_PROPERTY, application.getName());
		super.processProperties(beanFactoryToProcess, props);
	}
}

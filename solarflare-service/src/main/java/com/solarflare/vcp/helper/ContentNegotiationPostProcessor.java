package com.solarflare.vcp.helper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;

public class ContentNegotiationPostProcessor implements BeanPostProcessor {
	private static class ContentNegotiationAvailabilityHolder {
		public static final boolean isContentNegotiationManagerAvailable = calculateCNMAvailability();

		private static boolean calculateCNMAvailability() {

			try {
				Class.forName("org.springframework.web.accept.ContentNegotiationManagerFactoryBean");
				return true;
			} catch (NoClassDefFoundError | ClassNotFoundException err) {
				return false;
			}
		}
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!ContentNegotiationAvailabilityHolder.isContentNegotiationManagerAvailable) {
			return bean;
		}

		if (bean instanceof ContentNegotiationManagerFactoryBean) {
			// prefer the HTTP request's "Accept" header over path extensions
			ContentNegotiationManagerFactoryBean cnmfb = (ContentNegotiationManagerFactoryBean) bean;
			cnmfb.setFavorPathExtension(false);
			cnmfb.setIgnoreAcceptHeader(false);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}

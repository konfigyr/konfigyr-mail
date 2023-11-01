package com.konfigyr.mail;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of the {@link org.springframework.boot.diagnostics.FailureAnalyzer} to
 * provide more meaningful error messages when the Spring Boot Application fails when one
 * of the required Konfigyr Mail implementations are missing.
 *
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@Order(Ordered.HIGHEST_PRECEDENCE)
class MailingFailureAnalyzer extends AbstractFailureAnalyzer<NoSuchBeanDefinitionException> {

	static List<String> MAILER_IMPLEMENTATIONS = List.of("konfigyr-mail-smtp");
	static List<String> TEMPLATE_IMPLEMENTATIONS = List.of("konfigyr-mail-thymeleaf");

	@Override
	protected FailureAnalysis analyze(Throwable root, NoSuchBeanDefinitionException cause) {
		final ResolvableType type = cause.getResolvableType();

		if (type == null) {
			return null;
		}

		if (type.isAssignableFrom(Mailer.class)) {
			return new FailureAnalysis(generateDescription(type), generateAction(type, MAILER_IMPLEMENTATIONS), root);
		}

		if (type.isAssignableFrom(TemplateEngine.class)) {
			return new FailureAnalysis(generateDescription(type), generateAction(type, TEMPLATE_IMPLEMENTATIONS), root);
		}

		return null;
	}

	static String generateDescription(ResolvableType type) {
		return "It seems you attempted to use the Konfigyr Mail library without having the " + extractBeanTypeName(type)
				+ " Bean present in your Spring Context.";
	}

	static String generateAction(ResolvableType type, Iterable<String> libraries) {
		final StringBuilder builder = new StringBuilder(
				"Consider adding one of the following libraries to your project:\n\n");

		libraries.forEach(library -> builder.append("\t* ").append("com.konfigyr:").append(library).append("\n"));

		return builder.append("\nYou can also create your own implementation of the ")
			.append(extractBeanTypeName(type))
			.append(" and register it as a Spring Bean in your application.")
			.toString();
	}

	static String extractBeanTypeName(@NonNull ResolvableType resolvableType) {
		return Objects.requireNonNull(resolvableType.getRawClass()).getTypeName();
	}

}

package com.konfigyr.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Autoconfiguration class that would register the {@link Mailer} that is using Spring
 * {@link org.springframework.mail.javamail.JavaMailSender} to send {@link Mail mails}.
 *
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@AutoConfiguration
@ConditionalOnBean(JavaMailSender.class)
@ConditionalOnMissingBean(Mailer.class)
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
public class JavaMailerAutoConfiguration {

	static final String SENDER_PROPERTY = "spring.mail.sender";

	@Bean
	Mailer javaMailer(JavaMailSender sender, MessageSource messageSource, TemplateEngine templateEngine,
			ObjectProvider<Preperator<MimeMessageHelper>> preperators) {
		return new JavaMailer(sender, messageSource, templateEngine, preperators);
	}

	@Bean
	@ConditionalOnProperty(prefix = SENDER_PROPERTY, name = "email")
	Preperator<MimeMessageHelper> defaultSenderPreperator(Environment environment) {
		return JavaMailer.sender(environment.getProperty(SENDER_PROPERTY + ".email"),
				environment.getProperty(SENDER_PROPERTY + ".name"));
	}

}

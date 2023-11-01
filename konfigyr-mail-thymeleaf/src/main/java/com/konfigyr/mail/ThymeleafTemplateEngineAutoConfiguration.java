package com.konfigyr.mail;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.ITemplateEngine;

/**
 * Autoconfiguration class that would register the {@link TemplateEngine} that is using
 * {@link ITemplateEngine} to generate {@link Template mail templates}.
 *
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@AutoConfiguration
@ConditionalOnBean(ITemplateEngine.class)
@ConditionalOnMissingBean(TemplateEngine.class)
@AutoConfigureAfter(ThymeleafAutoConfiguration.class)
public class ThymeleafTemplateEngineAutoConfiguration {

	@Bean
	TemplateEngine thymeleafMailTemplateEngine(ITemplateEngine thymeleaf) {
		return new ThymeleafTemplateEngine(thymeleaf);
	}

}

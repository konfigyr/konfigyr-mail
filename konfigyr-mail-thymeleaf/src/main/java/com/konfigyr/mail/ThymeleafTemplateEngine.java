package com.konfigyr.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

/**
 * Implementation of the {@link TemplateEngine} that uses the Thymeleaf {@link ITemplateEngine}
 * to retrieve and process mail templates.
 *
 * @see ITemplateEngine
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@RequiredArgsConstructor
public class ThymeleafTemplateEngine implements TemplateEngine {

	private final ITemplateEngine thymeleaf;

	@NonNull
	@Override
	public Template render(@NonNull Mail mail) {
		final IContext context = new Context(mail.locale(), mail.attributes());
		final String contents = thymeleaf.process(mail.template(), context);

		return Template.html(contents);
	}

}

package com.konfigyr.mail;

import org.jspecify.annotations.NonNull;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

/**
 * Implementation of the {@link TemplateEngine} that uses the Thymeleaf
 * {@link ITemplateEngine} to retrieve and process mail templates.
 *
 * @see ITemplateEngine
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
public class ThymeleafTemplateEngine implements TemplateEngine {

	private final ITemplateEngine thymeleaf;

	/**
	 * Creates a new {@link ThymeleafTemplateEngine} that delegates template rendering
	 * to the given Thymeleaf {@link ITemplateEngine}.
	 * @param thymeleaf Thymeleaf template engine to use, can't be {@literal null}
	 */
	public ThymeleafTemplateEngine(ITemplateEngine thymeleaf) {
		this.thymeleaf = thymeleaf;
	}

	@NonNull
	@Override
	public Template render(@NonNull Mail mail) {
		final IContext context = new Context(mail.locale(), mail.attributes());
		final String contents = thymeleaf.process(mail.template(), context);

		return Template.html(contents);
	}

}

package com.konfigyr.mail;

import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * Interface that defines how Mail Template Engines should render the {@link Mail} before
 * it's being sent by the {@link Mailer}.
 *
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
public interface TemplateEngine {

	/**
	 * Process the specified {@link Mail} that carries the template name and attributes,
	 * or variables that will be available for the execution of expressions inside the
	 * template.
	 * @param mail mail to be rendered, never {@literal null}
	 * @return rendered mail template
	 * @throws IOException when there is an error during the rendering process.
	 */
	@NonNull
	Template render(@NonNull Mail mail) throws IOException;

}

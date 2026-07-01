package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;

/**
 * Interface that defines how Mail Template Engines should render the {@link Mail} before
 * it's being sent by the {@link Mailer}.
 * <p>
 * Implementations must be thread-safe; they are registered as singleton Spring beans and
 * may be invoked concurrently. When the requested template cannot be found, implementations
 * should throw an {@link IOException} or an appropriate unchecked exception rather than
 * returning an empty or {@literal null} result.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
public interface TemplateEngine {

	/**
	 * Process the specified {@link Mail} that carries the template name and attributes,
	 * or variables that will be available for the execution of expressions inside the
	 * template.
	 *
	 * @param mail mail to be rendered, never {@literal null}
	 * @return the fully rendered {@link Template} containing the message body and the
	 * content type determined by the engine; never {@literal null}
	 * @throws IOException if the template cannot be located, cannot be read from its
	 * source, or fails to render, for example due to an invalid expression or a missing
	 * required variable
	 */
	Template render(Mail mail) throws IOException;

}

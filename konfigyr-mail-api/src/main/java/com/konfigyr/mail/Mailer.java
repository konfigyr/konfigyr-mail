package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;

/**
 * Interface that defines how a {@link Mail} should be sent.
 * <p>
 * Implementations are expected to be thread-safe; the library's autoconfigured SMTP
 * implementation is stateless and safe for concurrent use.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
public interface Mailer {

	/**
	 * Sends out a {@link Mail}.
	 *
	 * @param mail mail message to be sent, can't be {@literal null}
	 */
	void send(Mail mail);

}

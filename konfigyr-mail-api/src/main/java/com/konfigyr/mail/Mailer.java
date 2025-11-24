package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;

/**
 * Interface that defines how a {@link Mail} should be sent.
 *
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@NullMarked
public interface Mailer {

	/**
	 * Sends out a {@link Mail}.
	 * @param mail mail message to be sent, can't be {@literal null}
	 */
	void send(Mail mail);

}

package com.konfigyr.mail;

import org.springframework.lang.NonNull;

/**
 * Interface that defines how a {@link Mail} should be sent.
 *
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
public interface Mailer {

	/**
	 * Sends out a {@link Mail}.
	 * @param mail mail message to be sent, can not be {@literal null}
	 */
	void send(@NonNull Mail mail);

}

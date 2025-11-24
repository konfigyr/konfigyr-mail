package com.konfigyr.mail;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.jspecify.annotations.NonNull;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;

/**
 * An immutable Mail subject object that can be used with
 * {@link org.springframework.context.MessageSource} to lookup a translated subject value
 * depending on the specified {@link java.util.Locale language}.
 * <p>
 * You can additionally specify arguments for your subject message that would be formatted
 * by {@link org.springframework.context.MessageSource}.
 *
 * @param value subject value
 * @param arguments formatting arguments
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
public record Subject(String value, Object... arguments) implements Serializable {

	@Serial
	private static final long serialVersionUID = -3569169986716025513L;

	public Subject {
		Assert.hasText(value, "Subject value can not be blank");
	}

	/**
	 * Returns the {@link MessageSourceResolvable}, with the subject value as the unique
	 * code, that can be used to perform a lookup and formatting of the message. When no
	 * such message is found, the raw subject value would be used as the actual Mail
	 * subject.
	 * @return message source resolvable for this subject, never {@literal null}
	 */
	@NonNull
	public MessageSourceResolvable toResolvable() {
		return new DefaultMessageSourceResolvable(new String[] { value }, arguments, value);
	}

}

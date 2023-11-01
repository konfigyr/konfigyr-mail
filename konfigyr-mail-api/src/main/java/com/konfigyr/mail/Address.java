package com.konfigyr.mail;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;

/**
 * An immutable address object, with an email address and a display name.
 *
 * @param email email address, can not be {@literal blank}
 * @param name display name, can be {@literal null}
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
public record Address(@NonNull String email, @Nullable String name) implements Serializable {

	@Serial
	private static final long serialVersionUID = 722890289582271673L;

	/**
	 * Creates a new address with just an email address without a display name.
	 * @param email email address
	 * @throws IllegalArgumentException when email is blank
	 */
	public Address(String email) {
		this(email, null);
	}

	public Address {
		Assert.hasText(email, "Address needs to have an email address set");
	}

}

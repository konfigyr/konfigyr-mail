package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;

/**
 * An immutable address object, with an email address and a display name.
 *
 * @param email email address, can't be {@literal blank}
 * @param name display name, can be {@literal null}
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@NullMarked
public record Address(String email, @Nullable String name) implements Serializable {

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

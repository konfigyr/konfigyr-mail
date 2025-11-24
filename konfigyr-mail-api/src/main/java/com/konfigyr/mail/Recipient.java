package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * An immutable Mail recipient object, with an {@link Address} and recipient type (eg
 * {@link Type#CC}).
 *
 * @param address recipient address, can't be {@literal null}
 * @param type recipient type, can't be {@literal null}
 * @see Address
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@NullMarked
public record Recipient(Address address, Type type) implements Serializable {

	@Serial
	private static final long serialVersionUID = 5377193327396172878L;

	/**
	 * Creates a new {@link Recipient} with {@link Type#TO} for an email address.
	 * @param email email address of the recipient
	 * @return recipient with just an email address
	 * @throws IllegalArgumentException when email is blank
	 */
	public static Recipient to(String email) {
		return to(email, null);
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#TO} for an email address and
	 * display name.
	 * @param email email address of the recipient
	 * @param name display name of the recipient
	 * @return recipient with an email address and display name
	 * @throws IllegalArgumentException when email is blank
	 */
	public static Recipient to(String email, @Nullable String name) {
		return to(new Address(email, name));
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#TO} for an {@link Address}.
	 * @param address address of the recipient, can't be {@literal null}
	 * @return recipient with an address
	 */
	public static Recipient to(Address address) {
		return new Recipient(address, Type.TO);
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#CC} for an email address.
	 * @param email email address of the recipient
	 * @return recipient with just an email address
	 * @throws IllegalArgumentException when email is blank
	 */
	public static Recipient cc(String email) {
		return cc(email, null);
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#CC} for an email address and
	 * display name.
	 * @param email email address of the recipient
	 * @param name display name of the recipient
	 * @return recipient with an email address and display name
	 * @throws IllegalArgumentException when email is blank
	 */
	public static Recipient cc(String email, @Nullable String name) {
		return cc(new Address(email, name));
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#CC} for an {@link Address}.
	 * @param address address of the recipient, can't be {@literal null}
	 * @return recipient with an address
	 */
	public static Recipient cc(Address address) {
		return new Recipient(address, Type.CC);
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#BCC} for an email address.
	 * @param email email address of the recipient
	 * @return recipient with just an email address
	 * @throws IllegalArgumentException when email is blank
	 */
	public static Recipient bcc(String email) {
		return bcc(email, null);
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#BCC} for an email address and
	 * display name.
	 * @param email email address of the recipient
	 * @param name display name of the recipient
	 * @return recipient with an email address and display name
	 * @throws IllegalArgumentException when email is blank
	 */
	public static Recipient bcc(String email, @Nullable String name) {
		return bcc(new Address(email, name));
	}

	/**
	 * Creates a new {@link Recipient} with {@link Type#BCC} for an {@link Address}.
	 * @param address address of the recipient, can't be {@literal null}
	 * @return recipient with an address
	 */
	public static Recipient bcc(Address address) {
		return new Recipient(address, Type.BCC);
	}

	/**
	 * Defines the types of recipients allowed by the Mail API.
	 */
	enum Type {

		/**
		 * The "To" (primary) recipients.
		 */
		TO,
		/**
		 * The "Cc" (carbon copy) recipients.
		 */
		CC,
		/**
		 * The "Bcc" (blind carbon copy) recipients.
		 */
		BCC

	}

}

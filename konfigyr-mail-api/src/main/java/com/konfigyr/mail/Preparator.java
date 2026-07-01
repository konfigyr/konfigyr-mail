package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Functional interface that can be used to prepare and configure the actual Mail message
 * that is going to be sent via {@link Mailer}.
 *
 * @param <T> generic type of the mailing target that is being prepared
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
@FunctionalInterface
public interface Preparator<T> {

	/**
	 * Prepare the {@link Mailer} target using the configured {@link Mail}. Usually the
	 * implementation is simply copying and mapping the values from the Konfigyr Mail API
	 * to the actual mail transport, be it SMTP, HTTP or any other.
	 * @param mail mail to be sent, can't be {@literal null}
	 * @param target target object to be prepared, can't be {@literal null}
	 * @return the prepared target, never {@literal null}
	 * @throws Exception when there is an error during preparation
	 */
	T prepare(Mail mail, T target) throws Exception;

	/**
	 * Returns a composed {@link Preparator} that first applies this {@link Preparator}
	 * and then applies the after function to the result.
	 * <p>
	 * If the execution of either {@link Preparator preparators} throws an exception, it
	 * is relayed to the caller of the composed {@link Preparator}.
	 * @param next the preparator to apply after this one
	 * @return a composed {@link Preparator}
	 */
	default Preparator<T> and(Preparator<T> next) {
		return (Mail mail, T t) -> next.prepare(mail, prepare(mail, t));
	}

	/**
	 * Returns a {@link Preparator} that always returns its target.
	 * @param <T> preparator target type
	 * @return the no-operation {@link Preparator}
	 */
	static <T> Preparator<T> noop() {
		return (mail, t) -> t;
	}

	/**
	 * Aggregates all the {@link Preparator preparators} in a single chained
	 * {@link Preparator} instance.
	 * @param <T> preparator target type
	 * @param preparators preparators to be reduced
	 * @return single chained {@link Preparator} instance.
	 */
	static <T> Preparator<T> aggregate(@Nullable Iterable<Preparator<T>> preparators) {
		if (preparators == null) {
			return Preparator.noop();
		}

		return aggregate(StreamSupport.stream(preparators.spliterator(), false));
	}

	/**
	 * Aggregates all the {@link Preparator preparators} in a single chained
	 * {@link Preparator} instance.
	 * @param <T> preparator target type
	 * @param preparators preparators to be reduced
	 * @return single chained {@link Preparator} instance.
	 */
	static <T> Preparator<T> aggregate(@Nullable Stream<Preparator<T>> preparators) {
		if (preparators == null) {
			return Preparator.noop();
		}

		return preparators.reduce(Preparator::and).orElseGet(Preparator::noop);
	}

}

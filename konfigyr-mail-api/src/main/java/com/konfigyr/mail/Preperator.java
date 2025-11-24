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
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@NullMarked
@FunctionalInterface
public interface Preperator<T> {

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
	 * Returns a composed {@link Preperator} that first applies this {@link Preperator}
	 * and then applies the after function to the result.
	 * <p>
	 * If the execution of either {@link Preperator preperators} throws an exception, it
	 * is relayed to the caller of the composed {@link Preperator}.
	 * @param next the preperator to apply after this one
	 * @return a composed {@link Preperator}
	 */
	default Preperator<T> and(Preperator<T> next) {
		return (Mail mail, T t) -> next.prepare(mail, prepare(mail, t));
	}

	/**
	 * Returns a {@link Preperator} that always returns its target.
	 * @param <T> preperator target type
	 * @return the no-operation {@link Preperator}
	 */
	static <T> Preperator<T> noop() {
		return (mail, t) -> t;
	}

	/**
	 * Aggregates all the {@link Preperator preparators} in a single chained
	 * {@link Preperator} instance.
	 * @param <T> preperator target type
	 * @param preperators preparators to be reduced
	 * @return single chained {@link Preperator} instance.
	 */
	static <T> Preperator<T> aggregate(@Nullable Iterable<Preperator<T>> preperators) {
		if (preperators == null) {
			return Preperator.noop();
		}

		return aggregate(StreamSupport.stream(preperators.spliterator(), false));
	}

	/**
	 * Aggregates all the {@link Preperator preparators} in a single chained
	 * {@link Preperator} instance.
	 * @param <T> preperator target type
	 * @param preperators preparators to be reduced
	 * @return single chained {@link Preperator} instance.
	 */
	static <T> Preperator<T> aggregate(@Nullable Stream<Preperator<T>> preperators) {
		if (preperators == null) {
			return Preperator.noop();
		}

		return preperators.reduce(Preperator::and).orElseGet(Preperator::noop);
	}

}

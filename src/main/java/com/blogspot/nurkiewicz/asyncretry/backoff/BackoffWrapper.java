package com.blogspot.nurkiewicz.asyncretry.backoff;

import java.util.Objects;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 11:16 PM
 */
public abstract class BackoffWrapper implements Backoff {

	protected final Backoff target;

	public BackoffWrapper(Backoff target) {
		this.target = Objects.requireNonNull(target);
	}
}

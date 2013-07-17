package com.blogspot.nurkiewicz.asyncretry.policy;

import java.util.Objects;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:22 PM
 */
abstract public class RetryPolicyWrapper implements RetryPolicy {

	protected final RetryPolicy target;

	protected RetryPolicyWrapper(RetryPolicy target) {
		this.target = Objects.requireNonNull(target);
	}

}

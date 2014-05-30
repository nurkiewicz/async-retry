package com.nurkiewicz.asyncretry;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 7:09 PM
 */
public interface FaultyService {

	int alwaysSucceeds();

	String sometimesFails();

	BigDecimal calculateSum(int retry);

	void withFlag(boolean flag);

	CompletableFuture<String> safeAsync();

	CompletableFuture<String> alwaysFailsAsync();

}

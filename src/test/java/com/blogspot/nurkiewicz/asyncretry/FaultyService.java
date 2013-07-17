package com.blogspot.nurkiewicz.asyncretry;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 7:09 PM
 */
public interface FaultyService {

	int alwaysSucceeds();
	String sometimesFails();

}

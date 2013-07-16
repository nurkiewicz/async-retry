package com.blogspot.nurkiewicz.asyncretry;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;

/**
 * @author Tomasz Nurkiewicz
 * @since 5/10/13, 9:56 PM
 */
public class AbstractBaseTestCase {

	@BeforeMethod(alwaysRun=true)
	public void injectMocks() {
		MockitoAnnotations.initMocks(this);
	}

}

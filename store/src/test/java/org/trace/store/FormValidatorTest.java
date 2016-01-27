package org.trace.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.trace.store.middleware.drivers.utils.FormFieldValidator;

public class FormValidatorTest {

	private final String[] testValidUsernames = {
			"common",
			"slingsymptoms",
			"whimbrelcommon",
			"porcelaincrib",
			"avowcollect",
			"fishycascaded",
			"golf_is_sad",
			"nonagontawny",
			"transitevident",
			"pilekuwaiti",
			"balmabrasive",
			"omicronmatch",
			"disks_fit",
			"wifeoctave",
			"landquintic",
			"awfulpogostick",
			"towh333vacuate",
			"havingwreck",
			"leverheels",
			"bosonplenty",
			"totteringtear"
	};

	private final String[] testInvalidUsernames = {
			"bob",
			"evil",
			"bob2",
			"c!drulestaking",
			"abscissafurrowed",
			"chloridescassiopeia",
			"sousaphonechanged",
			"consultantsavannah",
			"totte#ingtear",
			"a",
			"!"
	};

	@Test
	public void testUsernameValidation(){
		int i;
		for(i=0; i<testValidUsernames.length; i++)
			assertTrue(FormFieldValidator.isValidUsername(testValidUsernames[i]));
		
		for(i=0; i<testInvalidUsernames.length; i++)
			assertFalse(FormFieldValidator.isValidUsername(testInvalidUsernames[i]));
	}


}

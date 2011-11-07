/* *********************************************************************** *
 * project: org.matsim.*
 * PlanomatConfigGroupTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.config.groups;

import org.apache.log4j.Logger;
import org.matsim.core.config.ConfigUtils;
import org.matsim.testcases.MatsimTestCase;

public class VspExperimentalConfigGroupTest extends MatsimTestCase {

	private static final Logger log = Logger.getLogger(VspExperimentalConfigGroupTest.class);

	public void testVspConfigGroup() {
		
		VspExperimentalConfigGroup vspConfig = ConfigUtils.createConfig().vspExperimental() ;
			
		vspConfig.setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.WARN) ;
		// this should (just) produce warning messages:
		vspConfig.checkConsistency() ;
		
		vspConfig.setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.ABORT) ;
		try {
			// should throw RuntimeException:
			vspConfig.checkConsistency() ;
			fail("should never get here since it should have thrown an exception before") ;
		} catch ( RuntimeException e ) {
			log.info("Caught RuntimeException, as expected: " + e.getMessage());
		}
	}

}

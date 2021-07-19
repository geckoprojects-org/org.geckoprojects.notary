/**
 * Copyright (c) 2012 - 2019 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.itest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author mark
 * @since 24.09.2019
 */
public class TestDateFormatter {

	@Test
	public void test() throws ParseException {
		String dateString = "2019-09-24T08:24:37.325Z";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date d = sdf.parse(dateString);
		System.err.println(d.toString());
		dateString = "2019-09-24T08:24:37.325Z";
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		d = sdf.parse(dateString);
		System.err.println(d.toString());
	}
	
}

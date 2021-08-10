/**
 * Copyright (c) 2012 - 2021 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.merit.service.itest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.gecko.notary.merit.model.merit.Badge;
import org.gecko.notary.merit.model.merit.MeritFactory;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * This is your example OSGi integration test class
 * @since 1.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class MeritAssetTransactionEntryTextProviderTest {
	
	@Test
	public void testMeritAssetTETextProvider_NullObject(@InjectService(filter = "(&(object=TransactionEntry)(target=Asset))") TextProvider textProvider) {
		assertNotNull(textProvider);
		assertNull(textProvider.provideText(null, null));
	}
	
	@Test
	public void testMeritAssetTETextProvider_Object(@InjectService(filter = "(&(object=TransactionEntry)(target=Asset))") TextProvider textProvider) {
		assertNotNull(textProvider);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		assertNull(textProvider.provideText(b, null));
	}
	
}

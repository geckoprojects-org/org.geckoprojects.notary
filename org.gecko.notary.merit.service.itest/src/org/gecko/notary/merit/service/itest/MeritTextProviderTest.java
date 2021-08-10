/**
 * Copyright (c) 2012 - 2021 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.merit.service.itest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gecko.notary.merit.model.merit.Badge;
import org.gecko.notary.merit.model.merit.MeritFactory;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetInfo;
import org.gecko.notary.model.notary.NotaryFactory;
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
public class MeritTextProviderTest {
	
	@Test
	public void testMeritTextProvider_NullObject(@InjectService(filter = "(&(object=Asset)(target=Asset)(domain=Merit))") TextProvider textProvider) {
		assertNotNull(textProvider);
		assertNull(textProvider.provideText(null, null));
	}
	
	@Test
	public void testMeritTextProvider_NoBadge(@InjectService(filter = "(&(object=Asset)(target=Asset)(domain=Merit))") TextProvider textProvider) {
		assertNotNull(textProvider);
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		assertNull(textProvider.provideText(a, null));
	}
	
	@Test
	public void testMeritTextProvider_BadgeInfoNotNull(@InjectService(filter = "(&(object=Asset)(target=Asset)(domain=Merit))") TextProvider textProvider) {
		assertNotNull(textProvider);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		AssetInfo info = NotaryFactory.eINSTANCE.createAssetInfo();
		b.setInfo(info);
		assertNotNull(b.getInfo());
		assertNull(textProvider.provideText(b, null));
	}
	
	@Test
	public void testMeritTextProvider_BadgeInfoNoOwner(@InjectService(filter = "(&(object=Asset)(target=Asset)(domain=Merit))") TextProvider textProvider) {
		assertNotNull(textProvider);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		assertNull(b.getInfo());
		String text = textProvider.provideText(b, null);
		assertNotNull(text);
		assertTrue(text.contains("Badge of: <n/a>"));
		assertTrue(b.getInfo().getLabel().contains("<n/a>"));
		assertFalse(b.getInfo().getLabel().contains("TEST"));
		assertTrue(b.getInfo().getDescription().contains("<n/a>"));
		assertTrue(b.getInfo().getDescription().contains("amount: 0"));
		assertFalse(b.getInfo().getDescription().contains("TEST"));
		assertNull(b.getInfo().getAssetIdentifier());
		assertNotNull(b.getInfo());
	}
	
	@Test
	public void testMeritTextProvider_BadgeInfoOwner(@InjectService(filter = "(&(object=Asset)(target=Asset)(domain=Merit))") TextProvider textProvider) {
		assertNotNull(textProvider);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setOwnerId("TEST");
		assertNull(b.getInfo());
		String text = textProvider.provideText(b, null);
		assertNotNull(text);
		assertTrue(text.contains("Badge of: TEST"));
		assertTrue(b.getInfo().getLabel().contains("TEST"));
		assertFalse(b.getInfo().getLabel().contains("<n/a>"));
		assertTrue(b.getInfo().getDescription().contains("TEST"));
		assertFalse(b.getInfo().getDescription().contains("<n/a>"));
		assertTrue(b.getInfo().getDescription().contains("amount: 0"));
		assertNull(b.getInfo().getAssetIdentifier());
		assertNotNull(b.getInfo());
	}
	
	@Test
	public void testMeritTextProvider_BadgeInfoBadgeId(@InjectService(filter = "(&(object=Asset)(target=Asset)(domain=Merit))") TextProvider textProvider) {
		assertNotNull(textProvider);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setOwnerId("TEST");
		b.setId("ONE");
		assertNull(b.getInfo());
		String text = textProvider.provideText(b, null);
		assertNotNull(text);
		assertTrue(text.contains("Badge of: TEST"));
		assertTrue(b.getInfo().getLabel().contains("TEST"));
		assertFalse(b.getInfo().getLabel().contains("<n/a>"));
		assertTrue(b.getInfo().getDescription().contains("TEST"));
		assertFalse(b.getInfo().getDescription().contains("<n/a>"));
		assertTrue(b.getInfo().getDescription().contains("amount: 0"));
		assertEquals("ONE", b.getInfo().getAssetIdentifier());
		assertNotNull(b.getInfo());
	}
	
	@Test
	public void testMeritTextProvider_BadgeInfoAmount(@InjectService(filter = "(&(object=Asset)(target=Asset)(domain=Merit))") TextProvider textProvider) {
		assertNotNull(textProvider);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setOwnerId("TEST");
		b.setId("ONE");
		b.setMeritPoints(12);
		assertNull(b.getInfo());
		String text = textProvider.provideText(b, null);
		assertNotNull(text);
		assertTrue(text.contains("Badge of: TEST"));
		assertTrue(b.getInfo().getLabel().contains("TEST"));
		assertFalse(b.getInfo().getLabel().contains("<n/a>"));
		assertTrue(b.getInfo().getDescription().contains("TEST"));
		assertFalse(b.getInfo().getDescription().contains("<n/a>"));
		assertTrue(b.getInfo().getDescription().contains("amount: 12"));
		assertEquals("ONE", b.getInfo().getAssetIdentifier());
		assertNotNull(b.getInfo());
	}
	
}

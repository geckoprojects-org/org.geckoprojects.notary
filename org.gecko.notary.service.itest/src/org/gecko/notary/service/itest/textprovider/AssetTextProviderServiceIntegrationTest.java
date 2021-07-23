/**
 * Copyright (c) 2012 - 2018 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.itest.textprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Dictionary;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetInfo;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * <p>
 * This is an integration test for the context service
 * </p>
 * 
 * @since 1.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class AssetTextProviderServiceIntegrationTest {
	
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		Dictionary<String, Object> participantProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		bc.registerService(ParticipantService.class, mock(ParticipantService.class), participantProperties);
	}

	/**
	 * Here you can put your test
	 * @throws InvalidSyntaxException 
	 */
	@Test
	public void testSimple_Null(@InjectService(filter = "(&(object=Asset)(target=Asset))") TextProvider textProvider) {
		assertNull(textProvider.provideText(null, null));
	}
	
	@Test
	public void testSimple_WrongEntry(@InjectService(filter = "(&(object=Asset)(target=Asset))") TextProvider textProvider) {
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		assertNull(textProvider.provideText(t, null));
	}
	
	@Test
	public void testExistingInfo(@InjectService(filter = "(&(object=Asset)(target=Asset))") TextProvider textProvider) {
		Asset p = NotaryFactory.eINSTANCE.createAsset();
		p.setId("test");
		AssetInfo info = NotaryFactory.eINSTANCE.createAssetInfo();
		p.setInfo(info);
		Asset pCheck = EcoreUtil.copy(p);
		textProvider.provideText(p, null);
		assertTrue(EcoreUtil.equals(pCheck, p));
	}
	
	@Test
	public void testSimple_Entry(@InjectService(filter = "(&(object=Asset)(target=Asset))") TextProvider textProvider) {
		Asset p = NotaryFactory.eINSTANCE.createAsset();
		p.setId("test");
		assertNotNull(textProvider.provideText(p, null));
		assertNotNull(p.getInfo());
	}
	
	@Test
	public void testAsset(@InjectService(filter = "(&(object=Asset)(target=Asset))") TextProvider textProvider,
			@InjectService ParticipantService participantService) {
		
		Asset p = NotaryFactory.eINSTANCE.createAsset();
		p.setId("test");
		assertNotNull(textProvider.provideText(p, null));
		assertNotNull(p.getInfo());
		AssetInfo info = p.getInfo();
		assertEquals(String.format(TextProvider.ASSET_LABEL_TEMPLATE, "test", "<n/a>", "<n/a>"), info.getLabel());
		assertEquals(String.format(TextProvider.ASSET_DESCRIPTION_TEMPLATE, "test", "<n/a>", "<n/a>"), info.getDescription());
		assertEquals("test", info.getAssetIdentifier());
		assertEquals("Asset", info.getAssetTypeLabel());
		
		p = NotaryFactory.eINSTANCE.createAsset();
		assertNotNull(textProvider.provideText(p, null));
		assertNotNull(p.getInfo());
		info = p.getInfo();
		assertEquals(String.format(TextProvider.ASSET_LABEL_TEMPLATE, "<n/a>", "<n/a>", "<n/a>"), info.getLabel());
		assertEquals(String.format(TextProvider.ASSET_DESCRIPTION_TEMPLATE, "<n/a>", "<n/a>", "<n/a>"), info.getDescription());
		assertEquals("<n/a>", info.getAssetIdentifier());
		assertEquals("Asset", info.getAssetTypeLabel());

		Participant c = NotaryFactory.eINSTANCE.createParticipant();
		c.setId("creator");
		c.setDescription("CRATER");
		Participant o = NotaryFactory.eINSTANCE.createParticipant();
		o.setId("owner");
		o.setDescription("OWEN");
		Mockito.when(participantService.getParticipant("creator")).thenReturn(c);
		Mockito.when(participantService.getParticipant("owner")).thenReturn(o);
		p = NotaryFactory.eINSTANCE.createAsset();
		p.setId("test");
		p.setCreatorId("creator");
		p.setOwnerId("owner");
		assertNotNull(textProvider.provideText(p, null));
		assertNotNull(p.getInfo());
		info = p.getInfo();
		
		assertEquals(String.format(TextProvider.ASSET_LABEL_TEMPLATE, "test", "creator", "owner"), info.getLabel());
		assertEquals(String.format(TextProvider.ASSET_DESCRIPTION_TEMPLATE, "test").replace("${CREATOR}", "CRATER").replace("${OWNER}", "OWEN"), info.getDescription());
		assertEquals("test", info.getAssetIdentifier());
		assertEquals("Asset", info.getAssetTypeLabel());
	}
	
}

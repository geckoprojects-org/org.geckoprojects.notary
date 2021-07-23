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
import static org.mockito.Mockito.mock;

import java.util.Dictionary;

import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
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
public class EntryTextProviderServiceIntegrationTest {
	
	/**
	 * Setup the services and maybe mock the services.
	 * The registered services are unregistered after the test
	 * @param bc the {@link BundleContext} that closes after each test
	 */
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		TransactionService transactionService = mock(TransactionService.class);
		Dictionary<String, Object> transactionProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		bc.registerService(TransactionService.class, transactionService, transactionProperties);
		
		ParticipantService participantService = mock(ParticipantService.class);
		Dictionary<String, Object> participantProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		bc.registerService(ParticipantService.class, participantService, participantProperties);
	}

	/**
	 * Here you can put your test
	 * @throws InvalidSyntaxException 
	 */
	@Test
	public void testSimple_Null(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider) {
		assertNull(textProvider.provideText(null, null));
	}
	
	@Test
	public void test_NoTransaction(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(null);
		assertNull(textProvider.provideText(ate, null));
	}
	
	@Test
	public void test_ValidTransaction(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("test");
		t.setDescription("Test Transaction");
		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(t);
		assertNull(textProvider.provideText(ate, null));
		assertEquals(t.getDescription(), ate.getLabel());
	}
	
	@Test
	public void test_NoParticipant(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("test");
		t.setDescription("Test Transaction");
		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(t);
		Mockito.when(participantService.getParticipant(Mockito.any(String.class))).thenReturn(null);
		
		assertNull(textProvider.provideText(ate, null));
		assertEquals(t.getDescription(), ate.getLabel());
		assertNull(ate.getSource());
	}
	
	@Test
	public void test_ValidParticipant(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("test");
		t.setDescription("Test Transaction");
		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(t);
		
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setName("t");
		Mockito.when(participantService.getParticipant(Mockito.any())).thenReturn(p);
		
		assertNull(textProvider.provideText(ate, null));
		
		assertEquals(t.getDescription(), ate.getLabel());
		assertEquals(p.getName(), ate.getSource());
		
		p.setDescription("Tester");
		assertNull(textProvider.provideText(ate, null));
		
		assertEquals(t.getDescription(), ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		
	}
	
	@Test
	public void test_CreationComment(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		assertNull(ate.getComment());
		
		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(null);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setName("t");
		p.setDescription("Tester");
		Mockito.when(participantService.getParticipant(Mockito.any())).thenReturn(p);
		
		ate.setChangeType(AssetChangeType.CREATION);
		ate.setParentAssetId("P01");
		ate.setAssetId("2222");
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.CREATION_TEMLPATE, "2222", "<n/a>"), ate.getComment());
		
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		ate.setAsset(asset);
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.CREATION_TEMLPATE, "2222", "Asset"), ate.getComment());
	}
	
	@Test
	public void test_DestructionComment(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		assertNull(ate.getComment());

		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(null);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setName("t");
		p.setDescription("Tester");
		Mockito.when(participantService.getParticipant(Mockito.any())).thenReturn(p);
		
		ate.setChangeType(AssetChangeType.DESTRUCTION);
		ate.setParentAssetId("P01");
		ate.setAssetId("2222");
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.DESTRUCTION_TEMLPATE, "2222", "<n/a>"), ate.getComment());
		
		Asset prod = NotaryFactory.eINSTANCE.createAsset();
		ate.setAsset(prod);
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.DESTRUCTION_TEMLPATE, "2222", "Asset"), ate.getComment());
	}
	
	@Test
	public void test_ModificationComment(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		assertNull(ate.getComment());

		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(null);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setName("t");
		p.setDescription("Tester");
		Mockito.when(participantService.getParticipant(Mockito.any())).thenReturn(p);
		
		ate.setChangeType(AssetChangeType.MODIFICATION);
		ate.setParentAssetId("P01");
		ate.setAssetId("2222");
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.MODIFICATON_TEMPLATE, "2222", "<n/a>"), ate.getComment());
		
		Asset prod = NotaryFactory.eINSTANCE.createAsset();
		ate.setAsset(prod);
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.MODIFICATON_TEMPLATE, "2222", "Asset"), ate.getComment());
		System.out.println(ate.getComment());
	}
	
	@Test
	public void test_OwnerChangeComment(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		assertNull(ate.getComment());
		
		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(null);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setName("t");
		p.setDescription("Tester");
		Mockito.when(participantService.getParticipant(Mockito.any())).thenReturn(p);
		
		ate.setChangeType(AssetChangeType.OWNERSHIP);
		ate.setParticipantId("new");
		ate.setChangeData("old");
		ate.setParentAssetId("P01");
		ate.setAssetId("2222");
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.OWNERSHIP_TEMPLATE, "2222", "<n/a>", "old", "new"), ate.getComment());
		
		Asset prod = NotaryFactory.eINSTANCE.createAsset();
		ate.setAsset(prod);
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.OWNERSHIP_TEMPLATE, "2222", "Asset", "old", "new"), ate.getComment());
		System.out.println(ate.getComment());
	}
	
	@Test
	public void test_JoinComment(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		assertNull(ate.getComment());

		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(null);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setName("t");
		p.setDescription("Tester");
		Mockito.when(participantService.getParticipant(Mockito.any())).thenReturn(p);
		
		ate.setChangeType(AssetChangeType.JOIN);
		ate.setParentAssetId("P01");
		ate.setAssetId("2222");
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.JOIN_TEMPLATE, "2222", "<n/a>", "P01"), ate.getComment());
		
		Asset prod = NotaryFactory.eINSTANCE.createAsset();
		ate.setAsset(prod);
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.JOIN_TEMPLATE, "2222", "Asset", "P01"), ate.getComment());
		System.out.println(ate.getComment());
	}
	
	@Test
	public void test_SplitComment(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry))") TextProvider textProvider,
			@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setTransactionId("test");
		assertNull(ate.getLabel());
		assertNull(ate.getSource());
		assertNull(ate.getComment());
		
		Mockito.when(transactionService.getTransactionById(Mockito.any(String.class))).thenReturn(null);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setName("t");
		p.setDescription("Tester");
		Mockito.when(participantService.getParticipant(Mockito.any())).thenReturn(p);
		
		ate.setChangeType(AssetChangeType.SPLIT);
		ate.setParentAssetId("P01");
		ate.setAssetId("2222");
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.SPLIT_TEMPLATE, "2222", "<n/a>", "P01"), ate.getComment());
		
		Asset prod = NotaryFactory.eINSTANCE.createAsset();
		ate.setAsset(prod);
		
		assertNotNull(textProvider.provideText(ate, null));
		
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		assertEquals(p.getDescription(), ate.getSource());
		assertEquals(String.format(TextProvider.SPLIT_TEMPLATE, "2222", "Asset", "P01"), ate.getComment());
		System.out.println(ate.getComment());
	}
	
}

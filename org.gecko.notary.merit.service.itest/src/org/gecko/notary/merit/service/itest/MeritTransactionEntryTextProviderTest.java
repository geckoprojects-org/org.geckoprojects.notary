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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.gecko.notary.merit.model.merit.AchievementEntry;
import org.gecko.notary.merit.model.merit.BetResultType;
import org.gecko.notary.merit.model.merit.BettingEntry;
import org.gecko.notary.merit.model.merit.MeritFactory;
import org.gecko.notary.merit.model.merit.PurchaseEntry;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * This is your example OSGi integration test class
 * @since 1.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class MeritTransactionEntryTextProviderTest {
	
	/**
	 * Setup the services and maybe mock the services.
	 * The registered services are unregistered after the test
	 * @param bc the {@link BundleContext} that closes after each test
	 */
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		ParticipantService participantService = mock(ParticipantService.class);
		bc.registerService(ParticipantService.class, participantService, null);
		TransactionService transactionService = mock(TransactionService.class);
		bc.registerService(TransactionService.class, transactionService, null);
	}
	
	@Test
	public void testMeritTETextProvider_NullObject(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		when(transactionService.getTransactionById(any(String.class))).thenReturn(null);
		assertNull(textProvider.provideText(null, null));
		verify(transactionService, never()).getTransactionById(any(String.class));
	}
	
	@Test
	public void testMeritTETextProvider_NoEntry(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService,
			@InjectService ParticipantService participantService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		assertNotNull(participantService);
		when(transactionService.getTransactionById(any(String.class))).thenReturn(null);
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		assertNull(textProvider.provideText(a, null));
		verify(transactionService, never()).getTransactionById(any(String.class));
		verify(participantService, never()).getParticipant(any());
	}
	
	@Test
	public void testMeritTETextProvider_NoTransaction(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService,
			@InjectService ParticipantService participantService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		assertNotNull(participantService);
		when(transactionService.getTransactionById(any(String.class))).thenReturn(null);
		BettingEntry b = MeritFactory.eINSTANCE.createBettingEntry();
		assertNull(textProvider.provideText(b, null));
		verify(transactionService, times(1)).getTransactionById(any());
		verify(participantService, never()).getParticipant(any());
	}
	
	@Test
	public void testMeritTETextProvider_NoTransactionAssetTE(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService,
			@InjectService ParticipantService participantService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		assertNotNull(participantService);
		when(transactionService.getTransactionById(any(String.class))).thenReturn(null);
		when(participantService.getParticipant(any(String.class))).thenReturn(null);
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		assertNull(ate.getLabel());
		assertNull(textProvider.provideText(ate, null));
		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
		verify(transactionService, times(1)).getTransactionById(any());
		verify(participantService, times(1)).getParticipant(any());
	}
	
	@Test
	public void testMeritTETextProvider_Betting(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService,
			@InjectService ParticipantService participantService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		assertNotNull(participantService);
		BettingEntry b = MeritFactory.eINSTANCE.createBettingEntry();
		b.setTransactionId("TEST");
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("TEST");
		t.setDescription("This is a test");
		when(transactionService.getTransactionById(any(String.class))).thenReturn(t);
		when(participantService.getParticipant(any(String.class))).thenReturn(null);
		
		String text = textProvider.provideText(b, null);
		assertNotNull(text);
		assertTrue(text.contains("A bet was placed with a stake"));
		assertEquals("This is a test", b.getLabel());
		verify(transactionService, times(1)).getTransactionById(any());
		verify(participantService, times(1)).getParticipant(any());
	}
	
	@Test
	public void testMeritTETextProvider_Betting_Result(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService,
			@InjectService ParticipantService participantService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		assertNotNull(participantService);
		BettingEntry b = MeritFactory.eINSTANCE.createBettingEntry();
		b.setTransactionId("TEST");
		b.setResult(BetResultType.LOSE);
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("TEST");
		t.setDescription("This is a test");
		when(transactionService.getTransactionById(any(String.class))).thenReturn(t);
		when(participantService.getParticipant(any(String.class))).thenReturn(null);
		
		String text = textProvider.provideText(b, null);
		assertNotNull(text);
		assertTrue(text.contains("was finished with a result LOSE"));
		assertEquals("This is a test", b.getLabel());
		verify(transactionService, times(1)).getTransactionById(any());
		verify(participantService, times(1)).getParticipant(any());
		
		b.setResult(BetResultType.WIN);
		text = textProvider.provideText(b, null);
		assertNotNull(text);
		assertTrue(text.contains("was finished with a result WIN"));
		assertEquals("This is a test", b.getLabel());
		verify(transactionService, times(2)).getTransactionById(any());
		verify(participantService, times(2)).getParticipant(any());
	}
	
	@Test
	public void testMeritTETextProvider_Purchase(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService,
			@InjectService ParticipantService participantService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		assertNotNull(participantService);
		PurchaseEntry p = MeritFactory.eINSTANCE.createPurchaseEntry();
		p.setTransactionId("TEST");
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("TEST");
		t.setDescription("This is a test");
		when(transactionService.getTransactionById(any(String.class))).thenReturn(t);
		when(participantService.getParticipant(any(String.class))).thenReturn(null);
		
		String text = textProvider.provideText(p, null);
		assertNotNull(text);
		assertTrue(text.contains("merits has been purchased from provider"));
		assertEquals("This is a test", p.getLabel());
		verify(transactionService, times(1)).getTransactionById(any());
		verify(participantService, times(1)).getParticipant(any());
	}
	
	@Test
	public void testMeritTETextProvider_Achievement(@InjectService(filter = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") TextProvider textProvider,
			@InjectService TransactionService transactionService,
			@InjectService ParticipantService participantService) {
		assertNotNull(textProvider);
		assertNotNull(transactionService);
		assertNotNull(participantService);
		AchievementEntry a = MeritFactory.eINSTANCE.createAchievementEntry();
		a.setTransactionId("TEST");
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("TEST");
		t.setDescription("This is a test");
		when(transactionService.getTransactionById(any(String.class))).thenReturn(t);
		when(participantService.getParticipant(any(String.class))).thenReturn(null);
		
		String text = textProvider.provideText(a, null);
		assertNotNull(text);
		assertTrue(text.contains("An achievement"));
		assertEquals("This is a test", a.getLabel());
		verify(transactionService, times(1)).getTransactionById(any());
		verify(participantService, times(1)).getParticipant(any());
	}
	
}

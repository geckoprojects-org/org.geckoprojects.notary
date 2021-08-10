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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import org.eclipse.emf.ecore.EClass;
import org.gecko.notary.merit.model.merit.Badge;
import org.gecko.notary.merit.model.merit.BetResultType;
import org.gecko.notary.merit.model.merit.BettingEntry;
import org.gecko.notary.merit.model.merit.MeritFactory;
import org.gecko.notary.merit.model.merit.MeritPackage;
import org.gecko.notary.merit.model.merit.PurchaseProvider;
import org.gecko.notary.merit.service.api.MeritService;
import org.gecko.notary.service.api.AssetService;
import org.gecko.notary.service.api.TransactionEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class MeritServiceTest {
	
	public interface MyTest {
		int getTest();
	}
	
	/**
	 * Setup the services and maybe mock the services.
	 * The registered services are unregistered after the test
	 * @param bc the {@link BundleContext} that closes after each test
	 */
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		AssetService assetService = mock(AssetService.class);
		bc.registerService(AssetService.class, assetService, null);
		TransactionEntryService transactionEntryService = mock(TransactionEntryService.class);
		bc.registerService(TransactionEntryService.class, transactionEntryService, null);
	}
	
	/*
	 * ----------------------------------
	 * TEST MeritService#getBadge 
	 * ----------------------------------
	 */
	
	@Test
	public void testGetBadge_NullUser(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		when(assetService.getAssetByParticipant(any(String.class), any(String.class), any(EClass.class))).thenReturn(null);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.getBadge(null));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testGetBadge_UserNotRegistered(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		when(assetService.getAssetByParticipant(any(String.class), any(String.class), any(EClass.class))).thenReturn(null);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.getBadge("bla"));
		
		verify(assetService, times(1)).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	
	@Test
	public void testGetBadge_Success(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		
		assertEquals(b, meritService.getBadge("user"));
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
	}
	
	/*
	 * ----------------------------------
	 * TEST MeritService#purchaseMerits 
	 * ----------------------------------
	 */
	
	@Test
	public void testPurchaseMerits_NullUser(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.purchaseMerits(null, 0, null));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testPurchaseMerits_WrongProvider(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.purchaseMerits("bla", 0, null));
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.purchaseMerits("bla", 0, PurchaseProvider.OTHER));
	}
	
	@Test
	public void testPurchaseMerits_NegativeAmount(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.purchaseMerits("user", -10, PurchaseProvider.GOOGLE));
	}
	
	@Test
	public void testPurchaseMerits_OutOfLimit(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.purchaseMerits("user", 10000, PurchaseProvider.GOOGLE));
	}
	
	@Test
	public void testPurchaseMerits_ZeroAmount(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(assetService.updateAsset(any())).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenReturn(null);
		
		Badge returned = meritService.purchaseMerits("user", 0, PurchaseProvider.GOOGLE); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(assetService, never()).updateAsset(any());
		verify(transactionEntryService, never()).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testPurchaseMerits_Success(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		ArgumentCaptor<Badge> badgeC = ArgumentCaptor.forClass(Badge.class);
		when(assetService.updateAsset(badgeC.capture())).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenReturn(null);
		
		Badge returned = meritService.purchaseMerits("user", 100, PurchaseProvider.GOOGLE); 
		assertNotNull(returned);
		assertEquals(b, returned);
		Badge updated = badgeC.getValue();
		assertEquals(amount + 100, updated.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(assetService, times(1)).updateAsset(any());
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testPurchaseMerits_Fail(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenThrow(IllegalStateException.class);
		when(assetService.updateAsset(any())).thenThrow(IllegalStateException.class);
		
		Badge returned = meritService.purchaseMerits("user", 100, PurchaseProvider.GOOGLE); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
		verify(assetService, never()).updateAsset(any());
	}
	
	/*
	 * ----------------------------------
	 * TEST MeritService#earnMerits 
	 * ----------------------------------
	 */
	
	@Test
	public void testEarnMerits_NullUser(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.earnMerits(null, 0, null));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testEarnMerits_WrongProvider(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.earnMerits("bla", 0, null));
	}
	
	@Test
	public void testEarnMerits_NegativeAmount(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.earnMerits("user", -10, "Woaahh, Lost!"));
	}
	
	@Test
	public void testEarnMerits_OutOfLimit(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.earnMerits("user", 10000, "A bit too much"));
	}
	
	@Test
	public void testEarnMerits_ZeroAmount(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(assetService.updateAsset(any())).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenReturn(null);
		
		Badge returned = meritService.earnMerits("user", 0, "Because, you are an average user"); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(assetService, never()).updateAsset(any());
		verify(transactionEntryService, never()).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testEarnMerits_Success(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		ArgumentCaptor<Badge> badgeC = ArgumentCaptor.forClass(Badge.class);
		when(assetService.updateAsset(badgeC.capture())).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenReturn(null);
		
		Badge returned = meritService.earnMerits("user", 100, "Because, you are a goood user"); 
		assertNotNull(returned);
		assertEquals(b, returned);
		Badge updated = badgeC.getValue();
		assertEquals(amount + 100, updated.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(assetService, times(1)).updateAsset(any());
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testEarnMerits_Fail(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenThrow(IllegalStateException.class);
		when(assetService.updateAsset(any())).thenThrow(IllegalStateException.class);
		
		Badge returned = meritService.earnMerits("user", 100, "Because, you are a goood user"); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
		verify(assetService, never()).updateAsset(any());
	}
	
	/*
	 * ----------------------------------
	 * TEST MeritService#placeBet 
	 * ----------------------------------
	 */
	
	@Test
	public void testPlaceBet_NullBet(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.placeBet(null, null, 0));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testPlaceBet_NegativeAmount(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.placeBet("user", "bet", -10));
	}
	
	@Test
	public void testPlaceBet_OutOfLimit(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.placeBet("user", "bet", 10000));
	}
	
	@Test
	public void testPlaceBet_NullUser(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.placeBet(null, "bet", 10));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testPlaceBet_NotEnoughMerits(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(90);
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.placeBet("user", "bet", 100));
		verify(assetService, times(1)).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testPlaceBet_ZeroAmount(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenReturn(null);
		
		Badge returned = meritService.placeBet("user", "bet", 0); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, never()).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testPlaceBet_Success(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		ArgumentCaptor<BettingEntry> beC = ArgumentCaptor.forClass(BettingEntry.class);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), beC.capture())).thenReturn(null);
		
		Badge returned = meritService.placeBet("user", "bet", 10); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		BettingEntry entry = beC.getValue();
		assertNotNull(entry);
		assertEquals(BetResultType.INITIAL_BET, entry.getResult());
		assertEquals(10, entry.getStake());
		assertEquals("bet", entry.getBetIdentifier());
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testPlaceBet_Fail(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenThrow(IllegalStateException.class);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.placeBet("user", "bet", 100)); 
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	/*
	 * ----------------------------------
	 * TEST MeritService#setBetResult 
	 * ----------------------------------
	 */
	
	@Test
	public void testSetBetResult_NullBet(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult(null, null, 0, null));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testSetBetResult_OutOfLimit(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult(null, null, -10000, null));
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult(null, null, 10000, null));
	}
	
	@Test
	public void testSetBetResult_ValidResults(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult(null, "bet", 100, BetResultType.INITIAL_BET));
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult(null, "bet", 100, null));
	}
	
	@Test
	public void testSetBetResult_NullUser(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult(null, "bet", 100, BetResultType.LOSE));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testSetBetResult_ZeroAmount(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenReturn(null);
		
		Badge returned = meritService.setBetResult("user", "bet", 0, BetResultType.LOSE);
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, never()).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testSetBetResult_SuccessWin(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		ArgumentCaptor<BettingEntry> beC = ArgumentCaptor.forClass(BettingEntry.class);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), beC.capture())).thenReturn(null);
		ArgumentCaptor<Badge> badgeC = ArgumentCaptor.forClass(Badge.class);
		when(assetService.updateAsset(badgeC.capture())).thenReturn(b);
		
		Badge returned = meritService.setBetResult("user", "bet", 10, BetResultType.WIN); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		Badge updated = badgeC.getValue();
		assertEquals(amount + 10, updated.getMeritPoints());
		
		BettingEntry entry = beC.getValue();
		assertNotNull(entry);
		assertEquals(BetResultType.WIN, entry.getResult());
		assertEquals(10, entry.getStake());
		assertEquals("bet", entry.getBetIdentifier());
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
	}
	
	@Test
	public void testSetBetResult_SuccessLose(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		ArgumentCaptor<BettingEntry> beC = ArgumentCaptor.forClass(BettingEntry.class);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), beC.capture())).thenReturn(null);
		ArgumentCaptor<Badge> badgeC = ArgumentCaptor.forClass(Badge.class);
		when(assetService.updateAsset(badgeC.capture())).thenReturn(b);
		
		Badge returned = meritService.setBetResult("user", "bet", 10, BetResultType.LOSE); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		Badge updated = badgeC.getValue();
		assertEquals(amount - 10, updated.getMeritPoints());
		
		BettingEntry entry = beC.getValue();
		assertNotNull(entry);
		assertEquals(BetResultType.LOSE, entry.getResult());
		assertEquals(-10, entry.getStake());
		assertEquals("bet", entry.getBetIdentifier());
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
		verify(assetService, times(1)).updateAsset(any());
	}
	
	@Test
	public void testSetBetResult_SuccessLoseMoreThanYouHave(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(10);
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenReturn(null);
		when(assetService.updateAsset(any())).thenReturn(b);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult("user", "bet", 20, BetResultType.LOSE)); 
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, never()).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
		verify(assetService, never()).updateAsset(any());
	}
	
	@Test
	public void testSetBetResult_SuccessTie(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		ArgumentCaptor<BettingEntry> beC = ArgumentCaptor.forClass(BettingEntry.class);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), beC.capture())).thenReturn(null);
		ArgumentCaptor<Badge> badgeC = ArgumentCaptor.forClass(Badge.class);
		when(assetService.updateAsset(badgeC.capture())).thenReturn(b);
		
		Badge returned = meritService.setBetResult("user", "bet", 10, BetResultType.TIE); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		Badge updated = badgeC.getValue();
		assertEquals(amount, updated.getMeritPoints());
		
		BettingEntry entry = beC.getValue();
		assertNotNull(entry);
		assertEquals(BetResultType.TIE, entry.getResult());
		assertEquals(0, entry.getStake());
		assertEquals("bet", entry.getBetIdentifier());
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
		verify(assetService, times(1)).updateAsset(any());
	}
	
	@Test
	public void testSetBetResult_SuccessCancel(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		int amount = b.getMeritPoints();
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		ArgumentCaptor<BettingEntry> beC = ArgumentCaptor.forClass(BettingEntry.class);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), beC.capture())).thenReturn(null);
		ArgumentCaptor<Badge> badgeC = ArgumentCaptor.forClass(Badge.class);
		when(assetService.updateAsset(badgeC.capture())).thenReturn(b);
		
		Badge returned = meritService.setBetResult("user", "bet", 10, BetResultType.CANCEL); 
		assertNotNull(returned);
		assertEquals(b, returned);
		assertEquals(amount, returned.getMeritPoints());
		
		BettingEntry entry = beC.getValue();
		assertNotNull(entry);
		assertEquals(BetResultType.CANCEL, entry.getResult());
		assertEquals(0, entry.getStake());
		assertEquals("bet", entry.getBetIdentifier());
		
		Badge updated = badgeC.getValue();
		assertEquals(amount, updated.getMeritPoints());
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
		verify(assetService, times(1)).updateAsset(any());
		verify(assetService, times(1)).updateAsset(any());
	}
	
	@Test
	public void testSetBetResult_Fail(@InjectService AssetService assetService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(200);
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		when(transactionEntryService.createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any())).thenThrow(IllegalStateException.class);
		when(assetService.updateAsset(any())).thenThrow(IllegalStateException.class);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.setBetResult("user", "bet", 100, BetResultType.WIN)); 
		
		verify(assetService, times(1)).getAssetByParticipant(eq("user"), eq("user"), any(EClass.class));
		verify(transactionEntryService, times(1)).createTransactionEntry(eq("user"), eq(MeritPackage.Literals.BADGE), any());
		verify(assetService, never()).updateAsset(any());
	}
	
	/*
	 * ----------------------------------
	 * TEST MeritService#validateBadge 
	 * ----------------------------------
	 */
	
	@Test
	public void testValidateBadge_NullUser(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.validateBadge(null, 10));
		verify(assetService, never()).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testValidateBadge_NegativeAmount(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> meritService.validateBadge("user", -10));
	}
	
	@Test
	public void testValidateBadge_NotEnoughMerits(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(90);
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		assertFalse(meritService.validateBadge("user", 100));
		verify(assetService, times(1)).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	@Test
	public void testValidateBadge_EnoughMerits(@InjectService AssetService assetService,
			@InjectService MeritService meritService) {
		assertNotNull(assetService);
		assertNotNull(meritService);
		Badge b = MeritFactory.eINSTANCE.createBadge();
		b.setId("user");
		b.setMeritPoints(90);
		when(assetService.getAssetByParticipant(eq("user"), eq("user"), any(EClass.class))).thenReturn(b);
		assertTrue(meritService.validateBadge("user", 10));
		verify(assetService, times(1)).getAssetByParticipant(any(), any(), any(EClass.class));
	}
	
	
}

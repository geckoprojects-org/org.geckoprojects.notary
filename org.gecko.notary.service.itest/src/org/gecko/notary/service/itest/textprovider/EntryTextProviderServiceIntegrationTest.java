package org.gecko.notary.service.itest.textprovider;
///**
// * Copyright (c) 2012 - 2018 Data In Motion and others.
// * All rights reserved. 
// * 
// * This program and the accompanying materials are made available under the terms of the 
// * Eclipse Public License v1.0 which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     Data In Motion - initial API and implementation
// */
//package de.dim.diamant.service.tests.textprovider;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Dictionary;
//import java.util.Hashtable;
//
//import org.gecko.core.tests.AbstractOSGiTest;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.osgi.framework.Constants;
//import org.osgi.framework.FrameworkUtil;
//import org.osgi.framework.InvalidSyntaxException;
//
//import de.dim.diamant.AssetChangeType;
//import de.dim.diamant.AssetTransactionEntry;
//import de.dim.diamant.DiamantFactory;
//import de.dim.diamant.FeedbackTransactionEntry;
//import de.dim.diamant.Gauge;
//import de.dim.diamant.InboundLogistic;
//import de.dim.diamant.OutboundLogistic;
//import de.dim.diamant.Participant;
//import de.dim.diamant.Product;
//import de.dim.diamant.SellingContract;
//import de.dim.diamant.SupportEntry;
//import de.dim.diamant.Transaction;
//import de.dim.diamant.service.api.ParticipantService;
//import de.dim.diamant.service.api.TransactionService;
//import de.dim.diamant.service.api.textprovider.TextProvider;
//
//
///**
// * <p>
// * This is an integration test for the context service
// * </p>
// * 
// * @since 1.0
// */
//@RunWith(MockitoJUnitRunner.class)
//public class EntryTextProviderServiceIntegrationTest extends AbstractOSGiTest {
//	
//	@Mock
//	private TransactionService transactionService;
//	@Mock
//	private ParticipantService participantService;
//	private TextProvider textProvider;
//	
//	/**
//	 * Creates a new instance.
//	 * @param bundleContext
//	 */
//	public EntryTextProviderServiceIntegrationTest() {
//		super(FrameworkUtil.getBundle(EntryTextProviderServiceIntegrationTest.class).getBundleContext());
//	}
//
//	/**
//	 * Here you can put everything you want to be exectued before every test
//	 */
//	public void doBefore() {
//		
//	}
//	
//	/**
//	 * Here you can put everything you want to be exectued after every test
//	 */
//	public void doAfter() {
//		
//	}
//	
//	/**
//	 * Here you can put your test
//	 * @throws InvalidSyntaxException 
//	 */
//	@Test
//	public void testSimple_Null() throws InvalidSyntaxException {
//		setupServices();
//		assertNull(textProvider.provideText(null, null));
//	}
//	
//	@Test
//	public void test_NoTransaction() throws InvalidSyntaxException {
//		setupServices();
//		OutboundLogistic ob = DiamantFactory.eINSTANCE.createOutboundLogistic();
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
//		assertNull(textProvider.provideText(ob, null));
//	}
//	
//	@Test
//	public void test_ValidTransaction() throws InvalidSyntaxException {
//		setupServices();
//		OutboundLogistic ob = DiamantFactory.eINSTANCE.createOutboundLogistic();
//		ob.setTransactionId("test");
//		assertNull(ob.getLabel());
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		assertNull(textProvider.provideText(ob, null));
//		assertEquals(t.getDescription(), ob.getLabel());
//	}
//	
//	@Test
//	public void test_NoParticipant() throws InvalidSyntaxException {
//		setupServices();
//		OutboundLogistic ob = DiamantFactory.eINSTANCE.createOutboundLogistic();
//		ob.setTransactionId("test");
//		assertNull(ob.getLabel());
//		assertNull(ob.getSource());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(null);
//		
//		assertNull(textProvider.provideText(ob, null));
//		assertEquals(t.getDescription(), ob.getLabel());
//		assertNull(ob.getSource());
//	}
//	
//	@Test
//	public void test_ValidParticipant() throws InvalidSyntaxException {
//		setupServices();
//		OutboundLogistic ob = DiamantFactory.eINSTANCE.createOutboundLogistic();
//		ob.setTransactionId("test");
//		assertNull(ob.getLabel());
//		assertNull(ob.getSource());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		assertNull(textProvider.provideText(ob, null));
//		
//		assertEquals(t.getDescription(), ob.getLabel());
//		assertEquals(p.getName(), ob.getSource());
//		
//		p.setDescription("Tester");
//		assertNull(textProvider.provideText(ob, null));
//		
//		assertEquals(t.getDescription(), ob.getLabel());
//		assertEquals(p.getDescription(), ob.getSource());
//		
//	}
//	
//	@Test
//	public void test_OutboundComment() throws InvalidSyntaxException {
//		setupServices();
//		OutboundLogistic ob = DiamantFactory.eINSTANCE.createOutboundLogistic();
//		ob.setTransactionId("test");
//		assertNull(ob.getLabel());
//		assertNull(ob.getSource());
//		assertNull(ob.getComment());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		assertNull(textProvider.provideText(ob, null));
//		
//		assertEquals(t.getDescription(), ob.getLabel());
//		assertEquals(p.getDescription(), ob.getSource());
//		assertEquals(String.format(TextProvider.OUTBOUND_TEMPLATE, "<n/a>", "<n/a>", "<n/a>", "<n/a>"), ob.getComment());
//		
//		ob.setTargetAddress("Teststreet");
//		assertNull(textProvider.provideText(ob, null));
//		
//		assertEquals(t.getDescription(), ob.getLabel());
//		assertEquals(p.getDescription(), ob.getSource());
//		assertEquals(String.format(TextProvider.OUTBOUND_TEMPLATE, "Teststreet", "<n/a>", "<n/a>", "<n/a>"), ob.getComment());
//		
//		ob.setTransportationProvider("THL");
//		ob.setTransportationTrackingId("THL-123");
//		ob.setAssetId("2222");
//		assertNull(textProvider.provideText(ob, null));
//		
//		assertEquals(t.getDescription(), ob.getLabel());
//		assertEquals(p.getDescription(), ob.getSource());
//		assertEquals(String.format(TextProvider.OUTBOUND_TEMPLATE, "Teststreet", "THL", "THL-123", "2222"), ob.getComment());
//		System.out.println(ob.getComment());
//	}
//	
//	@Test
//	public void test_InboundComment() throws InvalidSyntaxException {
//		setupServices();
//		InboundLogistic ib = DiamantFactory.eINSTANCE.createInboundLogistic();
//		ib.setTransactionId("test");
//		assertNull(ib.getLabel());
//		assertNull(ib.getSource());
//		assertNull(ib.getComment());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		ib.setArticleNumber("MYART-321");
//		ib.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(ib, null));
//		
//		
//		assertEquals(t.getDescription(), ib.getLabel());
//		assertEquals(p.getDescription(), ib.getSource());
//		assertEquals(String.format(TextProvider.INBOUND_TEMPLATE, "MYART-321", "2222"), ib.getComment());
//		
//		System.out.println(ib.getComment());
//	}
//	
//	@Test
//	public void test_GaugeComment() throws InvalidSyntaxException, ParseException {
//		setupServices();
//		Gauge gauge = DiamantFactory.eINSTANCE.createGauge();
//		gauge.setTransactionId("test");
//		assertNull(gauge.getLabel());
//		assertNull(gauge.getSource());
//		assertNull(gauge.getComment());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		gauge.setCertificateId("MYART-321");
//		gauge.setExecutingAuthority("TEST-AUTH");
//		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//		Date d = sdf.parse("24.12.2009");
//		gauge.setValidTo(d);
//		gauge.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(gauge, null));
//		
//		
//		assertEquals(t.getDescription(), gauge.getLabel());
//		assertEquals(p.getDescription(), gauge.getSource());
//		assertEquals(String.format(TextProvider.GAUGE_TEMPLATE, "TEST-AUTH", "MYART-321", "24.12.2009"), gauge.getComment());
//		
//		System.out.println(gauge.getComment());
//	}
//	
//	@Test
//	public void test_SellingComment() throws InvalidSyntaxException {
//		setupServices();
//		SellingContract sc = DiamantFactory.eINSTANCE.createSellingContract();
//		sc.setTransactionId("test");
//		assertNull(sc.getLabel());
//		assertNull(sc.getSource());
//		assertNull(sc.getComment());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		sc.setContractId("C01");
//		sc.setCustomer("Me");
//		sc.setContractText("Piece of cheese");
//		sc.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(sc, null));
//		
//		
//		assertEquals(t.getDescription(), sc.getLabel());
//		assertEquals(p.getDescription(), sc.getSource());
//		assertEquals(String.format(TextProvider.SELLING_TEMPLATE, "Me", "C01", "Piece of cheese", "2222"), sc.getComment());
//		
//		System.out.println(sc.getComment());
//	}
//	
//	@Test
//	public void test_SupportComment() throws InvalidSyntaxException {
//		setupServices();
//		SupportEntry se = DiamantFactory.eINSTANCE.createSupportEntry();
//		se.setTransactionId("test");
//		assertNull(se.getLabel());
//		assertNull(se.getSource());
//		assertNull(se.getComment());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		se.setText("HOLLA");
//		se.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(se, null));
//		
//		
//		assertEquals(t.getDescription(), se.getLabel());
//		assertEquals(p.getDescription(), se.getSource());
//		assertEquals(String.format(TextProvider.SUPPORT_NOACTION_TEMPLATE, "HOLLA"), se.getComment());
//		
//		se.setActionEntryId("test");
//		se.setActionEntryLabel("did something");
//		assertNull(textProvider.provideText(se, null));
//		
//		assertEquals(t.getDescription(), se.getLabel());
//		assertEquals(p.getDescription(), se.getSource());
//		assertEquals(String.format(TextProvider.SUPPORT_ACTION_TEMPLATE, "HOLLA", "did something"), se.getComment());
//		
//		System.out.println(se.getComment());
//	}
//	
//	@Test
//	public void test_CreationComment() throws InvalidSyntaxException {
//		setupServices();
//		AssetTransactionEntry ate = DiamantFactory.eINSTANCE.createAssetTransactionEntry();
//		ate.setTransactionId("test");
//		assertNull(ate.getLabel());
//		assertNull(ate.getSource());
//		assertNull(ate.getComment());
//		
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		ate.setChangeType(AssetChangeType.CREATION);
//		ate.setParentAssetId("P01");
//		ate.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.CREATION_TEMLPATE, "2222", "<n/a>"), ate.getComment());
//		
//		Product prod = DiamantFactory.eINSTANCE.createProduct();
//		ate.setAsset(prod);
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.CREATION_TEMLPATE, "2222", "Product"), ate.getComment());
//		System.out.println(ate.getComment());
//	}
//	
//	@Test
//	public void test_DestructionComment() throws InvalidSyntaxException {
//		setupServices();
//		AssetTransactionEntry ate = DiamantFactory.eINSTANCE.createAssetTransactionEntry();
//		ate.setTransactionId("test");
//		assertNull(ate.getLabel());
//		assertNull(ate.getSource());
//		assertNull(ate.getComment());
//
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		ate.setChangeType(AssetChangeType.DESTRUCTION);
//		ate.setParentAssetId("P01");
//		ate.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.DESTRUCTION_TEMLPATE, "2222", "<n/a>"), ate.getComment());
//		
//		Product prod = DiamantFactory.eINSTANCE.createProduct();
//		ate.setAsset(prod);
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.DESTRUCTION_TEMLPATE, "2222", "Product"), ate.getComment());
//		System.out.println(ate.getComment());
//	}
//	
//	@Test
//	public void test_ModificationComment() throws InvalidSyntaxException {
//		setupServices();
//		AssetTransactionEntry ate = DiamantFactory.eINSTANCE.createAssetTransactionEntry();
//		ate.setTransactionId("test");
//		assertNull(ate.getLabel());
//		assertNull(ate.getSource());
//		assertNull(ate.getComment());
//
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		ate.setChangeType(AssetChangeType.MODIFICATION);
//		ate.setParentAssetId("P01");
//		ate.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.MODIFICATON_TEMPLATE, "2222", "<n/a>"), ate.getComment());
//		
//		Product prod = DiamantFactory.eINSTANCE.createProduct();
//		ate.setAsset(prod);
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.MODIFICATON_TEMPLATE, "2222", "Product"), ate.getComment());
//		System.out.println(ate.getComment());
//	}
//	
//	@Test
//	public void test_OwnerChangeComment() throws InvalidSyntaxException {
//		setupServices();
//		AssetTransactionEntry ate = DiamantFactory.eINSTANCE.createAssetTransactionEntry();
//		ate.setTransactionId("test");
//		assertNull(ate.getLabel());
//		assertNull(ate.getSource());
//		assertNull(ate.getComment());
//		
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		ate.setChangeType(AssetChangeType.OWNERSHIP);
//		ate.setParticipantId("new");
//		ate.setChangeData("old");
//		ate.setParentAssetId("P01");
//		ate.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.OWNERSHIP_TEMPLATE, "2222", "<n/a>", "old", "new"), ate.getComment());
//		
//		Product prod = DiamantFactory.eINSTANCE.createProduct();
//		ate.setAsset(prod);
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.OWNERSHIP_TEMPLATE, "2222", "Product", "old", "new"), ate.getComment());
//		System.out.println(ate.getComment());
//	}
//	
//	@Test
//	public void test_JoinComment() throws InvalidSyntaxException {
//		setupServices();
//		AssetTransactionEntry ate = DiamantFactory.eINSTANCE.createAssetTransactionEntry();
//		ate.setTransactionId("test");
//		assertNull(ate.getLabel());
//		assertNull(ate.getSource());
//		assertNull(ate.getComment());
//
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		ate.setChangeType(AssetChangeType.JOIN);
//		ate.setParentAssetId("P01");
//		ate.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.JOIN_TEMPLATE, "2222", "<n/a>", "P01"), ate.getComment());
//		
//		Product prod = DiamantFactory.eINSTANCE.createProduct();
//		ate.setAsset(prod);
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.JOIN_TEMPLATE, "2222", "Product", "P01"), ate.getComment());
//		System.out.println(ate.getComment());
//	}
//	
//	@Test
//	public void test_SplitComment() throws InvalidSyntaxException {
//		setupServices();
//		AssetTransactionEntry ate = DiamantFactory.eINSTANCE.createAssetTransactionEntry();
//		ate.setTransactionId("test");
//		assertNull(ate.getLabel());
//		assertNull(ate.getSource());
//		assertNull(ate.getComment());
//		
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		ate.setChangeType(AssetChangeType.SPLIT);
//		ate.setParentAssetId("P01");
//		ate.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.SPLIT_TEMPLATE, "2222", "<n/a>", "P01"), ate.getComment());
//		
//		Product prod = DiamantFactory.eINSTANCE.createProduct();
//		ate.setAsset(prod);
//		
//		assertNull(textProvider.provideText(ate, null));
//		
//		assertEquals(TextProvider.ASSET_DEFAULT_LABEL, ate.getLabel());
//		assertEquals(p.getDescription(), ate.getSource());
//		assertEquals(String.format(TextProvider.SPLIT_TEMPLATE, "2222", "Product", "P01"), ate.getComment());
//		System.out.println(ate.getComment());
//	}
//	
//	@Test
//	public void test_Other() throws InvalidSyntaxException {
//		setupServices();
//		FeedbackTransactionEntry fte = DiamantFactory.eINSTANCE.createFeedbackTransactionEntry();
//		fte.setTransactionId("test");
//		assertNull(fte.getLabel());
//		assertNull(fte.getSource());
//		assertNull(fte.getComment());
//		
//		Transaction t = DiamantFactory.eINSTANCE.createTransaction();
//		t.setId("test");
//		t.setDescription("Test Transaction");
//		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setName("t");
//		p.setDescription("Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		
//		fte.setAssetId("2222");
//		
//		assertNull(textProvider.provideText(fte, null));
//		
//		assertEquals(t.getDescription(), fte.getLabel());
//		assertEquals(p.getDescription(), fte.getSource());
//		assertNull(fte.getComment());
//		
//	}
//	
//	private void setupServices() throws InvalidSyntaxException {
//		Dictionary<String, Object> transactionProperties = new Hashtable<String, Object>();
//		transactionProperties.put(Constants.SERVICE_RANKING, 1000);
//		registerServiceForCleanup(TransactionService.class, transactionService, transactionProperties);
//		Dictionary<String, Object> participantProperties = new Hashtable<String, Object>();
//		participantProperties.put(Constants.SERVICE_RANKING, 1000);
//		registerServiceForCleanup(ParticipantService.class, participantService, participantProperties);
//		textProvider = (TextProvider) createTrackedChecker("(&(object=TransactionEntry)(target=TransactionEntry))", true).assertCreations(1, true).trackedServiceNotNull().getTrackedService();
//		assertNotNull(textProvider);		
//	}
//	
//}

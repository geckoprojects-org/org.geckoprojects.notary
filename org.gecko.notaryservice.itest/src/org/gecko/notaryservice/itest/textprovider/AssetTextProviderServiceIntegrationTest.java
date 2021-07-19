package org.gecko.notaryservice.itest.textprovider;
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
//import static org.junit.Assert.assertTrue;
//
//import java.text.ParseException;
//import java.util.Dictionary;
//import java.util.Hashtable;
//
//import org.eclipse.emf.ecore.util.EcoreUtil;
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
//import de.dim.diamant.AssetInfo;
//import de.dim.diamant.DiamantFactory;
//import de.dim.diamant.OutboundLogistic;
//import de.dim.diamant.Participant;
//import de.dim.diamant.Product;
//import de.dim.diamant.ProductType;
//import de.dim.diamant.SupportCase;
//import de.dim.diamant.SupportStatusType;
//import de.dim.diamant.Treatment;
//import de.dim.diamant.service.api.ParticipantService;
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
//public class AssetTextProviderServiceIntegrationTest extends AbstractOSGiTest {
//	
//	private TextProvider textProvider;
//	@Mock
//	private ParticipantService participantService;
//	
//	/**
//	 * Creates a new instance.
//	 * @param bundleContext
//	 */
//	public AssetTextProviderServiceIntegrationTest() {
//		super(FrameworkUtil.getBundle(AssetTextProviderServiceIntegrationTest.class).getBundleContext());
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
//	public void testSimple_WrongEntry() throws InvalidSyntaxException {
//		setupServices();
//		OutboundLogistic ob = DiamantFactory.eINSTANCE.createOutboundLogistic();
//		assertNull(textProvider.provideText(ob, null));
//	}
//	
//	@Test
//	public void testExistingInfo() throws ParseException, InvalidSyntaxException {
//		setupServices();
//		Product p = DiamantFactory.eINSTANCE.createProduct();
//		p.setId("test");
//		AssetInfo info = DiamantFactory.eINSTANCE.createAssetInfo();
//		p.setInfo(info);
//		Product pCheck = EcoreUtil.copy(p);
//		textProvider.provideText(p, null);
//		assertTrue(EcoreUtil.equals(pCheck, p));
//	}
//	
//	@Test
//	public void testSimple_Entry() throws ParseException, InvalidSyntaxException {
//		setupServices();
//		Product p = DiamantFactory.eINSTANCE.createProduct();
//		p.setId("test");
//		assertNull(textProvider.provideText(p, null));
//		assertNotNull(p.getInfo());
//		Treatment t = DiamantFactory.eINSTANCE.createTreatment();
//		t.setId("test");
//		assertNull(textProvider.provideText(t, null));
//		assertNotNull(t.getInfo());
//		SupportCase sc = DiamantFactory.eINSTANCE.createSupportCase();
//		sc.setId("test");
//		assertNull(textProvider.provideText(sc, null));
//		assertNotNull(sc.getInfo());
//	}
//	
//	@Test
//	public void testProduct() throws InvalidSyntaxException {
//		setupServices();
//		
//		Product p = DiamantFactory.eINSTANCE.createProduct();
//		p.setId("test");
//		assertNull(textProvider.provideText(p, null));
//		assertNotNull(p.getInfo());
//		AssetInfo info = p.getInfo();
//		assertEquals(String.format(TextProvider.PRODUCT_LABEL_TEMPLATE, "<n/a>", ProductType.CLASS_1, "<n/a>"), info.getLabel());
//		assertEquals(String.format(TextProvider.PRODUCT_DESCRIPTION_TEMPLATE, "<n/a>", "<n/a>"), info.getDescription());
//		assertEquals("<n/a>", info.getAssetIdentifier());
//		assertEquals("Product", info.getAssetTypeLabel());
//		
//		p = DiamantFactory.eINSTANCE.createProduct();
//		p.setId("test");
//		p.setArticleNumberRef("1234");
//		p.setSerialNumber("1234-1234");
//		p.setDescription("The Machine");
//		p.setVendor("Mr. Incredible");
//		assertNull(textProvider.provideText(p, null));
//		assertNotNull(p.getInfo());
//		info = p.getInfo();
//		
//		assertEquals(String.format(TextProvider.PRODUCT_LABEL_TEMPLATE, "The Machine", ProductType.CLASS_1, "Mr. Incredible"), info.getLabel());
//		assertEquals(String.format(TextProvider.PRODUCT_DESCRIPTION_TEMPLATE, "1234", "1234-1234"), info.getDescription());
//		assertEquals("1234-1234", info.getAssetIdentifier());
//		assertEquals("Product", info.getAssetTypeLabel());
//		
//		Participant c = DiamantFactory.eINSTANCE.createParticipant();
//		c.setId("creator");
//		c.setDescription("CRATER");
//		Participant o = DiamantFactory.eINSTANCE.createParticipant();
//		o.setId("owner");
//		o.setDescription("OWEN");
//		Mockito.when(participantService.getParticipant("creator")).thenReturn(c);
//		Mockito.when(participantService.getParticipant("owner")).thenReturn(o);
//		p = DiamantFactory.eINSTANCE.createProduct();
//		p.setId("test");
//		p.setCreatorId("creator");
//		p.setOwnerId("owner");
//		p.setArticleNumberRef("1234");
//		p.setSerialNumber("1234-1234");
//		p.setDescription("The Machine");
//		p.setVendor("Mr. Incredible");
//		assertNull(textProvider.provideText(p, null));
//		assertNotNull(p.getInfo());
//		info = p.getInfo();
//		
//		assertEquals(String.format(TextProvider.PRODUCT_LABEL_TEMPLATE, "The Machine", ProductType.CLASS_1, "Mr. Incredible"), info.getLabel());
//		assertEquals(String.format(TextProvider.PRODUCT_DESCRIPTION_TEMPLATE, "1234", "1234-1234").replace("${CREATOR}", "CRATER").replace("${OWNER}", "OWEN"), info.getDescription());
//		assertEquals("1234-1234", info.getAssetIdentifier());
//		assertEquals("Product", info.getAssetTypeLabel());
//	}
//	
//	@Test
//	public void testTreatment() throws InvalidSyntaxException {
//		setupServices();
//		
//		Treatment t = DiamantFactory.eINSTANCE.createTreatment();
//		t.setId("test");
//		assertNull(textProvider.provideText(t, null));
//		assertNotNull(t.getInfo());
//		AssetInfo info = t.getInfo();
//		assertEquals(String.format(TextProvider.TREATMENT_LABEL_TEMPLATE, "<n/a>", "<n/a>"), info.getLabel());
//		assertEquals(String.format(TextProvider.TREATMENT_DESCRIPTION_TEMPLATE, "<n/a>", "<n/a>"), info.getDescription());
//		assertEquals("test", info.getAssetIdentifier());
//		assertEquals("Treatment", info.getAssetTypeLabel());
//		
//		t = DiamantFactory.eINSTANCE.createTreatment();
//		t.setId("test");
//		t.setName("Beinbruch");
//		t.setPatientInsuranceId("bb001");
//		t.setDescription("Platte Beinbruch");
//		t.setRoom("OP3");
//		assertNull(textProvider.provideText(t, null));
//		assertNotNull(t.getInfo());
//		info = t.getInfo();
//		
//		assertEquals(String.format(TextProvider.TREATMENT_LABEL_TEMPLATE, "Beinbruch", "bb001"), info.getLabel());
//		assertEquals(String.format(TextProvider.TREATMENT_DESCRIPTION_TEMPLATE, "Platte Beinbruch", "OP3"), info.getDescription());
//		assertEquals("test", info.getAssetIdentifier());
//		assertEquals("Treatment", info.getAssetTypeLabel());
//		
//		Participant c = DiamantFactory.eINSTANCE.createParticipant();
//		c.setId("creator");
//		c.setDescription("CRATER");
//		Participant o = DiamantFactory.eINSTANCE.createParticipant();
//		o.setId("owner");
//		o.setDescription("OWEN");
//		Mockito.when(participantService.getParticipant("creator")).thenReturn(c);
//		Mockito.when(participantService.getParticipant("owner")).thenReturn(o);
//		
//		t = DiamantFactory.eINSTANCE.createTreatment();
//		t.setId("test");
//		t.setCreatorId("creator");
//		t.setOwnerId("owner");
//		t.setName("Beinbruch");
//		t.setPatientInsuranceId("bb001");
//		t.setDescription("Platte Beinbruch");
//		t.setRoom("OP3");
//		assertNull(textProvider.provideText(t, null));
//		assertNotNull(t.getInfo());
//		info = t.getInfo();
//		
//		assertEquals(String.format(TextProvider.TREATMENT_LABEL_TEMPLATE, "Beinbruch", "bb001"), info.getLabel());
//		assertEquals(String.format(TextProvider.TREATMENT_DESCRIPTION_TEMPLATE, "Platte Beinbruch", "OP3").replace("${CREATOR}", "CRATER").replace("${OWNER}", "OWEN"), info.getDescription());
//		assertEquals("test", info.getAssetIdentifier());
//		assertEquals("Treatment", info.getAssetTypeLabel());
//	}
//	
//	@Test
//	public void testSupportCase() throws InvalidSyntaxException {
//		setupServices();
//		
//		SupportCase sc = DiamantFactory.eINSTANCE.createSupportCase();
//		sc.setId("test");
//		assertNull(textProvider.provideText(sc, null));
//		assertNotNull(sc.getInfo());
//		AssetInfo info = sc.getInfo();
//		assertEquals(String.format(TextProvider.SUPPORT_CASE_LABEL_TEMPLATE, "<n/a>", "<n/a>"), info.getLabel());
//		assertEquals(String.format(TextProvider.SUPPORT_CASE_DESCRIPTION_TEMPLATE, "<n/a>", "<n/a>", SupportStatusType.NEW), info.getDescription());
//		assertEquals("<n/a>", info.getAssetIdentifier());
//		assertEquals("SupportCase", info.getAssetTypeLabel());
//		
//		sc = DiamantFactory.eINSTANCE.createSupportCase();
//		sc.setId("test");
//		sc.setDescription("Defekte Maschine");
//		sc.setLabel("Maschine Kaputt");
//		sc.setSupportAssetId("kaputtMaschine01");
//		sc.setCustomer("Ich");
//		sc.setCustomerContact("Herr L.Ich");
//		sc.setStatus(SupportStatusType.REJECTED);
//		sc.setTicketNumber("ticket123");
//		assertNull(textProvider.provideText(sc, null));
//		assertNotNull(sc.getInfo());
//		info = sc.getInfo();
//		
//		assertEquals(String.format(TextProvider.SUPPORT_CASE_LABEL_TEMPLATE, "Maschine Kaputt", "kaputtMaschine01"), info.getLabel());
//		assertEquals(String.format(TextProvider.SUPPORT_CASE_DESCRIPTION_TEMPLATE, "Herr L.Ich", "Defekte Maschine", SupportStatusType.REJECTED), info.getDescription());
//		assertEquals("ticket123", info.getAssetIdentifier());
//		assertEquals("SupportCase", info.getAssetTypeLabel());
//		
//		Participant c = DiamantFactory.eINSTANCE.createParticipant();
//		c.setId("creator");
//		c.setDescription("CRATER");
//		Participant o = DiamantFactory.eINSTANCE.createParticipant();
//		o.setId("owner");
//		o.setDescription("OWEN");
//		Mockito.when(participantService.getParticipant("creator")).thenReturn(c);
//		Mockito.when(participantService.getParticipant("owner")).thenReturn(o);
//		
//		sc = DiamantFactory.eINSTANCE.createSupportCase();
//		sc.setId("test");
//		sc.setCreatorId("creator");
//		sc.setOwnerId("owner");
//		sc.setDescription("Defekte Maschine");
//		sc.setLabel("Maschine Kaputt");
//		sc.setSupportAssetId("kaputtMaschine01");
//		sc.setCustomer("Ich");
//		sc.setCustomerContact("Herr L.Ich");
//		sc.setStatus(SupportStatusType.REJECTED);
//		sc.setTicketNumber("ticket123");
//		assertNull(textProvider.provideText(sc, null));
//		assertNotNull(sc.getInfo());
//		info = sc.getInfo();
//		
//		assertEquals(String.format(TextProvider.SUPPORT_CASE_LABEL_TEMPLATE, "Maschine Kaputt", "kaputtMaschine01"), info.getLabel());
//		assertEquals(String.format(TextProvider.SUPPORT_CASE_DESCRIPTION_TEMPLATE, "Herr L.Ich", "Defekte Maschine", SupportStatusType.REJECTED).replace("${CREATOR}", "CRATER").replace("${OWNER}", "OWEN"), info.getDescription());
//		assertEquals("ticket123", info.getAssetIdentifier());
//		assertEquals("SupportCase", info.getAssetTypeLabel());
//	}
//	
//	private void setupServices() throws InvalidSyntaxException {
//		Dictionary<String, Object> participantProperties = new Hashtable<String, Object>();
//		participantProperties.put(Constants.SERVICE_RANKING, 1000);
//		registerServiceForCleanup(ParticipantService.class, participantService, participantProperties);
//		textProvider = (TextProvider) createTrackedChecker("(&(object=Asset)(target=Asset))", true).assertCreations(1, true).trackedServiceNotNull().getTrackedService();
//		assertNotNull(textProvider);
//	}
//	
//}

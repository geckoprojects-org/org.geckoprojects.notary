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
//
//import org.gecko.core.tests.AbstractOSGiTest;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.osgi.framework.FrameworkUtil;
//import org.osgi.framework.InvalidSyntaxException;
//
//import de.dim.diamant.DiamantFactory;
//import de.dim.diamant.OutboundLogistic;
//import de.dim.diamant.SellingContract;
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
//public class TransactionEntryAssetTextProviderServiceIntegrationTest extends AbstractOSGiTest {
//	
//	private TextProvider textProvider;
//	
//	/**
//	 * Creates a new instance.
//	 * @param bundleContext
//	 */
//	public TransactionEntryAssetTextProviderServiceIntegrationTest() {
//		super(FrameworkUtil.getBundle(TransactionEntryAssetTextProviderServiceIntegrationTest.class).getBundleContext());
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
//	public void testSimple_Entry() throws ParseException, InvalidSyntaxException {
//		setupServices();
//		SellingContract sc = DiamantFactory.eINSTANCE.createSellingContract();
//		assertEquals("Vertrag: null vom n/a", textProvider.provideText(sc, null));
//		sc.setContractId("1234");
//		assertEquals("Vertrag: 1234 vom n/a", textProvider.provideText(sc, null));
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		Date d = sdf.parse("2019-12-24");
//		sc.setContractDate(d);
//		assertEquals("Vertrag: 1234 vom 24.12.2019", textProvider.provideText(sc, null));
//		
//	}
//	
//	private void setupServices() throws InvalidSyntaxException {
//		textProvider = (TextProvider) createTrackedChecker("(&(object=TransactionEntry)(target=Asset))", true).assertCreations(1, true).trackedServiceNotNull().getTrackedService();
//		assertNotNull(textProvider);
//	}
//	
//}

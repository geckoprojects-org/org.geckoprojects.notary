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
package org.gecko.notary.service.itest.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.service.api.TransactionEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
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
public class AssetModificationHandlerTest {
	
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) throws InvalidSyntaxException {
		Dictionary<String, Object> entryProperties = new Hashtable<String, Object>();
		entryProperties.put(Constants.SERVICE_RANKING, 1000);
		entryProperties.put(Constants.SERVICE_SCOPE, Constants.SCOPE_PROTOTYPE);
		TransactionEntryService entryService = mock(TransactionEntryService.class);
		bc.registerService(TransactionEntryService.class, entryService, entryProperties);
	}

	@Test
	public void testAssetChangeEvent_Empty(@InjectService(filter = "(component.name=AssetTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", new HashMap<String, Object>()));
		Mockito.verify(entryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
	}
	
	@Test
	public void testAssetChangeEvent_NoNew(@InjectService(filter = "(component.name=AssetTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		EventHandler notificationHandler = notificationHandlerAware.getService();
		
		Map<String, Object> eventProps = new HashMap<String, Object>();
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("test");
		eventProps.put("current", asset);
		
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
	}
	
	@Test
	public void testAssetChangeEvent_NewNoCurrent(@InjectService(filter = "(component.name=AssetTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		EventHandler notificationHandler = notificationHandlerAware.getService();
		Map<String, Object> eventProps = new HashMap<String, Object>();
		Asset assetNew = NotaryFactory.eINSTANCE.createAsset();
		assetNew.setId("test");
		eventProps.put("new", assetNew);
		ArgumentCaptor<Asset> assetC = ArgumentCaptor.forClass(Asset.class);
		Mockito.doNothing().when(entryService).createAssetModificationTransaction(Mockito.nullable(Asset.class), assetC.capture());
		
		notificationHandler.handleEvent(new Event("test", eventProps));
		
		assertEquals(assetNew, assetC.getValue());
		Mockito.verify(entryService, Mockito.times(1)).createAssetModificationTransaction(Mockito.nullable(Asset.class), Mockito.any(Asset.class));
	}
	
	@Test
	public void testAssetChangeEvent_NewAndCurrent(@InjectService(filter = "(component.name=AssetTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		EventHandler notificationHandler = notificationHandlerAware.getService();
		Map<String, Object> eventProps = new HashMap<String, Object>();
		Asset assetNew = NotaryFactory.eINSTANCE.createAsset();
		assetNew.setId("test");
		eventProps.put("new", assetNew);
		Asset assetCurrent = EcoreUtil.copy(assetNew);
		eventProps.put("current", assetCurrent);
		
		ArgumentCaptor<Asset> assetNewC = ArgumentCaptor.forClass(Asset.class);
		ArgumentCaptor<Asset> assetCurrentC = ArgumentCaptor.forClass(Asset.class);
		Mockito.doNothing().when(entryService).createAssetModificationTransaction(assetCurrentC.capture(), assetNewC.capture());
		
		notificationHandler.handleEvent(new Event("test", eventProps));
		
		assertEquals(assetNew, assetNewC.getValue());
		assertEquals(assetCurrent, assetCurrentC.getValue());
		
		Mockito.verify(entryService, Mockito.times(1)).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
	}
	
}

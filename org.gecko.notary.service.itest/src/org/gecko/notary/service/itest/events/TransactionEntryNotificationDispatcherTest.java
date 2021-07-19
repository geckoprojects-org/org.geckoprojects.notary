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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetTransaction;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.Contact;
import org.gecko.notary.model.notary.ContactType;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.model.notary.TransactionNotification;
import org.gecko.notary.model.notary.TransactionType;
import org.gecko.notary.service.api.TransactionNotificationService;
import org.gecko.notary.service.api.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
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
public class TransactionEntryNotificationDispatcherTest {
	
	/**
	 * Register Mock Services to fulfill the reference constraints for the TransactionNotificationDispatcher component
	 * @param bc the {@link BundleContext}
	 */
	@BeforeEach
	void beforeEach(@InjectBundleContext BundleContext bc) {
		TransactionService transactionService = mock(TransactionService.class);
		Dictionary<String, Object> transactionProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		bc.registerService(TransactionService.class, transactionService, transactionProperties);
		
		TransactionNotificationService notificationService = mock(TransactionNotificationService.class);
		Dictionary<String, Object> notificationProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		bc.registerService(TransactionNotificationService.class, notificationService, notificationProperties);
		
		EventAdmin eventAdmin = mock(EventAdmin.class);
		Dictionary<String, Object> eaProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		bc.registerService(EventAdmin.class, eventAdmin, eaProperties);
		
		
	}
	
	@Test
	void testEventHandler(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware) {
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
	}
	
	@Test
	void testDispatcher_Empty(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", new HashMap<String, Object>()));
		
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_NoTransactionId(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenThrow(IllegalStateException.class);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_WithTransactionId(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("transactionId");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenThrow(IllegalStateException.class);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}

	@Test
	public void testDispatcher_NoTransaction(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(null);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}

	@Test
	public void testDispatcher_TransactionNoParticipant(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("test");
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenThrow(IllegalStateException.class);
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(notificationService, Mockito.times(1)).getNotificationsByTransaction(Mockito.nullable(String.class), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}

	@Test
	public void testDispatcher_NoNotification(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId("part1");
		t.setId("test");
		
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(notificationService, Mockito.times(1)).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}

	@Test
	public void testDispatcher_NullNotification(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("test");
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(notificationService, Mockito.times(1)).getNotificationsByTransaction(Mockito.nullable(String.class), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_NullTransactionId(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_OneNotificationNoContact(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId("part1");
		t.setId("test");
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
		TransactionNotification not1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		List<TransactionNotification> nots = new ArrayList<TransactionNotification>();
		nots.add(not1);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(nots);

		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		Mockito.verify(notificationService, Mockito.times(1)).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_AssetEntryNoParticipant(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		ate.setAsset(asset);
		Map<String, Object> eventProps = Collections.singletonMap("entry", ate);
		
		Mockito.when(transactionService.getTransactionsByType(Mockito.anyString(), Mockito.any(TransactionType.class))).thenThrow(IllegalStateException.class);
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_AssetEntryNoTransaction(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setCreatorId("test");
		ate.setAsset(asset);
		Map<String, Object> eventProps = Collections.singletonMap("entry", ate);
		
		Mockito.when(transactionService.getTransactionsByType(Mockito.anyString(), Mockito.any(TransactionType.class))).thenReturn(Collections.emptyList());
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_AssetEntryNoAsset(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate.setParticipantId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", ate);
		
		Mockito.when(transactionService.getTransactionsByType(Mockito.anyString(), Mockito.any(TransactionType.class))).thenReturn(Collections.emptyList());
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		Mockito.verify(transactionService, Mockito.never()).getTransactionsByType(Mockito.anyString(), Mockito.any(TransactionType.class));
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_AssetEntryOneNotification(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setCreatorId("test");
		ate.setAsset(asset);
		ate.setChangeType(AssetChangeType.OWNERSHIP);
		Map<String, Object> eventProps = Collections.singletonMap("entry", ate);
		
		AssetTransaction t = NotaryFactory.eINSTANCE.createAssetTransaction();
		t.setChangeType(AssetChangeType.OWNERSHIP);
		t.setParticipantId("part1");
		t.setId("test");
		Mockito.when(transactionService.getTransactionsByType(Mockito.nullable(String.class), Mockito.any(TransactionType.class))).thenReturn(Collections.singletonList(t));
		
		TransactionNotification not1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		Contact c = NotaryFactory.eINSTANCE.createContact();
		c.setType(ContactType.EMAIL);
		not1.setContact(c);
		List<TransactionNotification> nots = new ArrayList<TransactionNotification>();
		nots.add(not1);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(nots);
		
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("notification"));
		assertEquals(not1, event.getProperty("notification"));
		assertTrue(event.containsProperty("entry"));
		assertEquals(ate, event.getProperty("entry"));
		assertEquals("notification/EMAIL", event.getTopic());
		
		Mockito.verify(notificationService, Mockito.times(1)).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.times(1)).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_AssetEntryNoNotification(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setCreatorId("test");
		ate.setAsset(asset);
		Map<String, Object> eventProps = Collections.singletonMap("entry", ate);
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId("part1");
		Mockito.when(transactionService.getTransactionsByType(Mockito.anyString(), Mockito.any(TransactionType.class))).thenReturn(Collections.singletonList(t));
		
		TransactionNotification not1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		Contact c = NotaryFactory.eINSTANCE.createContact();
		c.setType(ContactType.EMAIL);
		not1.setContact(c);
		List<TransactionNotification> nots = new ArrayList<TransactionNotification>();
		nots.add(not1);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(nots);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_AssetEntryDifferentChangeTypeNoNotification(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		AssetTransactionEntry ate = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setCreatorId("test");
		ate.setAsset(asset);
		ate.setChangeType(AssetChangeType.CREATION);
		Map<String, Object> eventProps = Collections.singletonMap("entry", ate);
		
		AssetTransaction t = NotaryFactory.eINSTANCE.createAssetTransaction();
		t.setParticipantId("part1");
		t.setChangeType(AssetChangeType.OWNERSHIP);
		Mockito.when(transactionService.getTransactionsByType(Mockito.anyString(), Mockito.any(TransactionType.class))).thenReturn(Collections.singletonList(t));
		
		TransactionNotification not1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		Contact c = NotaryFactory.eINSTANCE.createContact();
		c.setType(ContactType.EMAIL);
		not1.setContact(c);
		List<TransactionNotification> nots = new ArrayList<TransactionNotification>();
		nots.add(not1);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(nots);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		Mockito.verify(notificationService, Mockito.never()).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_OneNotification(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId("part1");
		t.setId("test");
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
		
		TransactionNotification not1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		Contact c = NotaryFactory.eINSTANCE.createContact();
		c.setType(ContactType.EMAIL);
		not1.setContact(c);
		List<TransactionNotification> nots = new ArrayList<TransactionNotification>();
		nots.add(not1);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(nots);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("notification"));
		assertEquals(not1, event.getProperty("notification"));
		assertTrue(event.containsProperty("entry"));
		assertEquals(te, event.getProperty("entry"));
		assertEquals("notification/EMAIL", event.getTopic());
		
		Mockito.verify(notificationService, Mockito.times(1)).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.times(1)).postEvent(Mockito.any(Event.class));
	}
	
	@Test
	public void testDispatcher_MultiNotifications(@InjectService(filter = "(component.name=TransactionNotificationDispatcher)")ServiceAware<EventHandler> dispatcherHandlerAware, 
			@InjectService(service = TransactionService.class) TransactionService transactionService,
			@InjectService(service = TransactionNotificationService.class) TransactionNotificationService notificationService,
			@InjectService(service = EventAdmin.class)EventAdmin eventAdmin) {
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setTransactionId("test");
		Map<String, Object> eventProps = Collections.singletonMap("entry", te);
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId("part1");
		t.setId("test");
		Mockito.when(transactionService.getTransactionById(Mockito.anyString())).thenReturn(t);
		TransactionNotification not1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setType(ContactType.EMAIL);
		not1.setContact(c1);
		TransactionNotification not2 = NotaryFactory.eINSTANCE.createTransactionNotification();
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setType(ContactType.APP);
		not2.setContact(c2);
		List<TransactionNotification> nots = new ArrayList<TransactionNotification>();
		nots.add(not1);
		nots.add(not2);
		Mockito.when(notificationService.getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString())).thenReturn(nots);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		
		assertThat(dispatcherHandlerAware.getServices()).hasSize(1);
		EventHandler dispatcherHandler = dispatcherHandlerAware.getService();
		dispatcherHandler.handleEvent(new Event("test", eventProps));
		
		assertEquals(2, eventC.getAllValues().size());
		Event event = eventC.getAllValues().get(0);
		assertTrue(event.containsProperty("notification"));
		assertEquals(not1, event.getProperty("notification"));
		assertTrue(event.containsProperty("entry"));
		assertEquals(te, event.getProperty("entry"));
		assertEquals("notification/EMAIL", event.getTopic());
		
		event = eventC.getAllValues().get(1);
		assertTrue(event.containsProperty("notification"));
		assertEquals(not2, event.getProperty("notification"));
		assertTrue(event.containsProperty("entry"));
		assertEquals(te, event.getProperty("entry"));
		assertEquals("notification/APP", event.getTopic());
		
		Mockito.verify(notificationService, Mockito.times(1)).getNotificationsByTransaction(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(eventAdmin, Mockito.times(2)).postEvent(Mockito.any(Event.class));
	}
	
}

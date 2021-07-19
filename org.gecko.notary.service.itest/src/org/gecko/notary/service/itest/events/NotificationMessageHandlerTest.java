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
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Notification;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.model.notary.TransactionNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
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
public class NotificationMessageHandlerTest  {
	
	public interface QueryRepositoryMock extends EMFRepository, QueryRepository {

	}
	
	@BeforeEach
	private void beforeEach(@InjectBundleContext BundleContext bc) throws InvalidSyntaxException {
		QueryRepositoryMock repository = mock(QueryRepositoryMock.class);
		Dictionary<String, Object> properties = Dictionaries.dictionaryOf("repo_id", "diamant.diamant");
		bc.registerService(EMFRepository.class, new PrototypeServiceFactory<EMFRepository>() {
	
			@Override
			public EMFRepository getService(Bundle bundle, ServiceRegistration<EMFRepository> registration) {
				return repository;
			}
	
			@Override
			public void ungetService(Bundle bundle, ServiceRegistration<EMFRepository> registration, EMFRepository service) {
				repository.dispose();
			}
		}, properties);
	}

	@Test
	public void testEmptyEvent(@InjectService(filter = "(component.name=NotificationMessageHandler)")ServiceAware<EventHandler> notificationHandlerAware,
			@InjectService(service = EMFRepository.class) ServiceAware<EMFRepository> repoAware) {
		assertThat(repoAware.getServices()).hasSize(1);
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		
		EMFRepository repository = repoAware.getService();
		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", new HashMap<String, Object>()));
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testEmptyEvent_Entry(@InjectService(filter = "(component.name=NotificationMessageHandler)")ServiceAware<EventHandler> notificationHandlerAware,
			@InjectService(service = EMFRepository.class) ServiceAware<EMFRepository> repoAware) {
		assertThat(repoAware.getServices()).hasSize(1);
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		TransactionEntry entry = NotaryFactory.eINSTANCE.createTransactionEntry();
		Map<String, Object> eventProps = Collections.singletonMap("entry", entry);
		
		EMFRepository repository = repoAware.getService();
		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testEmptyEvent_Notification(@InjectService(filter = "(component.name=NotificationMessageHandler)")ServiceAware<EventHandler> notificationHandlerAware,
			@InjectService(service = EMFRepository.class) ServiceAware<EMFRepository> repoAware) {
		assertThat(repoAware.getServices()).hasSize(1);
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		TransactionNotification not = NotaryFactory.eINSTANCE.createTransactionNotification();
		Map<String, Object> eventProps = Collections.singletonMap("notification", not);
		
		EMFRepository repository = repoAware.getService();
		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testEmptyEvent_NoTransactionRef(@InjectService(filter = "(component.name=NotificationMessageHandler)")ServiceAware<EventHandler> notificationHandlerAware,
			@InjectService(service = EMFRepository.class) ServiceAware<EMFRepository> repoAware) {
		assertThat(repoAware.getServices()).hasSize(1);
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		Map<String, Object> eventProps = new HashMap<String, Object>();
		TransactionEntry entry = NotaryFactory.eINSTANCE.createTransactionEntry();
		eventProps.put("entry", entry);
		TransactionNotification not = NotaryFactory.eINSTANCE.createTransactionNotification();
		eventProps.put("notification", not);
		
		EMFRepository repository = repoAware.getService();
		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testEmptyEvent_Save(@InjectService(filter = "(component.name=NotificationMessageHandler)")ServiceAware<EventHandler> notificationHandlerAware,
			@InjectService(service = EMFRepository.class) ServiceAware<EMFRepository> repoAware) {
		assertThat(repoAware.getServices()).hasSize(1);
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		Map<String, Object> eventProps = new HashMap<String, Object>();
		TransactionEntry entry = NotaryFactory.eINSTANCE.createTransactionEntry();
		entry.setParticipantId("MsSender");
		eventProps.put("entry", entry);
		TransactionNotification not = NotaryFactory.eINSTANCE.createTransactionNotification();
		not.setContent("my test content");
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setDescription("Test description");
		t.setParticipantId("MrTarget");
		not.setTransaction(t);
		eventProps.put("notification", not);
		
		EMFRepository repository = repoAware.getService();
		ArgumentCaptor<Notification> notC = ArgumentCaptor.forClass(Notification.class);
		Mockito.doNothing().when(repository).save(notC.capture());
		
		
		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", eventProps));
		
		assertNotNull(notC.getValue());
		Notification notification = notC.getValue();
		assertNotNull(notification.getTimestamp());
		assertEquals(entry.getParticipantId(), notification.getSenderId());
		assertEquals(t.getParticipantId(), notification.getReceipientId());
		assertEquals(t.getDescription(), notification.getSubject());
		assertEquals(not.getContent(), notification.getContent());
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}
	
}

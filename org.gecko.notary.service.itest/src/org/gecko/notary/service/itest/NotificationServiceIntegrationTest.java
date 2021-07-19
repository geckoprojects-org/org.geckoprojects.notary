/**
 * Copyright (c) 2012 - 2018 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.itest;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Notification;
import org.gecko.notary.service.api.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
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
public class NotificationServiceIntegrationTest {
	
	public interface QueryRepositoryMock extends EMFRepository, QueryRepository {

	}
	
	private IQueryBuilder builder;
	private IQuery query;
	
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("repo_id", "notary.notary");
		QueryRepositoryMock repository = mock(QueryRepositoryMock.class);
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
		
		builder = mock(IQueryBuilder.class);
		query = mock(IQuery.class);
	}

	@Test
	public void testNotificationMarkRead_NullNotificationId(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.markRead(null, false);
		});
	}
	
	@Test
	public void testNotificationMarkRead_NoNotification(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		assertNull(notificationService.markRead("test", false));
	}
	
	@Test
	public void testNotificationMarkRead_NoChange(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		Notification n = NotaryFactory.eINSTANCE.createNotification();
		n.setId("test");
		n.setRead(false);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(n);
		Notification result = notificationService.markRead("test", false);
		assertNotNull(result);
		assertEquals(n, result);
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testNotificationMarkRead_Change(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		Notification n = NotaryFactory.eINSTANCE.createNotification();
		n.setId("test");
		n.setRead(false);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(n);
		Notification result = notificationService.markRead("test", true);
		assertNotNull(result);
		assertEquals(n, result);
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testNotificationSender_NoOwnerNoType(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.getNotificationsBySender(null);
		});
	}
	
	@Test
	public void testNotificationSender_Result(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any(Object.class))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		List<EObject> nl = new ArrayList<>();
		Notification not1 = NotaryFactory.eINSTANCE.createNotification();
		not1.setId("not1");
		Notification not2 = NotaryFactory.eINSTANCE.createNotification();
		not2.setId("not2");
		nl.add(not1);
		nl.add(not2);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class))).thenReturn(nl);
		List<Notification> result = notificationService.getNotificationsBySender("test");
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(not1, result.get(0));
		assertEquals(not2, result.get(1));
	}
	
	@Test
	public void testNotificationReceiver_NoOwnerNoType(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.getNotificationsByReceiver(null);
		});
	}
	
	@Test
	public void testNotificationReceiver_Result(@InjectService NotificationService notificationService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(notificationService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any(Object.class))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		List<EObject> nl = new ArrayList<EObject>();
		Notification not1 = NotaryFactory.eINSTANCE.createNotification();
		not1.setId("not1");
		Notification not2 = NotaryFactory.eINSTANCE.createNotification();
		not2.setId("not2");
		nl.add(not1);
		nl.add(not2);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class))).thenReturn(nl);
		List<Notification> result = notificationService.getNotificationsBySender("test");
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(not1, result.get(0));
		assertEquals(not2, result.get(1));
	}
	
}

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
package org.gecko.notary.service.itest;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.notary.model.notary.Contact;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionNotification;
import org.gecko.notary.service.api.TransactionNotificationService;
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
public class TransactionNotificationServiceIntegrationTest {

	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {

		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("repo_id", "notary.notary");
		EMFRepository repository = mock(EMFRepository.class);
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
	public void testRemoveNotification_NoParticipant(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		assertFalse(notificationService.removeNotification(null, null));
	}

	@Test
	public void testRemoveNotification_NoNotificationId(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		assertFalse(notificationService.removeNotification("test", null));
	}

	@Test
	public void testRemoveNotification_UnknownParticipant(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.removeNotification("test", "t1");
		});
	}

	@Test
	public void testRemoveNotifications_NoNotificationFound(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		TransactionNotification t1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		t1.setId("t1");
		TransactionNotification t2 = NotaryFactory.eINSTANCE.createTransactionNotification();
		t2.setId("t2");
		update.getNotification().add(t1);
		update.getNotification().add(t2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertEquals(2, update.getNotification().size());

		assertFalse(notificationService.removeNotification(ID, "t3"));

		assertEquals(2, update.getNotification().size());

		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}

	@Test
	public void testRemoveNotifications(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);

		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		TransactionNotification t1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		t1.setId("t1");
		TransactionNotification t2 = NotaryFactory.eINSTANCE.createTransactionNotification();
		t2.setId("t2");
		update.getNotification().add(t1);
		update.getNotification().add(t2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertEquals(2, update.getNotification().size());

		assertTrue(notificationService.removeNotification(ID, "t1"));

		assertEquals(1, update.getNotification().size());
		assertEquals(t2, update.getNotification().get(0));

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testGetNotifications_NoParticipant(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.getNotifications(null);
		});
	}

	@Test
	public void testGetNotifications_ParticipantNotExist(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		assertNotNull(notificationService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.getNotifications("test");
		});
	}

	@Test
	public void testGetNotifications_Exist(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		TransactionNotification tn1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn1.setId("n1");
		TransactionNotification tn2 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn2.setId("n2");
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		def.getNotification().add(tn1);
		def.getNotification().add(tn2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);

		List<TransactionNotification> transactions = notificationService.getNotifications(ID);
		assertNotNull(transactions);
		assertEquals(2, transactions.size());
	}

	@Test
	public void testGetNotificationsByTransaction_Exist(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		String ID = "1222";
		String NAME = "test";
		String TRANSACTION = "tn1";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		TransactionNotification tn1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn1.setId("n1");
		tn1.setTransactionId(TRANSACTION);
		TransactionNotification tn2 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn2.setId("n2");
		tn2.setTransactionId("tn2");
		TransactionNotification tn3 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn3.setId("n3");
		tn3.setTransactionId(TRANSACTION);
		TransactionNotification tn4 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn4.setId("n4");
		tn4.setTransactionId("tn3");
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		def.getNotification().add(tn1);
		def.getNotification().add(tn2);
		def.getNotification().add(tn3);
		def.getNotification().add(tn4);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);
		
		List<TransactionNotification> transactions = notificationService.getNotificationsByTransaction(ID, TRANSACTION);
		assertNotNull(transactions);
		assertEquals(2, transactions.size());
		assertEquals(tn3, transactions.stream().filter(n->n.getId().equals("n3")).findFirst().get());
		assertEquals(tn1, transactions.stream().filter(n->n.getId().equals("n1")).findFirst().get());
	}

	@Test
	public void testGetNotifications_ExistNoContent(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);

		List<TransactionNotification> transactions = notificationService.getNotifications(ID);
		assertTrue(transactions.isEmpty());
	}

	@Test
	public void testUpdateNotification_NoParticipant(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		assertNull(notificationService.updateNotification(null, null));
	}

	@Test
	public void testUpdateNotifications_NoNotification(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		assertNull(notificationService.updateNotification("test", null));
	}

	@Test
	public void testUpdateNotifications_UnknownParticipant(@InjectService TransactionNotificationService notificationService,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(repository);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		TransactionNotification t = NotaryFactory.eINSTANCE.createTransactionNotification();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.updateNotification("test", t);
		});
	}

	@Test
	public void testUpdateNotifications_NoRefIds(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);

		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertTrue(update.getNotification().isEmpty());

		TransactionNotification t = NotaryFactory.eINSTANCE.createTransactionNotification();
		t.setId("t1");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.updateNotification(ID, t);
		});
	}

	@Test
	public void testUpdateNotifications_RefIdsNotFound(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertTrue(update.getNotification().isEmpty());

		TransactionNotification t = NotaryFactory.eINSTANCE.createTransactionNotification();
		t.setId("t1");
		t.setContactId("c1");
		t.setTransactionId("t1");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.updateNotification(ID, t);
		});
	}

	@Test
	public void testUpdateNotifications_NoTransaction(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		update.getContact().add(c1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertTrue(update.getNotification().isEmpty());

		TransactionNotification t = NotaryFactory.eINSTANCE.createTransactionNotification();
		t.setId("t1");
		t.setContactId("c1");
		t.setTransactionId("t1");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.updateNotification(ID, t);
		});
	}

	@Test
	public void testUpdateNotifications_NoContact(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c2");
		update.getContact().add(c1);
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		update.getTransaction().add(t1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertTrue(update.getNotification().isEmpty());

		TransactionNotification t = NotaryFactory.eINSTANCE.createTransactionNotification();
		t.setId("t1");
		t.setContactId("c1");
		t.setTransactionId("t1");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			notificationService.updateNotification(ID, t);
		});
	}

	@Test
	public void testUpdateNotifications(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		update.getContact().add(c1);
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		update.getTransaction().add(t1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertTrue(update.getNotification().isEmpty());

		TransactionNotification t = NotaryFactory.eINSTANCE.createTransactionNotification();
		t.setId("t1");
		t.setContactId("c1");
		t.setTransactionId("t1");
		notificationService.updateNotification(ID, t);

		assertEquals(1, update.getNotification().size());
		assertEquals(update.getNotification().get(0), t);

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateNotifications_DifferentTransaction(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		update.getContact().add(c1);
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		update.getContact().add(c2);
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		update.getTransaction().add(t1);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		update.getTransaction().add(t2);
		TransactionNotification tn1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn1.setId("tn1");
		tn1.setContactId("c1");
		tn1.setTransactionId("t1");
		update.getNotification().add(tn1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertEquals(1, update.getNotification().size());

		TransactionNotification tn = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn.setId("tn1");
		tn.setContactId("c1");
		tn.setTransactionId("t2");
		notificationService.updateNotification(ID, tn);

		assertEquals(1, update.getNotification().size());
		TransactionNotification notification = update.getNotification().get(0);
		assertEquals(tn, notification);
		assertEquals(t2, notification.getTransaction());
		assertEquals(t2.getId(), notification.getTransactionId());
		assertEquals(c1, notification.getContact());
		assertEquals(c1.getId(), notification.getContactId());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateNotifications_DifferentContact(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		update.getContact().add(c1);
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		update.getContact().add(c2);
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		update.getTransaction().add(t1);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		update.getTransaction().add(t2);
		TransactionNotification tn1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn1.setId("tn1");
		tn1.setContactId("c1");
		tn1.setTransactionId("t1");
		update.getNotification().add(tn1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertEquals(1, update.getNotification().size());

		TransactionNotification tn = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn.setId("tn1");
		tn.setContactId("c2");
		tn.setTransactionId("t1");
		notificationService.updateNotification(ID, tn);

		assertEquals(1, update.getNotification().size());
		TransactionNotification notification = update.getNotification().get(0);
		assertEquals(tn, notification);
		assertEquals(t1, notification.getTransaction());
		assertEquals(t1.getId(), notification.getTransactionId());
		assertEquals(c2, notification.getContact());
		assertEquals(c2.getId(), notification.getContactId());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateNotifications_NoChange(@InjectService TransactionNotificationService notificationService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(notificationService);
		assertNotNull(rs);
		assertNotNull(repository);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		update.getContact().add(c1);
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		update.getContact().add(c2);
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		update.getTransaction().add(t1);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		update.getTransaction().add(t2);
		TransactionNotification tn1 = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn1.setId("tn1");
		tn1.setContactId("c1");
		tn1.setContact(c1);
		tn1.setTransaction(t1);
		tn1.setTransactionId("t1");

		update.getNotification().add(tn1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);

		assertEquals(1, update.getNotification().size());

		TransactionNotification tn = NotaryFactory.eINSTANCE.createTransactionNotification();
		tn.setId("tn1");
		tn.setContactId("c1");
		tn.setTransaction(t1);
		tn.setTransactionId("t1");
		notificationService.updateNotification(ID, tn);

		assertEquals(1, update.getNotification().size());
		TransactionNotification notification = update.getNotification().get(0);
		assertEquals(tn1, notification);
		assertEquals(t1, notification.getTransaction());
		assertEquals(t1.getId(), notification.getTransactionId());
		assertEquals(c1, notification.getContact());
		assertEquals(c1.getId(), notification.getContactId());

		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}

}

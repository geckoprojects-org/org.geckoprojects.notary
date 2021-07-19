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
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionType;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
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
public class TransactionServiceIntegrationTest {
	
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		Dictionary<String, Object> participantProperties = new Hashtable<String, Object>();
		participantProperties.put(Constants.SERVICE_RANKING, 1000);
		ParticipantService participantService = mock(ParticipantService.class);
		bc.registerService(ParticipantService.class, new PrototypeServiceFactory<ParticipantService>() {
			
			@Override
			public ParticipantService getService(Bundle bundle, ServiceRegistration<ParticipantService> registration) {
				return participantService;
			}
			
			@Override
			public void ungetService(Bundle bundle, ServiceRegistration<ParticipantService> registration, ParticipantService service) {
			}
		}, participantProperties);
		EMFRepository repository = mock(EMFRepository.class);
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("repo_id", "notary.notary");
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
	public void testRemoveTransaction_NoParticipant(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		assertFalse(transactionService.removeTransaction(null, null));
	}
	
	@Test
	public void testRemoveTransaction_NoTransactionId(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		assertFalse(transactionService.removeTransaction("test", null));
	}
	
	@Test
	public void testRemoveTransaction_UnknownParticipant(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.removeTransaction("test", "t1");
		});
	}
	
	@Test
	public void testRemoveTransactions_NoTransactionFound(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		update.getTransaction().add(t1);
		update.getTransaction().add(t2);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(update);
		
		assertEquals(2, update.getTransaction().size());
		
		assertFalse(transactionService.removeTransaction(ID, "t3"));
		
		assertEquals(2, update.getTransaction().size());
		
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testRemoveTransaction_NotSameParticipants(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		update.getTransaction().add(t1);
		update.getTransaction().add(t2);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(update);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(t1);
		
		assertEquals(2, update.getTransaction().size());
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.removeTransaction(ID, "t1");
		});
	}
	
	@Test
	public void testRemoveTransaction(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setParticipantId(ID);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		update.getTransaction().add(t1);
		update.getTransaction().add(t2);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(update);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(t1);
		
		assertEquals(2, update.getTransaction().size());
		
		assertTrue(transactionService.removeTransaction(ID, "t1"));
		
		assertEquals(1, update.getTransaction().size());
		assertEquals(t2, update.getTransaction().get(0));
		
		Mockito.verify(participantService, Mockito.times(1)).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
		Mockito.verify(repository, Mockito.times(1)).delete(Mockito.any(EObject.class));
	}
	
	@Test
	public void testUpdateTransaction_NoParticipant(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assertNull(transactionService.updateTransaction(null, null));
		});
	}
	
	@Test
	public void testUpdateTransactions_NoTransaction(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.updateTransaction("test", null);
		});
	}
	
	@Test
	public void testUpdateTransactions_UnknownParticipant(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(null);
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId("test");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.updateTransaction("test", t);
		});
	}
	
	@Test
	public void testUpdateTransactions(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(update);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(null);
		
		assertTrue(update.getTransaction().isEmpty());
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("t1");
		t.setParticipantId(ID);
		transactionService.updateTransaction(ID, t);
		
		assertEquals(1, update.getTransaction().size());
		assertEquals(update.getTransaction().get(0), t);
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.times(1)).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
	}
	
	@Test
	public void testUpdateTransactions_NoParticipantIdInTransaction(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(update);
		
		assertTrue(update.getTransaction().isEmpty());
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.updateTransaction(ID, t);
		});
	}
	
	@Test
	public void testUpdateTransactions_NotSameParticipantIdInTransaction(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(update);
		
		assertTrue(update.getTransaction().isEmpty());
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId("test");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.updateTransaction(ID, t);
		});
	}
	
	@Test
	public void testUpdateTransactions_NewTransaction(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(update);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(null);
		
		assertTrue(update.getTransaction().isEmpty());
		
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setParticipantId(ID);
		Transaction result = transactionService.updateTransaction(ID, t);
		
		assertEquals(1, update.getTransaction().size());
		assertEquals(update.getTransaction().get(0), t);
		assertEquals(update.getTransaction().get(0), result);
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.times(1)).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
	}

	@Test
	public void testUpdateTransactions_Existing(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.GENESIS);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		t2.setType(TransactionType.LOGISTICS_OUT);
		update.getTransaction().add(t1);
		update.getTransaction().add(t2);
		assertEquals(TransactionType.LOGISTICS_OUT, update.getTransaction().get(1).getType());
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(update);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(t2);
		
		assertEquals(2, update.getTransaction().size());
		assertEquals("t2", update.getTransaction().get(1).getId());
		
		Transaction t3 = NotaryFactory.eINSTANCE.createTransaction();
		t3.setId("t2");
		t3.setParticipantId(ID);
		t3.setType(TransactionType.OPERATION_REPORT);
		
		transactionService.updateTransaction(ID, t3);
		
		assertEquals(2, update.getTransaction().size());
		assertEquals("t2", update.getTransaction().get(1).getId());
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.never()).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
	}
	
	@Test
	public void testUpdateTransactions_ExistingNoChange(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
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
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.GENESIS);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		t2.setParticipantId(ID);
		t2.setType(TransactionType.LOGISTICS_OUT);
		update.getTransaction().add(t1);
		update.getTransaction().add(t2);
		assertEquals(TransactionType.LOGISTICS_OUT, update.getTransaction().get(1).getType());
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(update);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(t2);
		
		assertEquals(2, update.getTransaction().size());
		assertEquals("t2", update.getTransaction().get(1).getId());
		
		Transaction t3 = NotaryFactory.eINSTANCE.createTransaction();
		t3.setId("t2");
		t3.setParticipantId(ID);
		t3.setType(TransactionType.LOGISTICS_OUT);
		
		transactionService.updateTransaction(ID, t3);
		
		assertEquals(2, update.getTransaction().size());
		assertEquals(t2, update.getTransaction().get(1));
		
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.never()).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
	}
	
	@Test
	public void testUpdateTransactions_NoParticipantTransaction(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.updateTransaction(null, t);
		});
	}
	
	@Test
	public void testGetTransactions_NoParticipant(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenThrow(new IllegalStateException());
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getTransactions(null);
		});
	}
	
	@Test
	public void testGetTransactions_ParticipantNotExist(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenThrow(new IllegalStateException());
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getTransactions("test");
		});
	}
	
	@Test
	public void testGetTransactions_Exist(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("c1");
		t1.setType(TransactionType.GENESIS);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("c2");
		t2.setType(TransactionType.LOGISTICS_IN);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		def.getTransaction().add(t1);
		def.getTransaction().add(t2);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		
		List<Transaction> transactions = transactionService.getTransactions(ID);
		assertNotNull(transactions);
		assertEquals(2, transactions.size());
	}
	
	@Test
	public void testGetTransactions_ExistNoContent(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		
		List<Transaction> transactions = transactionService.getTransactions(ID);
		assertTrue(transactions.isEmpty());
	}
	
	@Test
	public void testGetTransactionById_NullId(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getTransactionById(null);
		});
	}
	
	@Test
	public void testGetTransactionById_NoResult(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(null);
		assertNull(transactionService.getTransactionById("test"));
	}
	
	@Test
	public void testGetTransactionById_Result(@InjectService ParticipantService participantService,
			@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(participantService);
		assertNotNull(repository);
		Transaction t = NotaryFactory.eINSTANCE.createTransaction();
		t.setId("test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any(), Mockito.anyMap())).thenReturn(t);
		assertEquals(t, transactionService.getTransactionById("test"));
	}
	
}

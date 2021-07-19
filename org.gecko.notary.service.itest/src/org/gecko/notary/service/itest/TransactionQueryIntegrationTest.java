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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.Feedback;
import org.gecko.notary.model.notary.FeedbackTransaction;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionType;
import org.gecko.notary.service.api.TransactionService;
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
public class TransactionQueryIntegrationTest {
	
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
	public void testTransactionsByType_NoTypeNullParticipant(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getTransactionsByType(null, null);
		});
	}
	
	@Test
	public void testTransactionsByType_NullParticipant(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getTransactionsByType(null, TransactionType.LOGISTICS_IN);
		});
	}
	
	@Test
	public void testTransactionsByType_NoTypeResult(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepositoryMock);
		QueryRepositoryMock queryRepository = (QueryRepositoryMock) repository;
		
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.FEEDBACK);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		t2.setType(TransactionType.GENESIS);
		List<EObject> tl = new ArrayList<EObject>(3);
		tl.add(t1);
		tl.add(t2);
		Feedback f = NotaryFactory.eINSTANCE.createFeedback();
		tl.add(f);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(tl);
		List<Transaction> transactions = transactionService.getTransactionsByType("test", null);
		assertEquals(2, transactions.size());
		assertEquals(1, transactions.stream().filter(t->t.getId().equals("t1")).count());
		assertEquals(1, transactions.stream().filter(t->t.getId().equals("t2")).count());
	}
	
	@Test
	public void testTransactionsByType_Result(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepositoryMock);
		QueryRepositoryMock queryRepository = (QueryRepositoryMock) repository;
		
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.FEEDBACK);
		List<EObject> tl = new ArrayList<EObject>(3);
		tl.add(t1);
		Feedback f = NotaryFactory.eINSTANCE.createFeedback();
		tl.add(f);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.and(Mockito.any(IQuery.class), Mockito.any(IQuery.class))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		AtomicInteger ai = new AtomicInteger();
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).then((m)->{
			if (ai.getAndIncrement() == 0) {
				return tl;
			} else {
				return Collections.emptyList();
			}
		});
		List<Transaction> transactions = transactionService.getTransactionsByType("test", TransactionType.FEEDBACK);
		assertEquals(1, transactions.size());
		assertEquals(1, transactions.stream().filter(t->t.getId().equals("t1")).count());
		transactions = transactionService.getTransactionsByType("test", TransactionType.LOGISTICS_IN);
		assertTrue(transactions.isEmpty());
	}
	
	@Test
	public void testSharedTransactions_NoTypeNullParticipant(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getSharedTransactions(null, null);
		});
	}
	
	@Test
	public void testSharedTransactions_NoType(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepositoryMock);
		QueryRepositoryMock queryRepository = (QueryRepositoryMock) repository;
		
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.FEEDBACK);
		t1.setShare(true);
		Transaction t2 = NotaryFactory.eINSTANCE.createTransaction();
		t2.setId("t2");
		t2.setType(TransactionType.FEEDBACK);
		t2.setShare(true);
		List<EObject> tl = new ArrayList<EObject>(3);
		tl.add(t1);
		tl.add(t2);
		Feedback f = NotaryFactory.eINSTANCE.createFeedback();
		tl.add(f);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.and(Mockito.any(IQuery.class), Mockito.any(IQuery.class))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(tl);
		List<Transaction> transactions = transactionService.getSharedTransactions("test", null);
		assertEquals(2, transactions.size());
		assertEquals(1, transactions.stream().filter(t->t.getId().equals("t1")).count());
		assertEquals(1, transactions.stream().filter(t->t.getId().equals("t2")).count());
	}
	
	@Test
	public void testSharedTransactions_Type(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepositoryMock);
		QueryRepositoryMock queryRepository = (QueryRepositoryMock) repository;
		
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.FEEDBACK);
		t1.setShare(true);
		List<EObject> tl = new ArrayList<EObject>(3);
		tl.add(t1);
		Feedback f = NotaryFactory.eINSTANCE.createFeedback();
		tl.add(f);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		List<IQuery> queries = new ArrayList<IQuery>();
		queries.add(Mockito.any(IQuery.class));
		queries.add(Mockito.any(IQuery.class));
		queries.add(Mockito.any(IQuery.class));
		Mockito.when(builder.and(queries.toArray(new IQuery[3]))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		AtomicInteger ai = new AtomicInteger();
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).then((m)->{
			if (ai.getAndIncrement() == 0) {
				return tl;
			} else {
				return Collections.emptyList();
			}
		});
		
		List<Transaction> transactions = transactionService.getSharedTransactions("test", TransactionType.FEEDBACK);
		assertEquals(1, transactions.size());
		assertEquals(1, transactions.stream().filter(t->t.getId().equals("t1")).count());
		transactions = transactionService.getSharedTransactions("test", TransactionType.LOGISTICS_IN);
		assertTrue(transactions.isEmpty());
	}
	
	@Test
	public void testSharedTransactions_ThrowNPE(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepositoryMock);
		QueryRepositoryMock queryRepository = (QueryRepositoryMock) repository;
		
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.FEEDBACK);
		List<EObject> tl = new ArrayList<EObject>(3);
		tl.add(t1);
		Feedback f = NotaryFactory.eINSTANCE.createFeedback();
		tl.add(f);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.and(Mockito.any(IQuery.class), Mockito.any(IQuery.class))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenThrow(new NullPointerException());
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getSharedTransactions("test", null);
		});
	}
	
	@Test
	public void testTransactionsByType_ThrowNPE(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepositoryMock);
		QueryRepositoryMock queryRepository = (QueryRepositoryMock) repository;
		
		Transaction t1 = NotaryFactory.eINSTANCE.createTransaction();
		t1.setId("t1");
		t1.setType(TransactionType.FEEDBACK);
		List<EObject> tl = new ArrayList<EObject>(3);
		tl.add(t1);
		Feedback f = NotaryFactory.eINSTANCE.createFeedback();
		tl.add(f);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.and(Mockito.any(IQuery.class), Mockito.any(IQuery.class))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenThrow(new NullPointerException());
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.getTransactionsByType("test", TransactionType.FEEDBACK);
		});
	}
	
	@Test
	public void testTransactionsByType_NoTypeEmptyResult(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepositoryMock);
		QueryRepositoryMock queryRepository = (QueryRepositoryMock) repository;
		
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(Collections.emptyList());
		List<Transaction> transactions = transactionService.getTransactionsByType("test", null);
		assertTrue(transactions.isEmpty());
	}
	
	@Test
	public void testSimple_NullPartNullType(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.createSimpleTransaction(null, null);
		});
	}
	
	@Test
	public void testSimple_NoPartIdNullType(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.createSimpleTransaction(def, null);
		});
	}
	
	@Test
	public void testSimple_NullType(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId("test");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.createSimpleTransaction(def, null);
		});
	}
	
	@Test
	public void testSimple(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		String partID = "test";
		TransactionType type = TransactionType.LOGISTICS_IN;
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(partID);
		assertTrue(def.getTransaction().isEmpty());
		
		Transaction transaction = transactionService.createSimpleTransaction(def, type);
		assertNotNull(transaction);
		assertEquals(partID, transaction.getParticipantId());
		assertEquals(type, transaction.getType());
		assertEquals(1, def.getTransaction().size());
		assertEquals(def.getTransaction().get(0), transaction);
	}
	
	@Test
	public void testFeedbackTransaction_NullPartNullType(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.createFeedbackTransaction(null, null, false);
		});
	}
	
	@Test
	public void testFeedbackTransaction_NoPartIdNullType(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.createFeedbackTransaction(def, null, false);
		});
	}
	
	@Test
	public void testFeedbackTransaction_NullType(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId("test");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionService.createFeedbackTransaction(def, null, false);
		});
	}
	
	@Test
	public void testFeedbackTransaction(@InjectService TransactionService transactionService,
			@InjectService EMFRepository repository) {
		assertNotNull(transactionService);
		assertNotNull(repository);
		
		String partID = "test";
		String feedbackName = "myfeedback";

		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(partID);
		assertTrue(def.getTransaction().isEmpty());
		Feedback f = NotaryFactory.eINSTANCE.createFeedback();
		FeedbackTransaction transaction = transactionService.createFeedbackTransaction(def, f, true);
		assertNotNull(transaction);
		assertEquals(partID, transaction.getParticipantId());
		assertEquals(TransactionType.FEEDBACK, transaction.getType());
		assertEquals(1, def.getTransaction().size());
		assertEquals(def.getTransaction().get(0), transaction);
		assertNotNull(transaction.getFeedback());
		assertEquals(f, transaction.getFeedback());
		assertTrue(transaction.isShare());
		assertNull(transaction.getDescription());
		
		assertNull(f.getName());
		f.setName(feedbackName);
		transaction = transactionService.createFeedbackTransaction(def, f, false);
		assertNotNull(transaction);
		assertEquals(partID, transaction.getParticipantId());
		assertEquals(TransactionType.FEEDBACK, transaction.getType());
		assertEquals(2, def.getTransaction().size());
		assertEquals(def.getTransaction().get(1), transaction);
		assertNotNull(transaction.getFeedback());
		assertEquals(f, transaction.getFeedback());
		assertFalse(transaction.isShare());
		assertEquals(feedbackName, transaction.getDescription());
	}
	
}

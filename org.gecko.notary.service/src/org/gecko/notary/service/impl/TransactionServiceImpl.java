/**
 * Copyright (c) 2012 - 2019 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.mongo.Options;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetTransaction;
import org.gecko.notary.model.notary.Feedback;
import org.gecko.notary.model.notary.FeedbackTransaction;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionType;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Service implementation for the transactions in the participant definition 
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TransactionService.class)
public class TransactionServiceImpl implements TransactionService {

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED, target="(repo_id=notary.notary)")
	private EMFRepository repository;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ParticipantService participantService;

	private static Map<Object, Object> loadOptions = new HashMap<>();
	private static Map<Object, Object> saveOptions = new HashMap<>();

	static {
		loadOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.TRANSACTION);
		saveOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.TRANSACTION);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#updateTransaction(java.lang.String, de.dim.diamant.Transaction)
	 */
	@Override
	public Transaction updateTransaction(String participantId, Transaction transaction) {
		if (transaction == null) {
			throw new IllegalStateException("Cannot create transaction with null object");
		}
		if (participantId == null || transaction.getParticipantId() == null) {
			throw new IllegalStateException("Cannot create transaction without owning participant");
		}
		if (!transaction.getParticipantId().equals(participantId)) {
			throw new IllegalStateException("Cannot create transaction with competing partiticpant id's");
		}
		ParticipantDefinition definition = participantService.getDefinition(participantId);
		if (definition == null) {
			throw new IllegalStateException("No participant found for this transaction owner");
		}
		Transaction existing = null;
		if (transaction.getId() != null) {
			existing = repository.getEObject(transaction.eClass(), transaction.getId(), loadOptions);
		}
		if (existing == null || !EcoreUtil.equals(existing, transaction)) {
			repository.save(transaction, saveOptions);
			if (existing == null) {
				definition.getTransaction().add(transaction);
				participantService.updateParticipantDefinition(definition);
			}
		}
		return transaction;
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#getTransactions(java.lang.String)
	 */
	@Override
	public List<Transaction> getTransactions(String participantId) {
		ParticipantDefinition definition = participantService.getDefinition(participantId);
		if (definition == null) {
			throw new IllegalStateException("No participant found to return transactions for the given id");
		}
		return new ArrayList<>(definition.getTransaction());
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#removeTransaction(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeTransaction(String participantId, String transactionId) {
		if (participantId == null || transactionId == null) {
			return false;
		}
		ParticipantDefinition definition = participantService.getDefinition(participantId);
		if (definition == null) {
			throw new IllegalStateException("Cannot remove a transaction for an unknown participant");
		}
		Transaction toBeRemoved = repository.getEObject(NotaryPackage.Literals.TRANSACTION, transactionId, loadOptions);
		if (toBeRemoved == null) {
			return false;
		}
		if (toBeRemoved.getParticipantId() == null || 
				!toBeRemoved.getParticipantId().equals(participantId)) {
			throw new IllegalStateException("Cannot remove a transaction for a foreign participant");
		}
		Optional<Transaction> tr = definition.getTransaction().stream().filter(t->t.getId().equals(transactionId)).findFirst();
		if (tr.isPresent() && definition.getTransaction().remove(tr.get())) {
			participantService.updateParticipantDefinition(definition);
		}
		repository.delete(toBeRemoved);
		return true;
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#createSimpleTransaction(de.dim.diamant.ParticipantDefinition, de.dim.diamant.TransactionType)
	 */
	@Override
	public Transaction createSimpleTransaction(ParticipantDefinition participantDef, TransactionType type) {
		if (participantDef == null) {
			throw new IllegalStateException("Cannot create a transaction without a participant definition");
		}
		if (participantDef.getId() == null) {
			throw new IllegalStateException("Cannot create a transaction without a participant definition id");
		}
		if (type == null) {
			throw new IllegalStateException("Cannot create a transaction without a process step type");
		}
		Transaction transaction = NotaryFactory.eINSTANCE.createTransaction();
		transaction.setParticipantId(participantDef.getId());
		transaction.setType(type);
		participantDef.getTransaction().add(transaction);
		return transaction;
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#createAssetTransaction(de.dim.diamant.ParticipantDefinition, de.dim.diamant.AssetChangeType)
	 */
	@Override
	public AssetTransaction createAssetTransaction(ParticipantDefinition participantDef, AssetChangeType type) {
		if (participantDef == null) {
			throw new IllegalStateException("Cannot create an asset transaction without a participant definition");
		}
		if (participantDef.getId() == null) {
			throw new IllegalStateException("Cannot create an asset transaction without a participant definition id");
		}
		if (type == null) {
			throw new IllegalStateException("Cannot create an asset transaction without a process step type");
		}
		AssetTransaction transaction = NotaryFactory.eINSTANCE.createAssetTransaction();
		transaction.setParticipantId(participantDef.getId());
		transaction.setType(TransactionType.ASSET);
		transaction.setChangeType(type);
		participantDef.getTransaction().add(transaction);
		return transaction;
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#createFeedbackTransaction(de.dim.diamant.ParticipantDefinition, de.dim.diamant.Feedback, boolean)
	 */
	@Override
	public FeedbackTransaction createFeedbackTransaction(ParticipantDefinition participantDef, Feedback feedback,
			boolean share) {
		if (participantDef == null) {
			throw new IllegalStateException("Cannot create a feedback transaction without a participant definition");
		}
		if (feedback == null) {
			throw new IllegalStateException("Cannot create a feedback transaction without a feedback isntance");
		}
		FeedbackTransaction transaction = NotaryFactory.eINSTANCE.createFeedbackTransaction();
		transaction.setParticipantId(participantDef.getId());
		transaction.setType(TransactionType.FEEDBACK);
		transaction.setDescription(feedback.getName());
		transaction.setShare(share);
		transaction.setFeedback(feedback);
		participantDef.getTransaction().add(transaction);
		return transaction;
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#getTransactionsByType(java.lang.String, de.dim.diamant.TransactionType)
	 */
	@Override
	public List<Transaction> getTransactionsByType(String participantId, TransactionType type) {
		if (participantId == null) {
			throw new IllegalStateException("Cannot get transactions with null participant id");
		}
		try {
			return getTransactions(participantId, type);
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(String.format("[%s] Cannot get transactions because of an error '%s'", participantId, e.getMessage()), e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#getPublicTransactions(java.lang.String, de.dim.diamant.TransactionType)
	 */
	@Override
	public List<Transaction> getSharedTransactions(String participantId, TransactionType type) {
		try {
			QueryRepository qr = (QueryRepository) repository;
			List<IQuery> queries = new LinkedList<>();
			IQuery publicFilter = qr.createQueryBuilder().column(NotaryPackage.Literals.TRANSACTION__SHARE).simpleValue(Boolean.TRUE).build();
			if (participantId != null) {
				IQuery participantFilter = qr.createQueryBuilder().column(NotaryPackage.Literals.TRANSACTION__PARTICIPANT_ID).simpleValue(participantId).build();
				queries.add(participantFilter);
			}
			if (type != null) {
				IQuery typeFilter = qr.createQueryBuilder().column(NotaryPackage.Literals.TRANSACTION__TYPE).simpleValue(type.getLiteral()).build();
				queries.add(typeFilter);
			}
			IQuery query = publicFilter;
			if (!queries.isEmpty()) {
				queries.add(0, publicFilter);
				query = qr.createQueryBuilder().and(queries.toArray(new IQuery[queries.size()])).build();
			}
			List<EObject> transactionResult = qr.getEObjectsByQuery(NotaryPackage.Literals.TRANSACTION, query, loadOptions);
			return transactionResult.stream().filter(Transaction.class::isInstance).map(Transaction.class::cast).collect(Collectors.toList());
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(String.format("[%s] Cannot get transactions because of an error '%s'", participantId, e.getMessage()), e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionService#getTransactionById(java.lang.String)
	 */
	@Override
	public Transaction getTransactionById(String transactionId) {
		if (transactionId ==  null) {
			throw new IllegalStateException("Cannot get a transaction with null id");
		}
		return repository.getEObject(NotaryPackage.Literals.TRANSACTION, transactionId, loadOptions);
	}

	/**
	 * Returns the {@link Transaction} for a given {@link Participant} id and an optional type filter
	 * @param participantId the participants id
	 * @param type the optional type filter
	 * @return the {@link Transaction}'s or an empty {@link List}
	 */
	private List<Transaction> getTransactions(String participantId, TransactionType type) {
		if (participantId == null) {
			throw new IllegalStateException("Cannot get transactions for an null participant");
		}
		QueryRepository qr = (QueryRepository) repository;
		IQuery participantFilter = qr.createQueryBuilder().column(NotaryPackage.Literals.TRANSACTION__PARTICIPANT_ID).simpleValue(participantId).build();
		IQuery query = participantFilter;
		if (type != null) {
			IQuery typeFilter = qr.createQueryBuilder().column(NotaryPackage.Literals.TRANSACTION__TYPE).simpleValue(type.getLiteral()).build();
			query = qr.createQueryBuilder().and(participantFilter, typeFilter).build();
		}
		List<EObject> transactionResult = qr.getEObjectsByQuery(NotaryPackage.Literals.TRANSACTION, query, loadOptions);
		return transactionResult.stream().filter(Transaction.class::isInstance).map(Transaction.class::cast).collect(Collectors.toList());
	}

}

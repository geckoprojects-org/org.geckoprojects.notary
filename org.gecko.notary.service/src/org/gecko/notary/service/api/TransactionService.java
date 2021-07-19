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
package org.gecko.notary.service.api;

import java.util.List;

import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetTransaction;
import org.gecko.notary.model.notary.Feedback;
import org.gecko.notary.model.notary.FeedbackTransaction;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionType;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Service to handle participant definitions transactions
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@ProviderType
public interface TransactionService {
	
	/**
	 * Returns a list of transactions for a given participant definition id
	 * @param participantDefinitionId the participant id 
	 * @return a {@link List} of {@link Transaction}'s or an empty {@link List}
	 */
	public List<Transaction> getTransactions(String participantDefinitionId);
	
	/**
	 * Returns a transaction for its given id or <code>null</code> if nothing was found
	 * @param transactionId the transaction id 
	 * @return a {@link Transaction} or <code>null</code>
	 */
	public Transaction getTransactionById(String transactionId);
	
	/**
	 * Updates/adds a {@link Transaction} to the given participant definition
	 * @param participantId the participant definition to be updated or added the transaction to
	 * @param transaction the transaction to update
	 * @return the updated transaction
	 */
	public Transaction updateTransaction(String participantId, Transaction transaction);
	
	/**
	 * Removes a transaction with the given id from the given participant definition
	 * @param participantId the participant definition to remove the transaction from
	 * @param transactionId the id transaction to be removed
	 * @return <code>true</code>, if removal was successful, otherwise <code>false</code>
	 */
	public boolean removeTransaction(String participantId, String  transactionId);
	
	/**
	 * Create a new process step. No persistence is done within this method.
	 * @param participantDef the participant definition, that process step belongs to
	 * @param type the process type
	 * @return the {@link Transaction}
	 */
	public Transaction createSimpleTransaction(ParticipantDefinition participantDef, TransactionType type);
	
	/**
	 * Create a new asset process step. No persistence is done within this method.
	 * @param participantDef the participant definition, that process step belongs to
	 * @param type the change type
	 * @return the {@link AssetTransaction}
	 */
	public AssetTransaction createAssetTransaction(ParticipantDefinition participantDef, AssetChangeType type);

	/**
	 * Creates a {@link FeedbackTransaction} for a given {@link Feedback} template.
	 * @param participantDef the participant definition, that process step belongs to
	 * @param feedback the {@link Feedback} template, must not be <code>null</code>
	 * @param share set to <code>true</code>, to make this process step public available, otherwise use <code>false</code>
	 * @return a feedback process step
	 */
	public FeedbackTransaction createFeedbackTransaction(ParticipantDefinition participantDef, Feedback feedback, boolean share);

	/**
	 * Returns process steps for a given participant and process step type
	 * @param participantId the id of the participant, that process step belongs to. Must not be <code>null</code>
	 * @param type an type filter, can be <code>null</code>
	 * @param processType the {@link TransactionType}
	 * @return the {@link Transaction}'s or an empty list
	 */
	public List<Transaction> getTransactionsByType(String participantId, TransactionType type);

	/**
	 * Returns process steps that are shared from participants as public
	 * @param participantId the id of the participant filter, can be <code>null</code>
	 * @param type an type filter, can be <code>null</code>
	 * @return the {@link Transaction}'s or an empty list
	 */
	public List<Transaction> getSharedTransactions(String participantId, TransactionType type);
	
}

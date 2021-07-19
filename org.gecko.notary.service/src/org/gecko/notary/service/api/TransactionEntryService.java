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

import org.eclipse.emf.ecore.EClass;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.TransactionEntry;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Service to handle transaction entries / smart contracts. 
 * @author Mark Hoffmann
 * @since 19.09.2019
 */
@ProviderType
public interface TransactionEntryService {
	
	/**
	 * Update the assets and creates the asset relates transaction events
	 * @param currentAsset can be <code>null</code>, when creating a new {@link Asset}
	 * @param newAsset must not be <code>null</code>
	 */
	public void createAssetModificationTransaction(Asset currentAsset, Asset newAsset);
	
	/**
	 * Creates a transaction entry for the given asset of the given type
	 * @param assetId the asset id
	 * @param assetType the asset {@link EClass}
	 * @param entry the entry to create
	 */
	public TransactionEntry createTransactionEntry(String assetId, EClass assetType, TransactionEntry entry);
	
	/**
	 * Return the transactions for the given asset id
	 * @param assetId the asset id
	 * @return the transaction  entries or an empty list
	 */
	public List<TransactionEntry> getTransactionEntry(String assetId);
	
	/**
	 * Return the transactions for the given asset id
	 * @param participantId the participant id
	 * @param type the type String that contains the EClass
	 * @return the transaction  entries or an empty list
	 */
	public List<TransactionEntry> getTransactionEntryByParticipantAndType(String participantId, String type);
	
	/**
	 * Return the last transactions entry for the given asset id
	 * @param assetId the asset id
	 * @return the last transaction entry or <code>null</code>
	 */
	public TransactionEntry getLastTransactionEntry(String assetId);

}

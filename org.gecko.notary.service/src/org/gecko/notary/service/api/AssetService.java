/**
 * Copyright (c) 2012 - 2019 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.api;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.TransactionEntry;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Handles assets
 * @author mark
 * @since 25.09.2019
 */
 @ProviderType
public interface AssetService {
		
		/**
		 * Returns all assets for a given participant id. This participant id is the creator context.
		 * @param participantId the participant id
		 * @param assetType the {@link EClass} type of the {@link Asset}
		 * @return a list of assets or an empty list
		 */
		public List<Asset> getAssetsByParticipant(String participantId, EClass assetType);
		
		/**
		 * Returns all assets for a given owner participant id. 
		 * @param owningParticipantId the participant id of the owner
		 * @param assetType the {@link EClass} type of the {@link Asset}
		 * @return a list of assets or an empty list
		 */
		public List<Asset> getAssetsByOwner(String owningParticipantId, EClass assetType);
		
		/**
		 * Returns all assets for a given owner participant id. 
		 * @param id the asset id
		 * @param participantId the participant id of the creator
		 * @param assetType the {@link EClass} type of the {@link Asset}
		 * @return a list of assets or an empty list
		 */
		public Asset getAssetByParticipant(String id, String participantId, EClass assetType);
		
		/**
		 * Returns all assets for a given owner participant id. 
		 * @param id the asset id
		 * @param owningParticipantId the participant id of the owner
		 * @param assetType the {@link EClass} type of the {@link Asset}
		 * @return a list of assets or an empty list
		 */
		public Asset getAssetByOwner(String id, String owningParticipantId, EClass assetType);
		
		/**
		 * Searches for a {@link Asset} with the given field name and value
		 * @param field the field/column {@link EAttribute}
		 * @param value the value
		 * @param assetType the {@link EClass} type of the {@link Asset}, can be <code>null</code>
		 * @return a {@link List} of assets or an empty list
		 */
		public List<Asset> searchAsset(EAttribute field, String value, EClass assetType);
		
		/**
		 * Searches for a {@link Asset} with the given field name and value in the {@link TransactionEntry}'s
		 * @param field the field/column {@link EAttribute}
		 * @param value the value
		 * @return a {@link List} of assets or an empty list
		 */
		public List<Asset> searchAssetInEntry(EAttribute field, String value);
	 
		/**
		 * Updates a given asset/digital twin instance
		 * @param asset the asset instance to be updated
		 * @return the updated asset instance or <code>null</code>
		 */
		public Asset updateAsset(Asset asset);
		
		/**
		 * Updates a given new asset/digital twin instance, compared to the current one, if it exists
		 * @param current the existing asset instanc. Can be <code>null</code>
		 * @param newAsset the asset instance to be updated
		 * @return the updated asset instance or <code>null</code>
		 */
		public Asset updateCompareAsset(Asset current, Asset newAsset);
		
		/**
		 * Updates the owner of an {@link Asset} with the give nid
		 * @param participantId the participant that id the new owner
		 * @param assetId the {@link Asset} to be updated
		 * @param assetType the {@link EClass} type of the {@link Asset}
		 * @return the updated {@link Asset}
		 */
		public Asset updateOwner(String participantId, String assetId, EClass assetType);

}

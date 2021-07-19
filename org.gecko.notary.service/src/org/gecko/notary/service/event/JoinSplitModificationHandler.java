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
package org.gecko.notary.service.event;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClass;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.service.api.TransactionEntryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Event handler that creates transaction entries for {@link Asset}'s that are joined to new assets or splitted from parental assets
 * @author Mark Hoffmann
 * @since 01.10.2019
 */
@Component(name = "JoinSplitTransactionEntryWorker", service = EventHandler.class, immediate = true, property = { EventConstants.EVENT_TOPIC + "=asset/joinsplit" })
public class JoinSplitModificationHandler implements EventHandler {

	public static final String PARENT_ASSET_ID = "parentAssetId";
	public static final String PARENT_ASSET_TYPE = "parentAssetType";
	public static final String JOIN_SPLIT_TYPE = "joinSplitType";
	public static final String JOIN_DATA = "joinData";
	public static final String SPLIT_DATA = "splitData";
	private static final String comment = "%s %s %s asset %s with id %s";
	private static final Logger logger = Logger.getLogger(JoinSplitModificationHandler.class.getName());
	@Reference
	private TransactionEntryService transactionEntryService;
	@Reference(target="(repo_id=notary.notary)")
	private EMFRepository repository;
	
	/* 
	 * (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(Event event) {
		String parentAsset = (String) event.getProperty(PARENT_ASSET_ID);
		EClass parentType = (EClass) event.getProperty(PARENT_ASSET_TYPE);
		EClass jsType = (EClass) event.getProperty(JOIN_SPLIT_TYPE);
		List<String> addings = (List<String>) event.getProperty(JOIN_DATA);
		List<String> removals = (List<String>) event.getProperty(SPLIT_DATA);
		if (addings == null) {
			logger.warning("Received an null adding data, which should not happen. Doing nothing");
			return;
		}
		if (removals == null) {
			logger.warning("Received an null removal data, which should not happen. Doing nothing");
			return;
		}
		addings.forEach(s->createTransactionEntry(s, parentAsset, jsType, parentType, AssetChangeType.JOIN));
		removals.forEach(s->createTransactionEntry(s, parentAsset, jsType, parentType, AssetChangeType.SPLIT));
	}
	
	/**
	 * Creates a split or join transaction entry
	 * @param assetId the asset id to be splitted or joined
	 * @param parentAssetId the asset id of the parent asset to join into or split from
	 * @param joinSplitType the type of the sub asset that will be joined or splitted
	 * @param parentType the type of the parent asset
	 * @param type the {@link AssetChangeType}
	 */
	private void createTransactionEntry(String assetId, String parentAssetId, EClass joinSplitType, EClass parentType, AssetChangeType type) {
		if (joinSplitType == null) {
			logger.warning("Received an null split join type, which should not happen. Doing nothing");
			return;
		}
		if (parentType == null) {
			logger.warning("Received an null parent asset type, which should not happen. Doing nothing");
			return;
		}
		if (type == null) {
			logger.warning("Received an null assert change type, which should not happen. Doing nothing");
			return;
		}
		if (assetId == null) {
			logger.warning("Received an null asset id, which should not happen. Doing nothing");
			return;
		}
		if (parentAssetId == null) {
			logger.warning("Received an null parent asset id, which should not happen. Doing nothing");
			return;
		}
		Asset asset = repository.getEObject(NotaryPackage.Literals.ASSET, assetId);
		if (asset == null) {
			logger.warning(String.format("[%s] Cannot find asset for id, which should not happen. Doing nothing", assetId));
			return;
		}
		AssetTransactionEntry entry = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		entry.setAsset(asset);
		entry.setParticipantId(asset.getOwnerId());
		entry.setChangeType(type);
		if (AssetChangeType.JOIN.equals(type) || AssetChangeType.SPLIT.equals(type)) {
			entry.setParentAssetId(parentAssetId);
			String[] txt = AssetChangeType.JOIN.equals(type) ? new String[] {"Joined", "to"} : new String[] {"Removed", "from"};
			entry.setComment(String.format(comment, txt[0], joinSplitType.getName(), txt[1], parentType.getName(), parentAssetId));
		}
		transactionEntryService.createTransactionEntry(assetId, joinSplitType, entry);
		repository.detach(asset);
	}

}

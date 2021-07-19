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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.mongo.Options;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.emf.repository.query.SortType;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetLog;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.TransactionEntryService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Implementation of the asset event service
 * @author Mark Hoffmann
 * @since 19.09.2019
 */
@Component(scope=ServiceScope.PROTOTYPE)
public class TransactionEntryServiceimpl implements TransactionEntryService {

	private static final Logger logger = Logger.getLogger(TransactionEntryServiceimpl.class.getName());
	private static final String ECLASS_URI = NotaryPackage.eNS_URI + "#//%s";
	@Reference(scope=ReferenceScope.PROTOTYPE_REQUIRED, target="(repo_id=notary.notary)")
	private EMFRepository repository;
	@Reference(target = "(&(object=TransactionEntry)(target=TransactionEntry))")
	private TextProvider transactionEntryTextProvider;
	@Reference(target = "(&(object=TransactionEntry)(target=Asset))")
	private TextProvider textProvider;
	@Reference
	private EventAdmin eventAdmin;

	private static Map<Object, Object> loadOptions = new HashMap<Object, Object>();
	private static Map<Object, Object> saveOptions = new HashMap<Object, Object>();	
	private static Map<Object, Object> assetSaveOptions = new HashMap<Object, Object>();

	static {
		loadOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.TRANSACTION_ENTRY);
		saveOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.TRANSACTION_ENTRY);
		assetSaveOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.ASSET);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionEntryService#getTransactionEntry(java.lang.String)
	 */
	@Override
	public List<TransactionEntry> getTransactionEntry(String assetId) {
		if (assetId == null) {
			throw new IllegalStateException("Cannot get transaction entries for  a null assetId");
		}
		AssetLog assetLog = repository.getEObject(NotaryPackage.Literals.ASSET_LOG, assetId);
		if (assetLog == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(assetLog.getEntry());
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionEntryService#getTransactionEntryByParticipantAndType(java.lang.String, java.lang.String)
	 */
	@Override
	public List<TransactionEntry> getTransactionEntryByParticipantAndType(String participantId, String type) {
		if (participantId == null) {
			throw new IllegalStateException("Cannot get transaction entry for a null participant");
		}
		QueryRepository qr = (QueryRepository) repository;
		IQueryBuilder participantFilter = qr.createQueryBuilder().column(NotaryPackage.Literals.TRANSACTION_ENTRY__PARTICIPANT_ID).simpleValue(participantId);
		IQueryBuilder queryBuilder = participantFilter;
		if (type != null) {
			String eClassUri = String.format(ECLASS_URI, type);
			IQuery typeFilter = qr.createQueryBuilder().column("_eClass").simpleValue(eClassUri).build();
			queryBuilder = qr.createQueryBuilder().and(participantFilter.build(), typeFilter);
		}
		IQuery query = queryBuilder.sort(NotaryPackage.Literals.TRANSACTION_ENTRY__TIMESTAMP, SortType.DESCENDING).build();
		List<EObject> transactionResult = qr.getEObjectsByQuery(NotaryPackage.Literals.TRANSACTION_ENTRY, query, loadOptions);
		List<TransactionEntry> transactions = transactionResult.stream().filter(r->r instanceof TransactionEntry).map(r->(TransactionEntry)r).collect(Collectors.toList());
		return transactions;
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionEntryService#getLastTransactionEntry(java.lang.String)
	 */
	@Override
	public TransactionEntry getLastTransactionEntry(String assetId) {
		if (assetId == null) {
			throw new IllegalStateException("Cannot get last transaction entry for a null assetId");
		}
		AssetLog assetLog = repository.getEObject(NotaryPackage.Literals.ASSET_LOG, assetId);
		if (assetLog == null) {
			return null;
		}
		return assetLog.getLastEntry();
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetEventService#updateAsset(de.dim.diamant.Asset, de.dim.diamant.Asset)
	 */
	@Override
	public void createAssetModificationTransaction(Asset currentAsset, Asset newAsset) {
		if (newAsset == null) {
			throw new IllegalStateException("Cannot create a null Asset transaction");
		}
		if (currentAsset != null && !currentAsset.getId().equals(newAsset.getId())) {
			throw new IllegalStateException("Cannot update a asset with the same id's");
		}
		String logId = newAsset.getId();
		AssetTransactionEntry entry = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		entry.setAsset(EcoreUtil.copy(newAsset));
		entry.setParticipantId(newAsset.getOwnerId());
		entry.setTransactionId("assetTransaction");
		if (currentAsset == null) {
			entry.setChangeType(AssetChangeType.CREATION);
			entry.setComment(String.format("Created asset of type %s with id %s", newAsset.eClass().getName(), logId));
		} else if (currentAsset != null) {
			if (!currentAsset.isInactive() && newAsset.isInactive()) {
				entry.setChangeType(AssetChangeType.DESTRUCTION);
				entry.setComment(String.format("Removed asset of type %s with id %s", newAsset.eClass().getName(), logId));
			} else if (!currentAsset.getOwnerId().equals(newAsset.getOwnerId())) {
				entry.setChangeType(AssetChangeType.OWNERSHIP);
				entry.setComment(String.format("Asset owner changed from '%s' to '%s'", currentAsset.getOwnerId(), newAsset.getOwnerId()));
				entry.setChangeData(currentAsset.getOwnerId());
			} 
		}
		// First create the modification entry, because we may reuse the entry instance, that is then returned
		AssetTransactionEntry modification = createModificationEntry(entry, currentAsset, newAsset);
		
		createTransactionEntry(logId, newAsset.eClass(), entry);
		
		// In case we reused the entry instance, we dont want to save it twice. We only save a new created instance
		if (modification != null && !modification.equals(entry)) {
			createTransactionEntry(logId, newAsset.eClass(), modification);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TransactionEntryService#createTransactionEntry(java.lang.String, org.eclipse.emf.ecore.EClass, de.dim.diamant.TransactionEntry)
	 */
	@Override
	public TransactionEntry createTransactionEntry(String assetId, EClass assetType, TransactionEntry entry) {
		if (entry == null) {
			throw new IllegalStateException("Cannot create a null transaction entry");
		}
		if (assetId == null) {
			throw new IllegalStateException("Cannot create a null transaction entry without assetId");
		}
		if (assetType == null) {
			throw new IllegalStateException("Cannot create a null transaction entry without asset type");
		}
		if (entry.getAssetId() == null) {
			entry.setAssetId(assetId);
		}
		AssetLog assetLog = repository.getEObject(NotaryPackage.Literals.ASSET_LOG, assetId);
		AssetLog resolvedLog = resolveAssetLog(assetId, assetLog, assetType);
		if (entry.getTimestamp() == null) {
			entry.setTimestamp(new Date());
		}
		chainWithLatest(entry, resolvedLog);
		transactionEntryTextProvider.provideText(entry, null);
		repository.save(entry, getSaveOptions());
		appendToLog(entry, resolvedLog);
		appendDescriptionToAsset(entry, resolvedLog);
		sendTransactionNotification(resolvedLog, entry);
		return entry;
	}

	/**
	 * Adds a description to the asset, if available
	 * @param entry the entry, to get text for
	 * @param resolvedLog the asset log
	 */
	private void appendDescriptionToAsset(TransactionEntry entry, AssetLog resolvedLog) {
		if (resolvedLog == null) {
			logger.warning("Cannot create description for null AssetLog");
			return;
		}
		String transactionDesc = textProvider.provideText(entry, null);
		if (transactionDesc != null) {
			Asset asset = resolvedLog.getAsset();
			if (!asset.eIsProxy()) {
				asset.getTransactionDesc().add(transactionDesc);
				repository.save(asset, assetSaveOptions);
			}
		}
	}

	/**
	 * Send an event to eventually handle notification for this transaction entry
	 * @param assetLog the {@link AssetLog}
	 * @param entry the {@link TransactionEntry} to be handled
	 */
	private void sendTransactionNotification(AssetLog assetLog, TransactionEntry entry) {
		Map<String, Object> eventProperties = new HashMap<String, Object>();
		eventProperties.put("type", NotaryPackage.Literals.TRANSACTION_ENTRY.getName());
		eventProperties.put("assetLog", assetLog);
		eventProperties.put("entry", entry);
		Event event = new Event("transactionEntry/notification", eventProperties);
		eventAdmin.postEvent(event);
	}

	/**
	 * Adds the reference of the latest entry to the current one
	 * @param current the current entry
	 * @param assetLog the asset log
	 * @return the chained entry
	 */
	private TransactionEntry chainWithLatest(TransactionEntry current, AssetLog assetLog) {
		TransactionEntry latest = assetLog.getLastEntry();
		if (latest != null) {
			current.setPrecedingEntry(latest);
			current.setPrecedingEntryId(latest.getId());
		}
		return current;
	}

	/**
	 * Appends the entry to the given log
	 * @param entry the entry to append
	 * @param assetLog the log to append the entry to
	 * @return the new log
	 */
	private AssetLog appendToLog(TransactionEntry entry, AssetLog assetLog) {
		assetLog.getEntry().add(entry);
		assetLog.setLastEntry(entry);
		repository.save(assetLog);
		return assetLog;
	}

	/**
	 * Tries to resolve the asset in the asset log
	 * @param expectedAssetId the asset id, the log should belong to
	 * @param assetLog the asset log
	 * @param assetType the asset type
	 * @return the resolved asset log
	 */
	private AssetLog resolveAssetLog(String expectedAssetId, AssetLog assetLog, EClass assetType) {
		Asset asset = null;
		if (assetLog == null) {
			asset = resolveAsset(expectedAssetId, null, assetType);
			assetLog = NotaryFactory.eINSTANCE.createAssetLog();
			assetLog.setAsset(asset);
			assetLog.setAssetId(expectedAssetId);
			assetLog.setId(expectedAssetId);
		} else {
			asset = assetLog.getAsset();
			Asset resolved = resolveAsset(expectedAssetId, asset, assetType);
			if (!resolved.equals(asset)) {
				asset = resolved;
				assetLog.setAsset(asset);
				assetLog.setAssetId(expectedAssetId);
			}
			if (!asset.getId().equals(expectedAssetId)) {
				logger.warning(String.format("[%s] Asset log has an asset id set, that is not of this asset. Setting correct asset id", expectedAssetId));
				assetLog.setAssetId(asset.getId());
			}
		}
		return assetLog;
	}

	/**
	 * Tries to resolve the given asset, where expectedAssetId is the leading id for the expected asset.
	 * The asset self can be <code>null</code>, and would be resolved 
	 * @param expectedAssetId the id of the expected asset
	 * @param asset a given asset, to compare against, but can be <code>null</code>
	 * @param assetType the asset type
	 * @return the resolved {@link Asset}
	 */
	private Asset resolveAsset(String expectedAssetId, Asset asset, EClass assetType) {
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.ASSET);
		if (asset == null) {
			logger.warning(String.format("[%s] Asset log has no asset assigned, try to resolve it", expectedAssetId));
			asset = repository.getEObject(assetType, expectedAssetId, loadOptions);
			if (asset == null) {
				throw new IllegalStateException(String.format("[%s] Error getting asset for an unknown asset of type '%s'", expectedAssetId, assetType.getName()));
			} 
		}
		if (!asset.getId().equals(expectedAssetId)) {
			logger.warning(String.format("[%s] Given expected assetIs is different to the id of the given asset", expectedAssetId));
			asset = repository.getEObject(assetType, expectedAssetId, loadOptions);
			if (asset == null) {
				throw new IllegalStateException(String.format("[%s] Error getting asset for an unknown asset of type '%s'", expectedAssetId, assetType.getName()));
			} 
		}
		return asset;
	}
	
	/**
	 * If we have no other changes, we can use the existing instance. In case we already have
	 * an switch of the ownership, we have two entry to create, where the ownership change has the highest priority
	 * A modification is not triggered on ownership switch only.
	 * @param entry the existing entry
	 * @param oldAsset the old asset
	 * @param newAsset the new asset
	 * @return the new modification entry or <code>null</code>
	 */
	private AssetTransactionEntry createModificationEntry(AssetTransactionEntry entry, Asset oldAsset, Asset newAsset) {
		if (AssetChangeType.UNKNOWN.equals(entry.getChangeType())) {
			entry.setChangeType(AssetChangeType.MODIFICATION);
			entry.setComment(String.format("Modified fields on asset of type %s with id %s", newAsset.eClass().getName(), newAsset.getId()));
			return entry;
		} else if (AssetChangeType.OWNERSHIP.equals(entry.getChangeType())) {
			Asset aOld = EcoreUtil.copy(oldAsset);
			aOld.setOwnerId("");
			Asset aNew = EcoreUtil.copy(newAsset);
			aNew.setOwnerId("");
			if (!EcoreUtil.equals(aOld, aNew)) {
				AssetTransactionEntry modification = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
				modification.setAsset(EcoreUtil.copy(newAsset));
				modification.setParticipantId(newAsset.getOwnerId());
				modification.setTransactionId("assetTransaction");
				modification.setChangeType(AssetChangeType.MODIFICATION);
				modification.setComment(String.format("Modified fields on asset of type %s with id %s", newAsset.eClass().getName(), newAsset.getId()));
				return modification;
			}
		}
		return null;
	}

	private Map<Object, Object> getSaveOptions() {
		return new HashMap<Object, Object>(saveOptions);
	}

}

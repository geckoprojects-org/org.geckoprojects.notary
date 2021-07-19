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
package org.gecko.notary.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.mongo.Options;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.AssetService;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Implementation of the {@link AssetService} using a database as storage 
 * @author Mark Hoffmann
 * @since 19.08.2019
 */
@Component(scope=ServiceScope.PROTOTYPE)
public class AssetServiceImpl implements AssetService {

	@Reference(scope=ReferenceScope.PROTOTYPE_REQUIRED, target="(repo_id=notary.notary)")
	private EMFRepository repository;

	@Reference(scope=ReferenceScope.PROTOTYPE_REQUIRED)
	private ParticipantService participantService;
	
	@Reference(target = "(&(object=Asset)(target=Asset))")
	private TextProvider textProvider;
	
	@Reference
	private EventAdmin eventAdmin;

	private static final Logger logger = Logger.getLogger(AssetServiceImpl.class.getName());
	private static Map<Object, Object> loadOptions = new HashMap<Object, Object>();
	private static Map<Object, Object> saveOptions = new HashMap<Object, Object>();
	
	static {
		loadOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.ASSET);
		saveOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.ASSET);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#getAssetByParticipant(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public Asset getAssetByParticipant(String id, String participantId, EClass assetType) {
		if(id == null) {
			throw new IllegalStateException("Cannot retrieve Asset without id.");
		}
		if(participantId == null) {
			throw new IllegalStateException("Cannot retrieve Asset without participant.");
		}
		if (assetType == null) {
			assetType = NotaryPackage.Literals.ASSET;
		}
		Asset asset = repository.getEObject(assetType, id, loadOptions);
		if (asset == null) {
			return null;
		}
		if (!participantId.equals(asset.getCreatorId())) {
			throw new IllegalStateException("The given participant is not the creator of the requested asset");
		} else {
			textProvider.provideText(asset, null);
			return asset;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#getAssetsByParticipant(java.lang.String, org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public List<Asset> getAssetsByParticipant(String participantId, EClass assetType) {
		List<Asset> assets = getAssetsByFeature(NotaryPackage.Literals.ASSET__CREATOR_ID, participantId, assetType);
		return assets;	
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#getAssetByOwner(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public Asset getAssetByOwner(String id, String owningParticipantId, EClass assetType) {
		if(id == null) {
			throw new IllegalStateException("Cannot retrieve Asset without id.");
		}
		if(owningParticipantId == null) {
			throw new IllegalStateException("Cannot retrieve Asset without owning participant.");
		}
		if (assetType == null) {
			assetType = NotaryPackage.Literals.ASSET;
		}
		Asset asset = repository.getEObject(assetType, id, loadOptions);
		if (asset == null) {
			return null;
		}
		if (!owningParticipantId.equals(asset.getOwnerId())) {
			throw new IllegalStateException("The given owning participant is not the owner of the requested asset");
		} else {
			textProvider.provideText(asset, null);
			return asset;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#getAssetsByOwner(java.lang.String, org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public List<Asset> getAssetsByOwner(String owningParticipantId, EClass assetType) {
		List<Asset> assets = getAssetsByFeature(NotaryPackage.Literals.ASSET__OWNER_ID, owningParticipantId, assetType);
		return assets;	
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#searchAsset(org.eclipse.emf.ecore.EAttribute, java.lang.String, org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public List<Asset> searchAsset(EAttribute field, String value, EClass assetType) {
		if (field == null || value == null) {
			throw new IllegalStateException("Cannot search for asset with null field or value");
		}
		return getAssetsByFeature(field, value, assetType);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#searchAssetInEntry(org.eclipse.emf.ecore.EAttribute, java.lang.String)
	 */
	@Override
	public List<Asset> searchAssetInEntry(EAttribute field, String value) {
		if (field == null || value == null) {
			throw new IllegalStateException("Cannot search for asset with null field or value");
		}
		QueryRepository queryRepository = (QueryRepository) repository;        
		IQueryBuilder queryBuilder = queryRepository.createQueryBuilder(); //here we are creating the query		
		queryBuilder.column(field).simpleValue(value);
		IQuery query = queryBuilder.build();
		Map<Object, Object> entryOptions = new HashMap<Object, Object>();
		entryOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.TRANSACTION_ENTRY);
		List<TransactionEntry> entries = queryRepository
				.getEObjectsByQuery(NotaryPackage.Literals.TRANSACTION_ENTRY, query, entryOptions);
		if (entries.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> assetIds = entries.stream().filter(te->te.getAssetId() != null).map(te->te.getAssetId()).collect(Collectors.toSet());
		if (assetIds.isEmpty()) {
			return Collections.emptyList();
		}
		queryBuilder = queryRepository.createQueryBuilder(); //here we are creating the query		
		queryBuilder.column(NotaryPackage.Literals.ASSET__ID).in(assetIds.toArray());
		IQuery assetQuery = queryBuilder.build();
		List<Asset> assets = queryRepository.getEObjectsByQuery(NotaryPackage.Literals.ASSET, assetQuery, loadOptions);
		if (assets != null) {
			assets.forEach(a->textProvider.provideText(a, null));
		}
		return assets == null ? Collections.emptyList() : assets;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#updateAsset(de.dim.diamant.Asset)
	 */
	@Override
	public Asset updateAsset(Asset asset) {
		if(asset == null) {
			throw new IllegalStateException("Cannot update a null Asset.");
		}
		if(asset.getCreatorId() == null) {
			throw new IllegalStateException("Cannot update a Asset for an instance with no creator");
		}
		String assetId = asset.getId();
		Asset existing = repository.getEObject(asset.eClass(), assetId, loadOptions);
		return updateCompareAsset(existing, asset);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#updateCompareAsset(de.dim.diamant.Asset, de.dim.diamant.Asset)
	 */
	@Override
	public Asset updateCompareAsset(Asset current, Asset newAsset) {
		if(newAsset == null) {
			throw new IllegalStateException("Cannot update a null Asset.");
		}
		if(newAsset.getCreatorId() == null) {
			throw new IllegalStateException("Cannot update a Asset for an instance with no creator");
		}
		String participantId = newAsset.getCreatorId(); 
		ParticipantDefinition definition = participantService.getDefinition(participantId);
		if (definition == null) {
			throw new IllegalStateException(String.format("[%s] No participant found, cannot create the asset", participantId));
		}
		Map<String, Object> saveOptions = new HashMap<String, Object>();
		saveOptions.put(Options.OPTION_COLLECTION_NAME, NotaryPackage.Literals.ASSET);
		String assetId = newAsset.getId();
		textProvider.provideText(newAsset, null);
		if (assetId == null || current == null) {
			repository.save(newAsset, getSaveOptions());	
			definition.getAsset().add(newAsset);
			participantService.updateParticipantDefinition(definition);
			sendAssetModification(current, newAsset);
		} else if (!EcoreUtil.equals(newAsset, current)) {
			repository.save(newAsset, getSaveOptions());
			sendAssetModification(current, newAsset);
		}
		return newAsset;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.AssetService#updateOwner(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public Asset updateOwner(String participantId, String assetId, EClass assetType) {
		if (assetId == null) {
			throw new IllegalStateException("Cannot update the owner of a null asset");
		}
		if (participantId == null) {
			throw new IllegalStateException(String.format("[%s] Cannot update the owner that is null for the asset", assetId));
		}
		if (assetType == null) {
			logger.warning(()->String.format("[%s] No asset type was given, taking ASSET as default", assetId));
			assetType = NotaryPackage.eINSTANCE.getAsset();
		}
		Asset asset = repository.getEObject(assetType, assetId, loadOptions);
		if (asset == null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(String.format("[%s] No asset found for type '%s'", assetId, assetType.getName()));
			}
			return null;
		}
		if (!participantId.equals(asset.getOwnerId())) {
			Participant participant = participantService.getParticipant(participantId);
			if (participant == null) {
				throw new IllegalStateException(String.format("[%s] Unknown owner '%s' for the asset", assetId, participantId));
			}
			Asset current = EcoreUtil.copy(asset);
			current.setOwnerId(participantId);
			if (participant.getName() != null) {
				current.setOwnerName(participant.getDescription());
			}
			repository.save(current, getSaveOptions());
			sendAssetModification(asset, current);
			return current;
		}
		return asset;
	}
	
	/**
	 * Send an async event to handle modification on assets to create transaction entries for them.
	 * @param current the current/existing asset
	 * @param newAsset the new asset
	 * @see AssetModificationHandler
	 */
	private void sendAssetModification(Asset current, Asset newAsset) {
		Map<String, Object> eventProperties = new HashMap<String, Object>();
		eventProperties.put("type", NotaryPackage.Literals.ASSET.getName());
		if (current != null) {
			eventProperties.put("current", current);
		}
		eventProperties.put("new", newAsset);
		Event event = new Event("asset/modification", eventProperties);
		eventAdmin.sendEvent(event);
	}

	/**
	 * Executes a query against one {@link EStructuralFeature}
	 * @param attribute the feature
	 * @param value the value for the feature
	 * @param assetType the {@link EClass} type of the {@link Asset}
	 * @return the list of {@link Asset}'s or an empty list
	 */
	private List<Asset> getAssetsByFeature(EAttribute attribute, String value, EClass assetType) {
		if(value == null) {
			throw new IllegalStateException("Cannot retrieve Assets for a null feature ID.");
		}
		if (assetType == null) {
			assetType = NotaryPackage.Literals.ASSET;
		}
		QueryRepository queryRepository = (QueryRepository) repository;        
		IQueryBuilder queryBuilder = queryRepository.createQueryBuilder(); //here we are creating the query		
		queryBuilder.column(attribute).simpleValue(value);
		IQuery query = queryBuilder.build();
		List<Asset> assets = queryRepository
				.getEObjectsByQuery(assetType, query, loadOptions);
		assets.forEach(a->textProvider.provideText(a, null));
		return assets;
	}
	
	private Map<Object, Object> getSaveOptions() {
		return new HashMap<Object, Object>(saveOptions);
	}
}

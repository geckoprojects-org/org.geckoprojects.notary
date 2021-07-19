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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.service.api.ParticipantService;


/**
 * Base service implemenation
 * @author Mark Hoffmann
 * @since 05.09.2019
 */
public class BaseParticipantService {
	
	protected ParticipantService participantService;
	protected EMFRepository repository;
	
	/**
	 * Sets the participantService.
	 * @param participantService the participantService to set
	 */
	protected void setParticipantService(ParticipantService participantService) {
		this.participantService = participantService;
	}
	
	/**
	 * Sets the repository.
	 * @param repository the repository to set
	 */
	protected void setRepository(EMFRepository repository) {
		this.repository = repository;
	}
	
	/**
	 * Updates or adds a single element to the feature
	 * @param participantId
	 * @param updateObject
	 * @param feature
	 * @param updateIdFeature
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected EObject updateByFeature(String participantId, EObject updateObject, EStructuralFeature feature, EStructuralFeature updateIdFeature) {
		if (updateObject == null) {
			return null;
		}
		ParticipantDefinition definition = participantService.getDefinition(participantId);
		if (definition == null) {
			throw new IllegalStateException(String.format("No participant found to update the object '%s' for", updateObject.eClass().getName()));
		}
		List<EObject> resultObjects = (List<EObject>) definition.eGet(feature);
		String updateId = (String) updateObject.eGet(updateIdFeature);
		Optional<EObject> rOpt = resultObjects.stream().filter(r->updateId != null && r.eGet(updateIdFeature).equals(updateId)).findFirst();
		if (rOpt.isPresent()) {
			EObject existing = rOpt.get();
			if (!EcoreUtil.equals(existing, updateObject)) {
				int idx = resultObjects.indexOf(rOpt.get());
				resultObjects.set(idx, updateObject);
				participantService.updateParticipantDefinition(definition);
			}
		} else {
			if (updateId == null) {
				updateObject.eSet(updateIdFeature, UUID.randomUUID().toString());
			}
			resultObjects.add(updateObject);
			participantService.updateParticipantDefinition(definition);
		}
		return updateObject;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean removeByFeature(String participantId, String  removeId, EStructuralFeature feature, EStructuralFeature removeIdFeature) {
		if (removeId == null) {
			return false;
		}
		ParticipantDefinition definition = participantService.getDefinition(participantId);
		if (definition == null) {
			throw new IllegalStateException("No particpant found to remove the object for");
		}
		List<EObject> resultObjects = (List<EObject>) definition.eGet(feature);
		Optional<EObject> cOpt = resultObjects.stream().filter(r->r.eGet(removeIdFeature).equals(removeId)).findFirst();
		if (cOpt.isPresent()) {
			resultObjects.remove(cOpt.get());
			participantService.updateParticipantDefinition(definition);
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends EObject> List<T> getByFeature(String participantDefinitionId, EStructuralFeature feature) {
		ParticipantDefinition definition = participantService.getDefinition(participantDefinitionId);
		if (definition == null) {
			throw new IllegalStateException("No participant found to return the transactions");
		}
		List<EObject> resultObjects = (List<EObject>) definition.eGet(feature);
		List<T> castedResult = resultObjects.stream().map(r->(T)r).collect(Collectors.toList());
		return Collections.synchronizedList(castedResult);
	}

}

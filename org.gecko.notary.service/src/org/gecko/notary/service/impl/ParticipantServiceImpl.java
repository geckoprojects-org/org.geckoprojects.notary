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
package org.gecko.notary.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.Address;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.service.api.ParticipantService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Service implementation for the participants 
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@Component(scope = ServiceScope.PROTOTYPE)
public class ParticipantServiceImpl implements ParticipantService {
	
	private static final Logger logger = Logger.getLogger(ParticipantServiceImpl.class.getName());
	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED, target="(repo_id=diamant.diamant)")
	private EMFRepository repository;

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.participant.api.ParticipantService#createSimpleParticipant(java.lang.String, java.lang.String)
	 */
	@Override
	public ParticipantDefinition createSimpleParticipant(String name, String description) {
		return createSimpleParticipant(name, description, null);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.ParticipantService#createSimpleParticipant(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ParticipantDefinition createSimpleParticipant(String name, String description, String id) {
		if (name == null) {
			throw new IllegalStateException("Cannot create a Participant with  null name");
		}
		try {
			ParticipantDefinition partDef = getDefinitionByName(name);
			if(partDef != null) {
				throw new IllegalStateException(String.format("[%s] Cannot create a participant that  already exists.", name));
			}
			Participant participant = NotaryFactory.eINSTANCE.createParticipant();
			if (id != null) {
				participant.setId(id);
			}
			participant.setName(name);
			if (description != null) {
				participant.setDescription(description);
			}
			repository.save(participant);
			ParticipantDefinition definition = NotaryFactory.eINSTANCE.createParticipantDefinition();
			definition.setId(participant.getId());
			definition.setParticipant(participant);
			repository.save(definition);
			return definition;
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(String.format("[%s] Cannot create a participant definition because of an error '%s'", name, e.getMessage()), e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.participant.api.ParticipantService#updateParticipant(de.dim.diamant.Participant)
	 */
	@Override
	public Participant updateParticipant(Participant participant) {
		if (participant == null) {
			logger.severe("Cannot update a null Participant");
			return null;
		}
		try {
			if (participant.getName() == null) {
				throw new IllegalStateException("Cannot update a participant without name");
			}
			String id = participant.getId();
			if (id == null) {
				logger.info(String.format("[%s] Save new participant without id", participant.getName()));
				repository.save(participant);
			} else {
				Participant current = getParticipant(id);
				if (current == null) {
					logger.info(String.format("[%s] Save new participant", participant.getId()));
					repository.save(participant);
				} else if(!EcoreUtil.equals(current, participant)) {			
					logger.info(String.format("[%s] Update participant because a change was detected", id));
					repository.save(participant);
				}		
			}
			return participant;
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(String.format("[%s] Cannot update a participant because of an error '%s'", participant.getName(), e.getMessage()), e);
		}
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.ParticipantService#updateAddress(java.lang.String, de.dim.diamant.Address)
	 */
	@Override
	public Participant updateAddress(String participantId, Address address) {
		if (address == null) {
			return null;
		}
		Participant participant = getParticipant(participantId);
		if (participant == null) {
			throw new IllegalStateException("No participant found to update the address");
		}
		Address current = participant.getAddress();
		
		if (current == null || !EcoreUtil.equals(address, current)) {
			participant = EcoreUtil.copy(participant);
			participant.setAddress(address);
			updateParticipant(participant);
		}
		return participant;
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.participant.api.ParticipantService#updateParticipantDefinition(de.dim.diamant.ParticipantDefinition)
	 */
	@Override
	public ParticipantDefinition updateParticipantDefinition(ParticipantDefinition definition) {
		if (definition == null) {
			logger.severe("Cannot update a null participant definition");
			return null;
		}
		try {
			if (definition.getParticipant() == null) {
				throw new IllegalStateException("Cannot update a participant definition with null participant reference");
			}
			Participant participant = definition.getParticipant();
			if (participant.getId() == null) {
				throw new IllegalStateException("Cannot update a participant definition with empty participant id. You should update the participant first");
			}
			String participantId = participant.getId();
			if (participant.eResource() == null) {
				Participant proxy = repository.createProxy(NotaryPackage.Literals.PARTICIPANT, participantId);
				definition.setParticipant(proxy);
			}
			String id = definition.getId();
			if (id == null) {
				definition.setId(participantId);
				logger.info(String.format("[%s] Save new participant definition", participantId));
			} else {
				logger.info(String.format("[%s] Update participant definition because a change was detected", id));
			}
			repository.save(definition);
			return definition;
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(String.format("[%s] Cannot update a participant definition because of an error '%s'", definition.getId(), e.getMessage()), e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.participant.api.ParticipantService#getParticipant(java.lang.String)
	 */
	@Override
	public Participant getParticipant(String participantId) {
		if (participantId == null) {
			throw new IllegalStateException("Cannot get participant with null id");
		}
		try {
			Participant participant = repository.getEObject(NotaryPackage.Literals.PARTICIPANT, participantId);
			return participant;
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(String.format("[%s] Cannot get participant because of an error '%s'", participantId, e.getMessage()), e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.participant.api.ParticipantService#getDefinition(java.lang.String)
	 */
	@Override
	public ParticipantDefinition getDefinition(String participantDefinitionId) {
		if (participantDefinitionId == null) {
			throw new IllegalStateException("Cannot get participant definition with null id");
		}
		try {
			ParticipantDefinition definition = repository.getEObject(NotaryPackage.Literals.PARTICIPANT_DEFINITION, participantDefinitionId);
			return definition;
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(String.format("[%s] Cannot get participant definition because of an error '%s'", participantDefinitionId, e.getMessage()), e);
		}
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.ParticipantService#appendAsset(java.lang.String, de.dim.diamant.Asset)
	 */
	@Override
	public ParticipantDefinition appendAsset(String participantDefinitionId, Asset asset) {
		ParticipantDefinition participant = getDefinition(participantDefinitionId);
		if (participant == null) {
			return null;
		}
		if (asset == null) {
			logger.warning(String.format("[%s] Cannot add a null asset for this participant", participantDefinitionId));
			return participant;
		}
		List<Asset> assets = Collections.synchronizedList(participant.getAsset());
		synchronized (participant.getAsset()) {
			if (!assets.stream().anyMatch(a->asset.getId().equals(a.getId()))) {
				participant.getAsset().add(asset);
				updateParticipantDefinition(participant);
			}
		}
		return participant;
	}

	/**
	 * Returns the {@link ParticipantDefinition} for a given {@link Participant} name or <code>null</code>, if nothing was found
	 * @param name the participants name
	 * @return the {@link ParticipantDefinition} or <code>null</code>
	 */
	private ParticipantDefinition getDefinitionByName(String name) {
		QueryRepository qr = (QueryRepository) repository;
		IQueryBuilder qb = qr.createQueryBuilder();
		IQuery nameQuery = qb.column(NotaryPackage.Literals.PARTICIPANT__NAME).simpleValue(name).build();
		Participant participant = qr.getEObjectByQuery(NotaryPackage.Literals.PARTICIPANT, nameQuery, null);
		if (participant == null) {
			logger.log(Level.INFO, String.format("[%s] No participant found", name));
			return null;
		} else {
			ParticipantDefinition participantDef = repository.getEObject(NotaryPackage.Literals.PARTICIPANT_DEFINITION, participant.getId());
			if (participantDef == null) {
				throw new IllegalStateException(String.format("[%s][%s] Cannot find participant definition for existing participant. this should not happen!", participant.getId(), name));
			}
			participantDef = EcoreUtil.copy(participantDef);
			participantDef.setParticipant(EcoreUtil.copy(participant));
			return participantDef;
		}
	}
	
}

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

import org.gecko.notary.model.notary.Address;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Service to handle major functionalities for participants
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@ProviderType
public interface ParticipantService {
	
	/**
	 * Creates a simple {@link ParticipantDefinition} instance. The participant is then also available using
	 * {@link ParticipantDefinition#getParticipant()}.
	 * If an instance already exists, this method calls an {@link IllegalStateException} as well if the name parameter is <code>null</code>
	 * @param name the name of the participant to be created, mendatory
	 * @param description the optional description
	 * @return the participant definition
	 */
	public ParticipantDefinition createSimpleParticipant(String name, String description);
	public ParticipantDefinition createSimpleParticipant(String name, String description, String id);
	
	/**
	 * Updates a {@link Participant} instance. If the instance does not already exist, it will be created.
	 * @param participant the {@link Participant} to persist
	 * @return the updated instance
	 */
	public Participant updateParticipant(Participant participant);
	
	/**
	 * Updates the address for the given participant
	 * @param participantId the participant id
	 * @param address the address to update
	 * @return the updated participant
	 */
	public Participant updateAddress(String participantId, Address address);
	
	/**
	 * Updates a {@link ParticipantDefinition} instance. If the instance does not already exist, it will be created.
	 * @param participantDefinition the {@link ParticipantDefinition} to persist
	 * @return the updated instance
	 */
	public ParticipantDefinition updateParticipantDefinition(ParticipantDefinition participantDefinition);
	
	/**
	 * Returns the {@link Participant} for the given id or <code>null</code>, if unknown.
	 * @param participantId the id of the {@link Participant} to get
	 * @return the {@link Participant} instance or <code>null</code>
	 */
	public Participant getParticipant(String participantId);
	
	/**
	 * Returns the {@link ParticipantDefinition} for the given id or <code>null</code>, if unknown.
	 * @param participantDefinitionId the id of the {@link ParticipantDefinition} to get
	 * @return the {@link ParticipantDefinition} instance or <code>null</code>
	 */
	public ParticipantDefinition getDefinition(String participantDefinitionId);
	
	/**
	 * Adds the given asset to the participant definition
	 * @param participantDefinitionId the participant to add the asset 
	 * @param asset the asset to add
	 * @return The modified participant definition 
	 */
	public ParticipantDefinition appendAsset(String participantDefinitionId, Asset asset);
	
}

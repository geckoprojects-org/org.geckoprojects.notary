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

import java.util.List;

import org.gecko.emf.repository.EMFRepository;
import org.gecko.notary.model.notary.Contact;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.service.api.ContactService;
import org.gecko.notary.service.api.ParticipantService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Service implementation for the contacts in the participant definition 
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ContactService.class)
public class ContactServiceImpl extends BaseParticipantService implements ContactService {
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.impl.BaseParticipantService#setParticipantService(de.dim.diamant.service.api.ParticipantService)
	 */
	@Override
	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	protected void setParticipantService(ParticipantService participantService) {
		super.setParticipantService(participantService);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.impl.BaseParticipantService#setRepository(org.gecko.emf.repository.EMFRepository)
	 */
	@Override
	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED, target="(repo_id=notary.notary)")
	protected void setRepository(EMFRepository repository) {
		super.setRepository(repository);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.ContactService#updateContact(java.lang.String, de.dim.diamant.Contact)
	 */
	@Override
	public Contact updateContact(String participantId, Contact contact) {
		return (Contact) updateByFeature(participantId, contact, NotaryPackage.Literals.PARTICIPANT_DEFINITION__CONTACT, NotaryPackage.Literals.CONTACT__ID);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.ContactService#getContacts(java.lang.String)
	 */
	@Override
	public List<Contact> getContacts(String participantDefinitionId) {
		return getByFeature(participantDefinitionId, NotaryPackage.Literals.PARTICIPANT_DEFINITION__CONTACT);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.ContactService#removeContact(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeContact(String participantId, String  contactId) {
		return removeByFeature(participantId, contactId, NotaryPackage.Literals.PARTICIPANT_DEFINITION__CONTACT, NotaryPackage.Literals.CONTACT__ID);
	}
	
}

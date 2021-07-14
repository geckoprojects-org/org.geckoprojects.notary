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

import org.gecko.notary.model.notary.Contact;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Service to handle participant definitions contacts
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@ProviderType
public interface ContactService {
	
	/**
	 * Returns a list of contact for a given participant definition id
	 * @param participantDefinitionId the participant id 
	 * @return a {@link List} of {@link Contact}'s or an empty {@link List}
	 */
	public List<Contact> getContacts(String participantDefinitionId);
	
	/**
	 * Updates/adds a contact to the given participant definition
	 * @param participantId the participant definition to be updated or added the contact to
	 * @param contact the contact to update
	 * @return the updated contact
	 */
	public Contact updateContact(String participantId, Contact contact);
	
	/**
	 * Removes a contact with the given id from the given participant definition
	 * @param participantId the participant definition to remove the contact from
	 * @param contactId the id contact to be removed
	 * @return <code>true</code>, if removal was successful, otherwise <code>false</code>
	 */
	public boolean removeContact(String participantId, String  contactId);
	
}

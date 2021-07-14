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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gecko.emf.repository.EMFRepository;
import org.gecko.notary.model.notary.Contact;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionNotification;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionNotificationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Service implementation for the notifications in the participant definition 
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TransactionNotificationService.class)
public class TransactionNotificationServiceImpl extends BaseParticipantService implements TransactionNotificationService {
	
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
	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED, target="(repo_id=diamant.diamant)")
	protected void setRepository(EMFRepository repository) {
		super.setRepository(repository);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.NotificationService#updateNotification(java.lang.String, de.dim.diamant.TransactionNotification)
	 */
	@Override
	public TransactionNotification updateNotification(String participantId, TransactionNotification notification) {
		if (notification == null) {
			return null;
		}
		String contactId = notification.getContactId();
		String transactionId = notification.getTransactionId();
		if (contactId == null || transactionId == null) {
			throw new IllegalStateException("Cannot create a notification that doens not have a transaction id and/or contact id");
		}
		ParticipantDefinition definition = participantService.getDefinition(participantId);
		if (definition == null) {
			throw new IllegalStateException("No participant found to update the notifcation for");
		}
		if (notification.getContact() == null || 
				(notification.getContact() != null && 
				notification.getContact().getId() != contactId)) {
			Optional<Contact> cOpt = definition.getContact().stream().filter(c->c.getId().equals(contactId)).findFirst();
			if (cOpt.isPresent()) {
				Contact c = cOpt.get();
				notification.setContact(c);
			} else {
				throw new IllegalStateException("No contact found for the given contactId " + contactId);
			}
		}
		if (notification.getTransaction() == null || 
				(notification.getTransaction() != null && 
				notification.getTransaction().getId() != transactionId)) {
			Optional<Transaction> tOpt = definition.getTransaction().stream().filter(t->t.getId().equals(transactionId)).findFirst();
			if (tOpt.isPresent()) {
				Transaction t = tOpt.get();
				notification.setTransaction(t);
			} else {
				throw new IllegalStateException("No transaction found for the given transactionId " + transactionId);
			}
		}
		return (TransactionNotification) updateByFeature(participantId, notification, NotaryPackage.Literals.PARTICIPANT_DEFINITION__NOTIFICATION, NotaryPackage.Literals.TRANSACTION_NOTIFICATION__ID);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.NotificationService#removeNotification(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeNotification(String participantId, String notificationId) {
		return removeByFeature(participantId, notificationId, NotaryPackage.Literals.PARTICIPANT_DEFINITION__NOTIFICATION, NotaryPackage.Literals.TRANSACTION_NOTIFICATION__ID);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.NotificationService#getNotifications(java.lang.String)
	 */
	@Override
	public List<TransactionNotification> getNotifications(String participantId) {
		return getByFeature(participantId, NotaryPackage.Literals.PARTICIPANT_DEFINITION__NOTIFICATION);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.NotificationService#getNotificationsByTransaction(java.lang.String, java.lang.String)
	 */
	@Override
	public List<TransactionNotification> getNotificationsByTransaction(String participantId, String transactionId) {
		return getNotifications(participantId).stream().filter(n->n.getTransactionId().equals(transactionId)).collect(Collectors.toList());
	}	
}

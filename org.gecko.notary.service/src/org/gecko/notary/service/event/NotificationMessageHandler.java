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
package org.gecko.notary.service.event;

import java.util.Date;
import java.util.logging.Logger;

import org.gecko.emf.repository.EMFRepository;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Notification;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.model.notary.TransactionNotification;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Handler that creates {@link Notification} instance for a sent {@link TransactionEntry}
 * @author Mark Hoffmann
 * @since 02.10.2019
 */
@Component(name = "NotificationMessageHandler", property = { EventConstants.EVENT_TOPIC  + "=notification/*" })
public class NotificationMessageHandler implements EventHandler {
	
	private static final Logger logger = Logger.getLogger(NotificationMessageHandler.class.getName());
	@Reference
	private EMFRepository repository;

	/* 
	 * (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		TransactionNotification notificationDef = (TransactionNotification) event.getProperty("notification");
		TransactionEntry entry = (TransactionEntry) event.getProperty("entry");
		if (notificationDef == null || entry == null) {
			logger.warning("Received an message notification event without payload: entry, notification");
			return;
		}
		Transaction transaction = notificationDef.getTransaction();
		if (transaction == null) {
			logger.warning("Received an notification definition without transaction link. This should not happen.");
			return;
		}
		Notification notification = NotaryFactory.eINSTANCE.createNotification();
		notification.setRead(false);
		notification.setTimestamp(new Date());
		notification.setSenderId(entry.getParticipantId());
		notification.setReceipientId(transaction.getParticipantId());
		notification.setSubject(transaction.getDescription());
		notification.setContent(notificationDef.getContent());
		repository.save(notification);
	}

}

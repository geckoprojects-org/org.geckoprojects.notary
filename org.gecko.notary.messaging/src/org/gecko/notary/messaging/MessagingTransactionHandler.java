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
package org.gecko.notary.messaging;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.adapter.amqp.client.AMQPContextBuilder;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.osgi.messaging.MessagingContext;
import org.gecko.osgi.messaging.MessagingService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.promise.PromiseFactory;
import org.gecko.notary.model.notary.Contact;
import org.gecko.notary.model.notary.ContactType;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.model.notary.TransactionNotification;

/**
 * EventHandler to send email via SMTP
 * @author Mark Hoffmann
 * @since 02.10.2019
 */
@Component(name = "MessagingTransactionHandler", immediate = true, property = {EventConstants.EVENT_TOPIC + "=notification/MESSAGING"})
public class MessagingTransactionHandler implements EventHandler {

	private static final Logger logger = Logger.getLogger(MessagingTransactionHandler.class.getName());
	@Reference
	private EMFRepository repository;
	@Reference(target="(component.name=AMQPService)")
	private MessagingService messaging;
	private PromiseFactory pf = new PromiseFactory(Executors.newCachedThreadPool());

	/* 
	 * (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		TransactionNotification definition = (TransactionNotification) event.getProperty("notification");
		TransactionEntry entry = (TransactionEntry) event.getProperty("entry");
		if (definition == null || entry == null) {
			logger.warning("Received an message notification event without payload: entry, notification");
			return;
		}
		Transaction transaction = definition.getTransaction();
		if (transaction == null) {
			logger.warning("Received an notification definition without transaction link. This should not happen.");
			return;
		}
		pf.submit(()->sendMessage(definition, entry));
	}

	/**
	 * Sends an message using messaging service
	 * @param definition the {@link TransactionNotification}
	 * @param entry the {@link TransactionEntry} the created entry
	 * @return
	 */
	private Void sendMessage(TransactionNotification definition, TransactionEntry entry) {
		Contact c = definition.getContact();
		if (!ContactType.MESSAGING.equals(c.getType())) {
			logger.log(Level.WARNING, String.format("[%s][%s] The notification definition is not of type messaging but %s. Nothing to send!", definition.getId(), entry.getId(), c.getType()));
			return null;
		}
		if (c.getValue().isEmpty()) {
			logger.log(Level.WARNING, String.format("[%s][%s] The notification definition does not contain a value to be used to send a message. Nothing to send!", definition.getId(), entry.getId(), c.getType()));
			return null;
		}
		String queue = c.getValue().get(0);
		queue = queue.replace("/", ".");
		Transaction transaction = definition.getTransaction();
		if (transaction == null) {
			logger.warning("Received an notification definition without transaction link. This should not happen.");
			return null;
		}
		try {
			Resource resource = repository.createResource(entry, "xmi");
			resource.getContents().add(EcoreUtil.copy(entry));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			resource.save(baos, null);
			/*
			 * Send the message with the receiver/owner of the NotificationDefinition linked transaction.
			 * This is the participant who owns a transaction. So he will listen for it
			 */
			MessagingContext ctx = new AMQPContextBuilder().direct().exchange(queue, transaction.getParticipantId()).build();
			messaging.publish(queue, ByteBuffer.wrap(baos.toByteArray()), ctx);
			logger.log(Level.INFO, String.format("[%s][%s] Sent message", entry.getId(), queue));
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("[%s][%s] Error sending EMail for template", entry.getParticipantId(), definition.getId()), e);
		}
		return null;
	}

}

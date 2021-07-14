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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gecko.notary.model.notary.AssetTransaction;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.model.notary.TransactionNotification;
import org.gecko.notary.model.notary.TransactionType;
import org.gecko.notary.service.api.TransactionNotificationService;
import org.gecko.notary.service.api.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Dispatcher that handles all {@link TransactionEntry}'s and forwards those, that are configured for 
 * a certain notification. This belong to all entry's that have transactionNotifications configured for a certain transaction
 * @author Mark Hoffmann
 * @since 01.10.2019
 */
@Component(name = "TransactionNotificationDispatcher", property = { EventConstants.EVENT_TOPIC  + "=transactionEntry/notification" })
public class TransactionEntryNotificationHandler implements EventHandler {

	private static final Logger logger = Logger.getLogger(TransactionEntryNotificationHandler.class.getName());
	@Reference
	private TransactionNotificationService notificationService;
	@Reference
	private TransactionService transactionService;
	@Reference
	private EventAdmin eventAdmin;

	/* 
	 * (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		TransactionEntry entry = (TransactionEntry) event.getProperty("entry");
		if (entry == null) {
			logger.info("Received an transaction notification event with a null transaction entry. This should not happen! Doing nothing");
			return;
		}
		String transactionId = entry.getTransactionId();
		try {
			Transaction transaction = getTransaction(entry);
			if (transaction == null) {
				logger.info(String.format(" [%s] Received an transaction notification event for an unknown transaction. This should not happen! Doing nothing", transactionId));
				return;
			}
			if (transaction.getId() == null) {
				logger.info(String.format(" [%s] Received an transaction notification event for a transaction without id. This should not happen! Doing nothing", transactionId));
				return;
			}
			if (!transaction.getId().equals(transactionId)) {
				transactionId = transaction.getId();
			}
			String participantId = transaction.getParticipantId();
			List<TransactionNotification> notifications = notificationService.getNotificationsByTransaction(participantId, transactionId);
			if (notifications == null || notifications.isEmpty()) {
				logger.info(String.format("[%s][%s] Nothing to notify", transactionId, participantId));
			} else {
				notifications.forEach(n->sendHandleNotification(n, entry));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("[%s] Error dispatching messages", transactionId), e);
		}
	}

	/**
	 * Tries to get a {@link Transaction} for the given parameters.
	 * AssetTransactionEntries don't have a transaction id. They get the transaction using the 
	 * creator of the asset and the {@link TransactionType#GENESIS}.
	 * @param entry the transaction entry
	 * @return the {@link Transaction} instance or <code>null</code>
	 * @throws an {@link IllegalStateException} in case no {@link Transaction} was found
	 */
	private Transaction getTransaction(TransactionEntry entry) {
		String transactionId = entry.getTransactionId();
		transactionId = transactionId == null || "assetTransaction".equals(transactionId) ? null : transactionId;
		Transaction transaction = null;
		if (transactionId == null) {
			/*
			 */
			if (entry instanceof AssetTransactionEntry) {
				AssetTransactionEntry ate = (AssetTransactionEntry) entry;
				String owner = ate.getAsset().getOwnerId();
				List<Transaction> transactions = transactionService.getTransactionsByType(owner, TransactionType.ASSET);
				if (!transactions.isEmpty()) {
					transaction = transactions.stream()
							.filter(t->t instanceof AssetTransaction)
							.map(t->(AssetTransaction)t)
							.filter(at->at.getChangeType().equals(ate.getChangeType()))
							.findFirst()
							.orElse(null);
					
				}
			} else {
				throw new IllegalStateException("Cannot find a transaction for a null transaction id");
			}
		} else {
			transaction = transactionService.getTransactionById(transactionId);
		}
		return transaction;
	}

	/**
	 * Send an event to handle notification of a certain contact type
	 * @param notification the notification to use
	 * @param entry the {@link TransactionEntry} to be handled
	 */
	private void sendHandleNotification(TransactionNotification notification, TransactionEntry entry) {
		if (notification.getContact() == null) {
			logger.info("Received an transaction notification without contact reference. This should not happen! Doing nothing");
			return;
		}
		Map<String, Object> eventProperties = new HashMap<String, Object>();
		eventProperties.put("notification", notification);
		eventProperties.put("entry", entry);
		Event event = new Event("notification/" + notification.getContact().getType(), eventProperties);
		eventAdmin.postEvent(event);
	}


}

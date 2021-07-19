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
package org.gecko.notary.service.api;

import java.util.List;

import org.gecko.notary.model.notary.TransactionNotification;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Service to handle participant definitions notifications
 * @author Mark Hoffmann
 * @since 23.08.2019
 */
@ProviderType
public interface TransactionNotificationService {
	
	/**
	 * Update or creates a notification with the given data
	 * @param participantId the participant this notification belongs to
	 * @param notification the notification object
	 * @return the updated {@link TransactionNotification} instance
	 */
	public TransactionNotification updateNotification(String participantId, TransactionNotification notification);
	
	/**
	 * Removed a notification object for the given participant
	 * @param participantId the participant this notification belongs to
	 * @param notificationId the notification
	 * @return <code>true</code>, if deletion was successful
	 */
	public boolean removeNotification(String participantId, String notificationId);
	
	/**
	 * Returns all notifications for that participant or an empty {@link List}
	 * @param participantId the participant, to get notifications from
	 * @return all notifications for that participant or an empty {@link List}
	 */
	public List<TransactionNotification> getNotifications(String participantId);
	
	/**
	 * Returns all notifications for that participant and transaction or an empty {@link List}
	 * @param participantId the participant, to get notifications from
	 * @param transactionId the transaction to get notifications for
	 * @return all notifications for that participant and transaction or an empty {@link List}
	 */
	public List<TransactionNotification> getNotificationsByTransaction(String participantId, String transactionId);

}

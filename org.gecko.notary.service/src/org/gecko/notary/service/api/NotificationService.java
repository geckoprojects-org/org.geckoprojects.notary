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
import java.util.Set;

import org.gecko.notary.model.notary.Notification;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Handles notifications
 * @author Mark Hoffmann
 * @since 02.10.2019
 */
@ProviderType
public interface NotificationService {
		
		/**
		 * Get {@link Notification} for a given sender
		 * @param participantId the sender participant
		 * @return the list of {@link Notification} or an empty list
		 */
		public List<Notification> getNotificationsBySender(String participantId);
		
		/**
		 * Get {@link Notification} for a given receiver
		 * @param participantId the receivers participant
		 * @return the list of {@link Notification} or an empty list
		 */
		public List<Notification> getNotificationsByReceiver(String participantId);
		
		/**
		 * Updates a given {@link Notification} read value
		 * @param notificationId the {@link Notification} id
		 * @param read {@link Set} to <code>true</code>, to mark the {@link Notification} as read
		 * @return the updated {@link Notification} instance or <code>null</code>
		 */
		public Notification markRead(String notificationId, boolean read);
		
}

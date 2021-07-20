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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.Notification;
import org.gecko.notary.service.api.NotificationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Implementation for the notification service
 * @author Mark Hoffmann
 * @since 02.10.2019
 */
@Component(scope = ServiceScope.PROTOTYPE)
public class NotificationServiceImpl implements NotificationService {

	@Reference(scope=ReferenceScope.PROTOTYPE_REQUIRED, target="(repo_id=notary.notary)")
	private EMFRepository repository;
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.NotificationService#getNotificationsBySender(java.lang.String)
	 */
	@Override
	public List<Notification> getNotificationsBySender(String participantId) {
		return getNotificationsByFeature(NotaryPackage.Literals.NOTIFICATION__SENDER_ID, participantId);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.NotificationService#getNotificationsByReceiver(java.lang.String)
	 */
	@Override
	public List<Notification> getNotificationsByReceiver(String participantId) {
		return getNotificationsByFeature(NotaryPackage.Literals.NOTIFICATION__RECEIPIENT_ID, participantId);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.NotificationService#markRead(java.lang.String, boolean)
	 */
	@Override
	public Notification markRead(String notificationId, boolean read) {
		if(notificationId == null) {
			throw new IllegalStateException("Cannot update a null Notification ID.");
		}
		Notification notification = repository.getEObject(NotaryPackage.Literals.NOTIFICATION, notificationId);
		if (notification == null) {
			return null;
		}
		if (read != notification.isRead()) {
			notification.setRead(read);
			repository.save(notification);
		}
		return notification;
	}
	
	/**
	 * Executes a query against one {@link EStructuralFeature}
	 * @param attribute the feature
	 * @param value the value for the feature
	 * @return the list of {@link Notification}'s or an empty list
	 */
	private List<Notification> getNotificationsByFeature(EAttribute attribute, String value) {
		if(value == null) {
			throw new IllegalStateException("Cannot retrieve notifications for a null feature ID.");
		}
		QueryRepository queryRepository = (QueryRepository) repository;        
		IQueryBuilder queryBuilder = queryRepository.createQueryBuilder(); //here we are creating the query		
		queryBuilder.column(attribute).simpleValue(value);
		IQuery query = queryBuilder.build();
		return queryRepository
				.getEObjectsByQuery(NotaryPackage.Literals.NOTIFICATION, query);
	}

}

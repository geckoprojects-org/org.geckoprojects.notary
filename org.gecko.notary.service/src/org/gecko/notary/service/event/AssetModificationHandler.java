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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.service.api.TransactionEntryService;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Event handler that creates transaction entries for {@link Asset}'s
 * @author Mark Hoffmann
 * @since 01.10.2019
 */
@Component(name = "AssetTransactionEntryWorker", service = EventHandler.class, immediate = true, property = { EventConstants.EVENT_TOPIC + "=asset/modification" })
public class AssetModificationHandler implements EventHandler {

	private static final Logger logger = Logger.getLogger(AssetModificationHandler.class.getName());
	@Reference
	private ComponentServiceObjects<TransactionEntryService> transactionEntrySO;

	/* 
	 * (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		Asset current = (Asset) event.getProperty("current");
		Asset assetNew = (Asset) event.getProperty("new");
		if (assetNew == null) {
			logger.info("Received an asset event with a null 'new asset'. This should not happen! Doing nothing");
			return;
		}
		TransactionEntryService transactionEntryService = transactionEntrySO.getService();
		try {
			transactionEntryService.createAssetModificationTransaction(current, assetNew);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error creating asset modification", e);
		} finally {
			transactionEntrySO.ungetService(transactionEntryService);
		}
	}

}

/**
 * Copyright (c) 2012 - 2020 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.impl.textprovider;

import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
import org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation for {@link TransactionEntry} provider for texts
 * @author Mark Hoffmann
 * @since 19.03.2020
 */
@Component(service = TextProvider.class, property = {"object=TransactionEntry", "target=TransactionEntry"})
public class TransactionEntryTextProvider extends AbstractTransactionEntryTextProvider {

	@Reference
	private TransactionService transactionService;
	@Reference
	private ParticipantService participantService;

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider#getParticipantService()
	 */
	protected ParticipantService getParticipantService() {
		return participantService;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider#getTransactionService()
	 */
	protected TransactionService getTransactionService() {
		return transactionService;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider#doCreateComment(org.gecko.notary.model.notary.TransactionEntry)
	 */
	@Override
	protected String doCreateComment(TransactionEntry entry) {
		return null;
	}
	
}

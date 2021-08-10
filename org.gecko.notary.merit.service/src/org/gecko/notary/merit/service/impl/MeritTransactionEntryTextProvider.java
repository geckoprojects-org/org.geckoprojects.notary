/**
 * Copyright (c) 2012 - 2021 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.merit.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.gecko.notary.merit.model.merit.AchievementEntry;
import org.gecko.notary.merit.model.merit.BetResultType;
import org.gecko.notary.merit.model.merit.BettingEntry;
import org.gecko.notary.merit.model.merit.MeritPackage;
import org.gecko.notary.merit.model.merit.PurchaseEntry;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
import org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Transaction entry text provider that can handle texts for certain Merit-Model {@link TransactionEntry} classes
 * @author Mark Hoffmann
 * @since 23.07.2021
 */
@Component(service = TextProvider.class, property = {"object=TransactionEntry", "target=TransactionEntry", "domain=Merit", "service.rank=100"})
public class MeritTransactionEntryTextProvider extends AbstractTransactionEntryTextProvider {

	public static final String PURCHASE_LABEL = "[%s] An amount of %s merits has been purchased from provider %s with transaction id: '%s'";
	public static final String PLACE_BET_LABEL = "[%s] A bet was placed with a stake of %s merits for bet '%s'";
	public static final String BET_RESULT_LABEL = "[%s] The bet '%s' was finished with a result %s and a stake of %s merits";
	public static final String ACHIEVEMTENT_RESULT_LABEL = "[%s] An achievement '%s' of %s merits was eared";
	public static final String BADGE_LABEL_TEMPLATE = "Badge of: %s";
	public static final String BADGE_DESCRIPTION_TEMPLATE = "Owner: %s with current merit point amount: %s";
	
	@Reference
	private TransactionService transactionService;
	@Reference
	private ParticipantService participantService;
	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider#getParticipantService()
	 */
	@Override
	protected ParticipantService getParticipantService() {
		return participantService;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider#getTransactionService()
	 */
	@Override
	protected TransactionService getTransactionService() {
		return transactionService;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.AbstractTransactionEntryTextProvider#doCreateComment(org.gecko.notary.model.notary.TransactionEntry)
	 */
	protected String doCreateComment(TransactionEntry entry) {
		List<EStructuralFeature> features = new LinkedList<>();
		String text = null;
		if (entry instanceof PurchaseEntry) {
			features.add(MeritPackage.Literals.PURCHASE_ENTRY__PURCHASE_TIME);
			features.add(MeritPackage.Literals.PURCHASE_ENTRY__AMOUNT);
			features.add(MeritPackage.Literals.PURCHASE_ENTRY__PURCHASE_PROVIDER);
			features.add(MeritPackage.Literals.PURCHASE_ENTRY__PURCHASE_TRANSACTION_ID);
			text = PURCHASE_LABEL;
		} else if (entry instanceof BettingEntry) {
			BetResultType result = ((BettingEntry)entry).getResult();
			features.add(NotaryPackage.Literals.TRANSACTION_ENTRY__TIMESTAMP);
			if (BetResultType.INITIAL_BET.equals(result)) {
				features.add(MeritPackage.Literals.BETTING_ENTRY__STAKE);
				features.add(MeritPackage.Literals.BETTING_ENTRY__BET_IDENTIFIER);
				text = PLACE_BET_LABEL;
			} else {
				features.add(MeritPackage.Literals.BETTING_ENTRY__BET_IDENTIFIER);
				features.add(MeritPackage.Literals.BETTING_ENTRY__RESULT);
				features.add(MeritPackage.Literals.BETTING_ENTRY__STAKE);
				text = BET_RESULT_LABEL;
			}
		} else if (entry instanceof AchievementEntry) {
			features.add(NotaryPackage.Literals.TRANSACTION_ENTRY__TIMESTAMP);
			features.add(MeritPackage.Literals.ACHIEVEMENT_ENTRY__DESCRIPTION);
			features.add(MeritPackage.Literals.ACHIEVEMENT_ENTRY__MERIT_AMOUNT);
			text = ACHIEVEMTENT_RESULT_LABEL;
		} else if (entry instanceof AssetTransactionEntry) {
			return super.createComment(entry);
		}
		return text == null ? null : getText(text, entry, features);
	}
	
}

/**
 * Copyright (c) 2012 - 2021 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.merit.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.gecko.notary.merit.model.merit.BetResultType;
import org.gecko.notary.merit.model.merit.MeritPackage;
import org.gecko.notary.merit.model.merit.PurchaseEntry;
import org.gecko.notary.merit.model.merit.BettingEntry;
import org.gecko.notary.merit.model.merit.AchievementEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Transaction entry text provider that can handle texts for certain Merit-Model {@link TransactionEntry} classes
 * @author Mark Hoffmann
 * @since 23.07.2021
 */
@Component(property = {"object=TransactionEntry", "target=TransactionEntry", "domain=Merit", "service.rank=100"})
public class MeritTransactionEntryTextProvider implements TextProvider {

	private static final Logger logger = Logger.getLogger(MeritTransactionEntryTextProvider.class.getName());
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
	private static final String PURCHASE_LABEL = "[%s] An amount of %s merits has been purchased from provider %s with transaction id: '%s'";
	private static final String PLACE_BET_LABEL = "[%s] A bet was placed with a stake of %s merits for bet '%s'";
	private static final String BET_RESULT_LABEL = "[%s] The bet '%s' was finisned with a result %s and a stake of %s merits";
	private static final String ACHIEVEMTENT_RESULT_LABEL = "[%s] An achievement '%s' of %s merits was eared";
	public static final String BADGE_LABEL_TEMPLATE = "Badge of: %s";
	public static final String BADGE_DESCRIPTION_TEMPLATE = "Owner: %s with current merit point amount: %s";
	
	@Reference
	private TransactionService transactionService;
	@Reference
	private ParticipantService participantService;
	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.TextProvider#provideText(org.eclipse.emf.ecore.EObject, java.util.Map)
	 */
	@Override
	public String provideText(EObject object, Map<String, Object> properties) {
		if (!(object instanceof TransactionEntry)) {
			logger.log(Level.WARNING, "Object is not of instance TransactionEntry. Returning without result");
			return null;
		}
		TransactionEntry te = (TransactionEntry) object;
		String transactionId = te.getTransactionId();
		// Set transaction information
		Transaction t = transactionService.getTransactionById(transactionId);
		if (t == null) {
			if (te instanceof AssetTransactionEntry) {
				t = NotaryFactory.eINSTANCE.createTransaction();
				t.setDescription("Asset Veränderung");
			} else {
				logger.log(Level.WARNING, ()->String.format("[%s] No transaction found. Returning without result", transactionId));
				return null;
			}
		}
		te.setLabel(t.getDescription());
		// Set source information
		Participant p = participantService.getParticipant(te.getParticipantId());
		if (p == null) {
			logger.log(Level.WARNING, ()->String.format("[%s] No participant found. Cannot set source information", te.getParticipantId()));
		} else {
			String source = p.getDescription();
			if (source == null) {
				source = p.getName();
			}
			te.setSource(source);
		}
		String comment = createComment(te);
		if (comment != null) {
			te.setComment(comment);
			return comment;
		}
		return null;
	}
	
	/**
	 * Creates a comment for a certain {@link TransactionEntry}
	 * @param entry the 
	 * @param transaction
	 * @param participant
	 * @return
	 */
	private String createComment(TransactionEntry entry) {
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
			AssetTransactionEntry ate = (AssetTransactionEntry) entry;
			features.add(NotaryPackage.Literals.TRANSACTION_ENTRY__ASSET_ID);
			features.add(NotaryPackage.Literals.ASSET_TRANSACTION_ENTRY__ASSET);
			switch (ate.getChangeType()) {
			case CREATION:
				text = CREATION_TEMLPATE;
				break;
			case DESTRUCTION:
				text = DESTRUCTION_TEMLPATE;
				break;
			case JOIN:
				features.add(NotaryPackage.Literals.ASSET_TRANSACTION_ENTRY__PARENT_ASSET_ID);
				text = JOIN_TEMPLATE;
				break;
			case MODIFICATION:
				text = MODIFICATON_TEMPLATE;
				break;
			case OWNERSHIP:
				features.add(NotaryPackage.Literals.ASSET_TRANSACTION_ENTRY__CHANGE_DATA);
				features.add(NotaryPackage.Literals.TRANSACTION_ENTRY__PARTICIPANT_ID);
				text = OWNERSHIP_TEMPLATE;
				break;
			case SPLIT:
				features.add(NotaryPackage.Literals.ASSET_TRANSACTION_ENTRY__PARENT_ASSET_ID);
				text = SPLIT_TEMPLATE;
				break;
			default:
				break;
			}
		}
		return text == null ? null : getText(text, entry, features);
	}
	
	/**
	 * Returns the formatted text from the given template string, using the entry parameter as object
	 * and the list of {@link EStructuralFeature} as provider for the string formatting parameters.
	 * @param template the string template 
	 * @param entry the {@link EObject}, to get values from
	 * @param features the features to get value for
	 * @return the formatted {@link String}
	 */
	private String getText(String template, TransactionEntry entry, List<EStructuralFeature> features) {
		if (template == null || entry == null) {
			return null;
		}
		if (features == null || features.isEmpty()) {
			return template;
		}
		Object[] values = features.stream()
				.map(entry::eGet)
				.map(this::mapToString)
				.collect(Collectors.toList())
				.toArray();
		return String.format(template, values);
	}
	
	private String mapToString(Object object) {
		if (object == null) {
			return "<n/a>";
		}
		if (object instanceof Date) {
			return SDF.format(object);
		} else if (object instanceof EObject) {
			return ((EObject)object).eClass().getName();
		} else {
			return object.toString();
		}
	}

}

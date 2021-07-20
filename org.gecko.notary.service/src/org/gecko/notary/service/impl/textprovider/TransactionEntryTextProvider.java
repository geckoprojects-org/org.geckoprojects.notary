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
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.ParentAssetEntry;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation for {@link TransactionEntry} provider for texts
 * @author Mark Hoffmann
 * @since 19.03.2020
 */
@Component(property = {"object=TransactionEntry", "target=TransactionEntry"})
@SuppressWarnings({"java:S125", "java:S1172"})
public class TransactionEntryTextProvider implements TextProvider {

	private static final Logger logger = Logger.getLogger(TransactionEntryTextProvider.class.getName());
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	@Reference
	private TransactionService transactionService;
	@Reference
	private ParticipantService participantService;

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TextProvider#provideText(org.eclipse.emf.ecore.EObject, java.util.Map)
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
		String comment = createComment(te, t, p);
		if (comment != null)
			te.setComment(comment);
		return comment;
	}

	/**
	 * Creates a comment for a certain {@link TransactionEntry}
	 * @param entry the 
	 * @param transaction
	 * @param participant
	 * @return
	 */
	private String createComment(TransactionEntry entry, Transaction transaction, Participant participant) {
		List<EStructuralFeature> features = new LinkedList<>();
		String text = null;
//		if (entry instanceof OutboundLogistic) {
//			features.add(DiamantPackage.Literals.OUTBOUND_LOGISTIC__TARGET_ADDRESS);
//			features.add(DiamantPackage.Literals.OUTBOUND_LOGISTIC__TRANSPORTATION_PROVIDER);
//			features.add(DiamantPackage.Literals.OUTBOUND_LOGISTIC__TRANSPORTATION_TRACKING_ID);
//			features.add(DiamantPackage.Literals.TRANSACTION_ENTRY__ASSET_ID);
//			text = OUTBOUND_TEMPLATE;
//		} else if (entry instanceof SupportEntry) {
//			SupportEntry se = (SupportEntry) entry;
//			features.add(DiamantPackage.Literals.SUPPORT_ENTRY__TEXT);
//			if (se.getActionEntryId() == null) {
//				text = SUPPORT_NOACTION_TEMPLATE;
//			} else {
//				text = SUPPORT_ACTION_TEMPLATE;
//				features.add(DiamantPackage.Literals.SUPPORT_ENTRY__ACTION_ENTRY_LABEL);
//			}
//		} else if (entry instanceof Gauge) {
//			features.add(DiamantPackage.Literals.GAUGE__EXECUTING_AUTHORITY);
//			features.add(DiamantPackage.Literals.GAUGE__CERTIFICATE_ID);
//			features.add(DiamantPackage.Literals.GAUGE__VALID_TO);
//			text = GAUGE_TEMPLATE;
//		} else if (entry instanceof InboundLogistic) {
//			features.add(DiamantPackage.Literals.INBOUND_LOGISTIC__ARTICLE_NUMBER);
//			features.add(DiamantPackage.Literals.TRANSACTION_ENTRY__ASSET_ID);
//			text = INBOUND_TEMPLATE;
//		} else if (entry instanceof SellingContract) {
//			features.add(DiamantPackage.Literals.SELLING_CONTRACT__CUSTOMER);
//			features.add(DiamantPackage.Literals.SELLING_CONTRACT__CONTRACT_ID);
//			features.add(DiamantPackage.Literals.SELLING_CONTRACT__CONTRACT_TEXT);
//			features.add(DiamantPackage.Literals.TRANSACTION_ENTRY__ASSET_ID);
//			text = SELLING_TEMPLATE;
//		} else if (entry instanceof ParentAssetEntry) {
		if (entry instanceof ParentAssetEntry) {
			features.add(NotaryPackage.Literals.PARENT_ASSET_ENTRY__PARENT_ASSET_ID);
			features.add(NotaryPackage.Literals.TRANSACTION_ENTRY__ASSET_ID);
			text = PARENT_TEMLPATE;
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
			return sdf.format(object);
		} else if (object instanceof EObject) {
			return ((EObject)object).eClass().getName();
		} else {
			return object.toString();
		}
	}

}

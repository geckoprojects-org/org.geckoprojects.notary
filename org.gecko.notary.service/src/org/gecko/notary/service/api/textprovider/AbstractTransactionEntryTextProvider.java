package org.gecko.notary.service.api.textprovider;

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

/**
 * Implementation for {@link TransactionEntry} provider for texts
 * @author Mark Hoffmann
 * @since 19.03.2020
 */
public abstract class AbstractTransactionEntryTextProvider implements TextProvider {

	private static final Logger logger = Logger.getLogger(AbstractTransactionEntryTextProvider.class.getName());
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	
	protected abstract String doCreateComment(TransactionEntry entry);
	
	protected abstract TransactionService getTransactionService();
	
	protected abstract ParticipantService getParticipantService();
	
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
		TransactionService transactionService = getTransactionService();
		if (transactionService == null) {
			throw new IllegalStateException("No transaction service is available");
		}
		ParticipantService participantService = getParticipantService();
		if (participantService == null) {
			throw new IllegalStateException("No participant service is available");
		}
		TransactionEntry te = (TransactionEntry) object;
		String transactionId = te.getTransactionId();
		// Set transaction information
		Transaction t = transactionService.getTransactionById(transactionId);
		if (t == null) {
			if (te instanceof AssetTransactionEntry) {
				t = NotaryFactory.eINSTANCE.createTransaction();
				t.setDescription(ASSET_DEFAULT_LABEL);
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
		if (comment != null)
			te.setComment(comment);
		return comment;
	}

	/**
	 * Creates a comment for a certain {@link TransactionEntry}
	 * @param entry the 
	 * @return
	 */
	protected String createComment(TransactionEntry entry) {
		List<EStructuralFeature> features = new LinkedList<>();
		String text = null;
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
		} else {
			return doCreateComment(entry);
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
	protected String getText(String template, TransactionEntry entry, List<EStructuralFeature> features) {
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

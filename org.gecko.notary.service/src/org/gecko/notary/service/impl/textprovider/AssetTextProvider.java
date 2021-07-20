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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetInfo;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation for {@link Asset} provider for texts
 * @author Mark Hoffmann
 * @since 19.03.2020
 */
@Component(property = {"object=Asset", "target=Asset"})
@SuppressWarnings("java:S125")
public class AssetTextProvider implements TextProvider {

	private static final Logger logger = Logger.getLogger(AssetTextProvider.class.getName());
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	@Reference
	private ParticipantService participantService;

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TextProvider#provideText(org.eclipse.emf.ecore.EObject, java.util.Map)
	 */
	@Override
	public String provideText(EObject object, Map<String, Object> properties) {
		if (!(object instanceof Asset)) {
			logger.log(Level.WARNING, "Object is not of instance Asset. Returning without result");
			return null;
		}
		Asset asset = (Asset) object;
		// We already have data in our info object. We use that.
		if (asset.getInfo() != null) {
			return null;
		}
		Participant creator = participantService.getParticipant(asset.getCreatorId());
		Participant owner = participantService.getParticipant(asset.getOwnerId());
		AssetInfo info = NotaryFactory.eINSTANCE.createAssetInfo();
		try {
			info.setAssetTypeLabel(getTypeLabel(asset));
			asset.getTransactionDesc().forEach(s->info.getTransactionDesc().add(s));
			info.setLabel(createLabel(asset));
			info.setDescription(createDescription(asset, creator,  owner));
			info.setAssetIdentifier(createIdentifier(asset));
			asset.setInfo(info);
			return info.toString();
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("[%s] Error creating asset info text", asset.getId()), e);
		}
		return null;
	}

	/**
	 * Returns the type label for an asset
	 * @param asset the asset to get the label for
	 * @return the {@link EClass} name of the instance
	 */
	private String getTypeLabel(Asset asset) {
		return asset.eClass().getName();
	}

	/**
	 * Creates a comment for a certain {@link TransactionEntry}
	 * @param asset the asset
	 * @return the label string or <code>null</code>
	 */
	private String createLabel(Asset asset) {
		List<EStructuralFeature> features = new LinkedList<>();
		String text = null;
//		if (asset instanceof Product) {
//			features.add(DiamantPackage.Literals.PRODUCT__DESCRIPTION);
//			features.add(DiamantPackage.Literals.PRODUCT__TYPE);
//			features.add(DiamantPackage.Literals.PRODUCT__VENDOR);
//			text = PRODUCT_LABEL_TEMPLATE;
//		} else if (asset instanceof SupportCase) {
//			features.add(DiamantPackage.Literals.SUPPORT_CASE__LABEL);
//			features.add(DiamantPackage.Literals.SUPPORT_CASE__SUPPORT_ASSET_ID);
//			text = SUPPORT_CASE_LABEL_TEMPLATE;
//		} else if (asset instanceof Treatment) {
//			features.add(DiamantPackage.Literals.TREATMENT__NAME);
//			features.add(DiamantPackage.Literals.TREATMENT__PATIENT_INSURANCE_ID);
//			text = TREATMENT_LABEL_TEMPLATE;
//		}
		features.add(NotaryPackage.Literals.ASSET__ID);
		features.add(NotaryPackage.Literals.ASSET__CREATOR_ID);
		features.add(NotaryPackage.Literals.ASSET__OWNER_ID);
		text = ASSET_LABEL_TEMPLATE;
		return text == null ? null : getText(text, asset, features);
	}

	/**
	 * Creates a comment for a certain {@link TransactionEntry}
	 * @param entry the 
	 * @param transaction
	 * @param participant
	 * @return
	 */
	@SuppressWarnings({"java:S1172", "java:S2583"})
	private String createDescription(Asset asset, Participant creator, Participant owner) {
//		List<EStructuralFeature> features = new LinkedList<>();
		String text = null;
//		if (asset instanceof Product) {
//			features.add(DiamantPackage.Literals.PRODUCT__ARTICLE_NUMBER_REF);
//			features.add(DiamantPackage.Literals.PRODUCT__SERIAL_NUMBER);
//			text = PRODUCT_DESCRIPTION_TEMPLATE;
//		} else if (asset instanceof SupportCase) {
//			features.add(DiamantPackage.Literals.SUPPORT_CASE__CUSTOMER_CONTACT);
//			features.add(DiamantPackage.Literals.SUPPORT_CASE__DESCRIPTION);
//			features.add(DiamantPackage.Literals.SUPPORT_CASE__STATUS);
//			text = SUPPORT_CASE_DESCRIPTION_TEMPLATE;
//		} else if (asset instanceof Treatment) {
//			features.add(DiamantPackage.Literals.TREATMENT__DESCRIPTION);
//			features.add(DiamantPackage.Literals.TREATMENT__ROOM);
//			text = TREATMENT_DESCRIPTION_TEMPLATE;
//		}
//		if (text != null) {
//			text = getText(text, asset, features);
//			if (creator != null) {
//				text = text.replace("${CREATOR}", creator.getDescription());
//			}
//			if (owner != null) {
//				text = text.replace("${OWNER}", owner.getDescription());
//			}
//		}
		return text == null ? null : text;
	}

	/**
	 * Creates the product identifier
	 * @param asset the asset to get the identifier for
	 * @return the identifier
	 */
	@SuppressWarnings("java:S2583")
	private String createIdentifier(Asset asset) {
		List<EStructuralFeature> features = new LinkedList<>();
		String text = "%s";
//		if (asset instanceof Product) {
//			features.add(DiamantPackage.Literals.PRODUCT__SERIAL_NUMBER);
//		} else if (asset instanceof SupportCase) {
//			features.add(DiamantPackage.Literals.SUPPORT_CASE__TICKET_NUMBER);
//		} else if (asset instanceof Treatment) {
//			features.add(DiamantPackage.Literals.ASSET__ID);
//		}
		return text == null ? null : getText(text, asset, features);
	}

	/**
	 * Returns the formatted text from the given template string, using the entry parameter as object
	 * and the list of {@link EStructuralFeature} as provider for the string formatting parameters.
	 * @param template the string template 
	 * @param entry the {@link EObject}, to get values from
	 * @param features the features to get value for
	 * @return the formatted {@link String}
	 */
	private String getText(String template, EObject entry, List<EStructuralFeature> features) {
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

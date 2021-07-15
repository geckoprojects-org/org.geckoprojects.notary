/**
 * Copyright (c) 2012 - 2020 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.api.textprovider;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Text provider that provides a text for EObjects
 * @author mark
 * @since 19.03.2020
 */
@ProviderType
public interface TextProvider {
	
	public static final String ASSET_LABEL_TEMPLATE = "Asset: '%s', Creator: %s, Owner: %s";
	public static final String PRODUCT_LABEL_TEMPLATE = "Produkt: '%s', Typ: %s, Hersteller: %s";
	public static final String PRODUCT_DESCRIPTION_TEMPLATE = "Artikelnummer: %s, Seriennummer: %s, Ersteller: ${CREATOR}, Besitzer: ${OWNER}";
	public static final String TREATMENT_LABEL_TEMPLATE = "Behandlung: '%s', Versicherungsnummer: %s";
	public static final String TREATMENT_DESCRIPTION_TEMPLATE = "Beschreibung: %s, Ort: %s, Instanz: ${CREATOR}";
	public static final String SUPPORT_CASE_LABEL_TEMPLATE = "Betreff: '%s', Produkt: %s";
	public static final String SUPPORT_CASE_DESCRIPTION_TEMPLATE = "Kontakt: %s, Beschreibung: %s, Status: %s, Zugewiesen: ${OWNER}";
	
	public static final String OUTBOUND_TEMPLATE = "Warensendung nach %s, mittels Dienstleister %s (TrackingNo: %s, Asset: %s)";
	public static final String INBOUND_TEMPLATE = "Eingangsbuchung auf Artikelnummer %s (%s)";
	public static final String GAUGE_TEMPLATE = "Eichung durch %s (Zertifikat '%s' gültig bis %s";
	public static final String SUPPORT_ACTION_TEMPLATE = "%s (Aktivität: %s)";
	public static final String SUPPORT_NOACTION_TEMPLATE = "%s";
	public static final String SELLING_TEMPLATE = "Kunde: %s, Vertragsnummer %s, Details: %s (Asset: %s)";
	public static final String PARENT_TEMLPATE = "Asset %s wurde an Asset %s gehängt";
	public static final String CREATION_TEMLPATE = "Das Asset %s vom Typ %s wurde erstellt";
	public static final String JOIN_TEMPLATE = "Das Asset %s vom Typ %s wurde dem Asset %s zugeordnet";
	public static final String SPLIT_TEMPLATE = "Das Asset %s vom Typ %s vom Asset %s gelöst";
	public static final String MODIFICATON_TEMPLATE = "Der Besitzer von Asset %s vom Typ %s wurde geändert";
	public static final String OWNERSHIP_TEMPLATE = "Der Besitzer des Asset %s vom Typ %s wurde von '%s' auf '%s'";
	public static final String DESTRUCTION_TEMLPATE = "Das Asset %s vom Typ %s wurde gelöscht";
	public static final String ASSET_DEFAULT_LABEL = "Asset Veränderung";
	
	/**
	 * Provide the text for the given object and properties
	 * @param object the object to provide text for
	 * @param properties optional properties
	 * @return the text or <code>null</code>
	 */
	String provideText(EObject object, Map<String, Object> properties);

}

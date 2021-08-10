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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.gecko.notary.merit.model.merit.Badge;
import org.gecko.notary.merit.model.merit.MeritPackage;
import org.gecko.notary.model.notary.AssetInfo;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Text provider to create texts for Merit-specific Assets like the Badge
 * @author Mark Hoffmann
 * @since 23.07.2021
 */
@Component(property = {"object=Asset", "target=Asset", "domain=Merit", "service.rank=100"})
public class MeritTextProvider implements TextProvider {

	private static final Logger logger = Logger.getLogger(MeritTextProvider.class.getName());
	public static final String BADGE_LABEL_TEMPLATE = "Badge of: %s";
	public static final String BADGE_DESCRIPTION_TEMPLATE = "Owner: %s with current merit point amount: %s";
	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.service.api.textprovider.TextProvider#provideText(org.eclipse.emf.ecore.EObject, java.util.Map)
	 */
	@Override
	public String provideText(EObject object, Map<String, Object> properties) {
		if (!(object instanceof Badge)) {
			logger.log(Level.WARNING, "Asset is not of instance Badge. Cannot create text");
			return null;
		}
		Badge badge = (Badge) object;
		// We already have data in our info object. We use that.
		if (badge.getInfo() != null) {
			return null;
		}
		String owner = badge.getOwnerId() == null ? "<n/a>" : badge.getOwnerId();
		AssetInfo info = NotaryFactory.eINSTANCE.createAssetInfo();
		try {
			info.setAssetTypeLabel(MeritPackage.Literals.BADGE.getName());
			badge.getTransactionDesc().forEach(s->info.getTransactionDesc().add(s));
			info.setLabel(String.format(BADGE_LABEL_TEMPLATE, owner));
			info.setDescription(String.format(BADGE_DESCRIPTION_TEMPLATE, owner, badge.getMeritPoints()));
			info.setAssetIdentifier(badge.getId());
			badge.setInfo(info);
			return info.toString();
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("[%s] Error creating asset info text", badge.getId()), e);
		}
		return null;
	}

}

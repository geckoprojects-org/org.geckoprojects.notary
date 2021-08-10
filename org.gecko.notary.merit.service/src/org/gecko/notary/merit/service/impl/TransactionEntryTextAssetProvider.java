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
package org.gecko.notary.merit.service.impl;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation for {@link TransactionEntry} provider for texts
 * @author Mark Hoffmann
 * @since 19.03.2020
 */
@Component(property = {"object=TransactionEntry", "target=Asset"})
public class TransactionEntryTextAssetProvider implements TextProvider {

	/* 
	 * (non-Javadoc)
	 * @see de.dim.diamant.service.api.TextProvider#provideText(org.eclipse.emf.ecore.EObject, java.util.Map)
	 */
	@Override
	public String provideText(EObject object, Map<String, Object> properties) {
		return null;
	}

}
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
package org.gecko.notary.service.api.textprovider;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;

/**
 * Text provider that can be used for 
 * @author Mark Hoffmann
 * @since 21.07.2021
 */
public interface CustomTextProvider extends TextProvider {
	
	public static final String TYPE_PROPERTY = "textProviderType";
	public static final String ASSET_TEXT_PROVIDER = "ASSET";
	public static final String ASSET_TRANSACTION_TEXT_PROVIDER = "ASSET_TRANSACTION";
	public static final String TRANSACTION_TEXT_PROVIDER = "TRANSACTION";
	
	/**
	 * Returns <code>true</code>, if this custom text provider can provide a text for the given {@link EObject} and property map
	 * @param object the provided {@link EObject}
	 * @param properties the property map
	 * @return <code>true</code>, if the object can be handled to provide a text
	 */
	boolean canHandle(EObject object, Map<Object, Object> properties);

}

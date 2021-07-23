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
	public static final String ASSET_DESCRIPTION_TEMPLATE = "Asset: '%s', Creator: ${CREATOR}, Owner: ${OWNER}";
	
	public static final String PARENT_TEMLPATE = "Asset %s has been attached to asset %s";
	public static final String CREATION_TEMLPATE = "Asset %s of type %s has been created";
	public static final String JOIN_TEMPLATE = "Asset %s of type %s has been assigned to parental asset %s";
	public static final String SPLIT_TEMPLATE = "Asset %s of type %s has been detached from asset %s";
	public static final String MODIFICATON_TEMPLATE = "The asset %s of type %s has been modified";
	public static final String OWNERSHIP_TEMPLATE = "The owner of asset %s of type %s has changed from '%s' to '%s'";
	public static final String DESTRUCTION_TEMLPATE = "The asset %s of type %s has been destructed";
	public static final String ASSET_DEFAULT_LABEL = "Asset Change";
	
	/**
	 * Provide the text for the given object and properties
	 * @param object the object to provide text for
	 * @param properties optional properties
	 * @return the text or <code>null</code>
	 */
	String provideText(EObject object, Map<String, Object> properties);

}

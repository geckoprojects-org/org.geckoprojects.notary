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
package org.gecko.notary.merit.service.api;

import org.gecko.notary.merit.model.merit.Badge;
import org.gecko.notary.merit.model.merit.BetResultType;
import org.gecko.notary.merit.model.merit.PurchaseProvider;

/**
 * Service to Handle Merit points
 * @author Mark Hoffmann
 * @since 22.07.2021
 */
public interface MeritService {
	
	static final int PURCHASE_LIMIT = 100;
	
	static int getPurchaseLimit() {
		return PURCHASE_LIMIT;
	}
	
	/**
	 * Returns the basge of a given user. If no user was found an {@link IllegalStateException} is thrown.
	 * If no badge existed for a user, it will be initialized.
	 * @param userId the users id
	 * @return a badge, if it was found for the user. 
	 */
	Badge getBadge(String userId) throws IllegalStateException;
	
	
	/**
	 * Purchase the given amount of merits for the user and add the points to its badge 
	 * @param userId the user of the badge, who wants to purchase
	 * @param amount, the amount of merit point s to purchase
	 * @param provider the provider (Google or Apple) that wants to be used
	 * @return the {@link Badge}, with the new amount
	 * @throws IllegalStateException
	 */
	Badge purchaseMerits(String userId, int amount, PurchaseProvider provider) throws IllegalStateException;
	
	/**
	 * Earn merits when getting achievements. The points will be added to the users badge
	 * @param userId the user of the badge, who earned the points
	 * @param amount the amount of the earned points 
	 * @param reason the description for what achievement the user gets the points
	 * @return the {@link Badge} with the added points
	 * @throws IllegalStateException
	 */
	Badge earnMerits(String userId, int amount, String reason) throws IllegalStateException;
	
	/**
	 * Set a bet with a certain amount 
	 * @param userId the user, who wants to set a bet
	 * @param betId the id of the bet
	 * @param amount the amount to be set
	 * @return the {@link Badge} with the discounted amount
	 * @throws IllegalStateException
	 */
	Badge placeBet(String userId, String betId, int amount) throws IllegalStateException;
	
	/**
	 * Account the final bet result 
	 * @param userId the user, who get the corresponding result accounted
	 * @param betId the bet id
	 * @param amount the amount to add or substract
	 * @param result the bet result, whether LOST OR WON
	 * @return the {@link Badge} with the new calculated merit point account
	 */
	Badge setBetResult(String userId, String betId, int amount, BetResultType result);
	
	/**
	 * Returns <code>true</code>, if the badge have an account for the given amount, otherwise <code>false</code>
	 * @param user the users which badge needs to be checked
	 * @param amount the merit amount to check
	 * @return <code>true</code>, if all badges have an account for the given amount, otherwise <code>false</code>
	 * @throws IllegalStateException
	 */
	boolean validateBadge(String user, int amount) throws IllegalStateException;

}

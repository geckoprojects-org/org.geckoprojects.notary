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

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.notary.merit.model.merit.AchievementEntry;
import org.gecko.notary.merit.model.merit.Badge;
import org.gecko.notary.merit.model.merit.BetResultType;
import org.gecko.notary.merit.model.merit.BettingEntry;
import org.gecko.notary.merit.model.merit.MeritFactory;
import org.gecko.notary.merit.model.merit.MeritPackage;
import org.gecko.notary.merit.model.merit.PurchaseEntry;
import org.gecko.notary.merit.model.merit.PurchaseProvider;
import org.gecko.notary.merit.service.api.MeritService;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.AssetService;
import org.gecko.notary.service.api.TransactionEntryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Service Implementation for a simple merit service
 * @author Mark Hoffmann
 * @since 23.07.2021
 */
@Component
public class MeritServiceImpl implements MeritService {

	private static final Logger LOGGER = Logger.getLogger(MeritServiceImpl.class.getName());
	@Reference
	private AssetService assetService;
	@Reference
	private TransactionEntryService transactionEntryService;

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.merit.service.api.MeritService#getBadge(java.lang.String)
	 */
	@Override
	public Badge getBadge(String userId) throws IllegalStateException {
		if (userId == null) {
			throw new IllegalStateException("Cannot return merit badge without user");
		}
		try {
			Badge badge = (Badge) assetService.getAssetByParticipant(userId, userId, MeritPackage.Literals.BADGE);
			if (badge == null) {
				throw new IllegalStateException("There is no badge for this user. It seems that the given userId has no access to the merit point system.");
			} else {
				return badge;
			}
		} catch (Exception e) {
			throw new IllegalStateException("An error occured getting a badge for this user.", e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.merit.service.api.MeritService#purchaseMerits(String, int, PurchaseProvider)
	 */
	@Override
	public Badge purchaseMerits(String userId, int amount, PurchaseProvider provider) throws IllegalStateException {
		if (provider == null || PurchaseProvider.OTHER.equals(provider)) {
			throw new IllegalStateException("Cannot purchase merits without a valid provider");
		}
		if (amount < 0) {
			throw new IllegalStateException("Selling merits is not supported.");
		}
		if (amount > MeritService.getPurchaseLimit()) {
			throw new IllegalStateException("You want to purchase merits point out of the limit.");
		}
		return account(userId, 
				amount, 
				()->doPurchase(userId, amount, provider), 
				e->LOGGER.log(Level.SEVERE, String.format("Error purchasing %s merits for user %s from provider %s", amount, userId, provider.getName()),e));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.merit.service.api.MeritService#earnMerits(java.lang.String, int, java.lang.String)
	 */
	@Override
	public Badge earnMerits(String userId, int amount, String reason) throws IllegalStateException {
		if (reason == null) {
			throw new IllegalStateException("Cannot earn merits without a reason");
		}
		if (amount < 0) {
			throw new IllegalStateException("Losing merits is not supported.");
		}
		if (amount > MeritService.getPurchaseLimit()) {
			throw new IllegalStateException("You want to earn merits point out of the limit.");
		}
		return account(userId, 
				amount, 
				()->doEarn(userId, amount, reason), 
				e->LOGGER.log(Level.SEVERE, String.format("Error earning %s merits for user %s with reason '%s'", amount, userId, reason),e));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.merit.service.api.MeritService#setBet(java.lang.String, java.lang.String, int)
	 */
	@Override
	public Badge placeBet(String userId, String betId, int amount) throws IllegalStateException {
		if (betId == null) {
			throw new IllegalStateException("Cannot place a bet without a bet id");
		}
		if (amount < 0) {
			throw new IllegalStateException("Losing merits is not supported.");
		}
		if (amount > MeritService.getPurchaseLimit()) {
			throw new IllegalStateException("You want to place a bet with merits point out of the limit.");
		}
		// Just checking for a valid badge, otherwise we exit here
		Badge badge = getBadge(userId);
		if (amount > badge.getMeritPoints()) {
			throw new IllegalStateException("You dont have enough merit points to place a bet.");
		}
		if (amount == 0) {
			LOGGER.warning("Placing a bet with an amount of 0 makes no sense");
			return badge;
		}
		BettingEntry be = MeritFactory.eINSTANCE.createBettingEntry();
		be.setAssetId(userId);
		be.setParticipantId(userId);
		be.setTransactionId(MeritPackage.Literals.BET.getName());
		be.setStake(amount);
		be.setBetIdentifier(betId);
		be.setResult(BetResultType.INITIAL_BET);
		transactionEntryService.createTransactionEntry(userId, MeritPackage.Literals.BADGE, be);
		return badge;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.merit.service.api.MeritService#setBetResult(java.lang.String, java.lang.String, int, org.gecko.notary.merit.model.merit.BetResultType)
	 */
	@Override
	public Badge setBetResult(String userId, String betId, int amount, BetResultType result) {
		if (betId == null) {
			throw new IllegalStateException("Cannot account a bet result without a bet id");
		}
		if (Math.abs(amount) > MeritService.getPurchaseLimit()) {
			throw new IllegalStateException("Setting bet results with merits point out of the limit.");
		}
		if (result == null || BetResultType.INITIAL_BET.equals(result)) {
			throw new IllegalStateException("Invalid bet result type");
		}
		Badge badge = getBadge(userId);
		if (amount == 0) {
			return badge;
		}
		Badge copy = EcoreUtil.copy(badge);
		int merits = copy.getMeritPoints();
		if (amount > merits) {
			throw new IllegalStateException("The user cannot lose more points than he has. Also betting more that you have is also not possible. Something is really wrong here");
		}
		BettingEntry be = MeritFactory.eINSTANCE.createBettingEntry();
		be.setAssetId(userId);
		be.setParticipantId(userId);
		be.setBetIdentifier(betId);
		be.setTransactionId(MeritPackage.Literals.BET.getName());
		be.setResult(result);
		switch (result) {
		case WIN:
			be.setStake(amount);
			merits += amount;
			break;
		case LOSE:
			be.setStake(amount * -1);
			merits -= amount;
			break;
		default:
			be.setStake(0);
			break;
		}
		copy.setMeritPoints(merits);
		transactionEntryService.createTransactionEntry(userId, MeritPackage.Literals.BADGE, be);
		return (Badge) assetService.updateAsset(copy);
	}
	
	/**
	 * Makes the accounting of merit points for various contexts. The stuff that happens should be always the same
	 * @param userId the user that triggers the accounting
	 * @param amount the merit point account
	 * @param supplier the supplier, that creates the context logic
	 * @param error the error handler for custom handling
	 * @return {@link Badge}
	 */
	private Badge account(String userId, int amount, Supplier<TransactionEntry> supplier, Consumer<Throwable> error) {
		if (supplier == null) {
			throw new IllegalStateException("Cannot execute method without supplier");
		}
		if (error == null) {
			throw new IllegalStateException("Cannot handle errors without error consumer");
		}
		Badge badge = getBadge(userId);
		if (amount == 0) {
			return badge;
		}
		Badge copy = EcoreUtil.copy(badge);
		int oldMerits = copy.getMeritPoints();
		copy.setMeritPoints(oldMerits + amount);

		try {
			TransactionEntry te = supplier.get();
			transactionEntryService.createTransactionEntry(userId, MeritPackage.Literals.BADGE, te);
			return (Badge) assetService.updateAsset(copy);
		} catch (Exception e) {
			error.accept(e);
			return badge;
		}
	}

	/**
	 * Here we can do the real purchase. But in the end we need this {@link PurchaseEntry}, that is then later recorded
	 * @param userId
	 * @param amount
	 * @param provider
	 * @return
	 */
	private PurchaseEntry doPurchase(String userId, int amount, PurchaseProvider provider) {
		PurchaseEntry pe = MeritFactory.eINSTANCE.createPurchaseEntry();
		pe.setAmount(amount);
		pe.setAssetId(userId);
		pe.setTransactionId(MeritPackage.Literals.PURCHASE.getName());
		pe.setParticipantId(userId);
		pe.setPurchaseProvider(provider.getLiteral());
		pe.setPurchaseTime(new Date());
		pe.setPurchaseTransactionId(UUID.randomUUID().toString());
		return pe;
	}
	
	/**
	 * Here we can do the real earning of merits. But in the end we need this {@link AchievementEntry}, that is then later recorded
	 * @param userId
	 * @param amount
	 * @param reason
	 * @return
	 */
	private AchievementEntry doEarn(String userId, int amount, String reason) {
		AchievementEntry ae = MeritFactory.eINSTANCE.createAchievementEntry();
		ae.setMeritAmount(amount);
		ae.setDescription(reason);
		ae.setTransactionId(MeritPackage.Literals.ACHIEVEMENT.getName());
		ae.setAssetId(userId);
		ae.setParticipantId(userId);
		return ae;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.notary.merit.service.api.MeritService#validateBadge(java.lang.String, int)
	 */
	@Override
	public boolean validateBadge(String user, int amount) throws IllegalStateException {
		if (amount < 0) {
			throw new IllegalStateException("Negative merits are not supported.");
		}
		// Just checking for a valid badge, otherwise we exit here
		Badge badge = getBadge(user);
		return amount <= badge.getMeritPoints();
	}

}

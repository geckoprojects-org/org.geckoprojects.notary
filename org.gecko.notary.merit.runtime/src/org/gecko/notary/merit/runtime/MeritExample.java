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
package org.gecko.notary.merit.runtime;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.gecko.emf.repository.mongo.annotations.RequireMongoEMFRepository;
import org.gecko.notary.merit.model.merit.Badge;
import org.gecko.notary.merit.model.merit.BetResultType;
import org.gecko.notary.merit.model.merit.MeritFactory;
import org.gecko.notary.merit.model.merit.MeritPackage;
import org.gecko.notary.merit.model.merit.PurchaseProvider;
import org.gecko.notary.merit.service.api.MeritService;
import org.gecko.notary.model.notary.AssetInfo;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.Transaction;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.model.notary.TransactionType;
import org.gecko.notary.service.api.AssetService;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionEntryService;
import org.gecko.notary.service.api.TransactionService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.configurator.annotations.RequireConfigurator;

/**
 * 
 * @author mark
 * @since 09.08.2021
 */
@RequireMongoEMFRepository
@RequireConfigurator
@Component(immediate = true)
public class MeritExample {
	
	@Reference
	private ParticipantService participantService;
	@Reference
	private TransactionService transactionService;
	@Reference
	private TransactionEntryService transactionEntryService;
	@Reference
	private AssetService assetService;
	@Reference
	private MeritService meritService;
	@Reference(target = "(&(object=TransactionEntry)(target=TransactionEntry)(domain=Merit))") 
	private TextProvider textProvider;
	private final Random random = new Random();
	
	@Activate
	public void activate(ComponentContext ctx) {
		createParticipants();
		
		Badge badge = meritService.getBadge("user1");
		if (badge.getMeritPoints() == 0) {
			purchaseMerits("user1", 100);
		}
		badge = meritService.getBadge("user2");
		if (badge.getMeritPoints() == 0) {
			purchaseMerits("user2", 90);
			earnMeritPoints("user2", 5);
		}
		bet("user1", "user2", getRandomNumberUsingNextInt(1, 20));
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("");
		System.out.println("----------");
		showTransactionEntries("user1");
		System.out.println("----------");
		showTransactionEntries("user2");	
		System.out.println("----------");
	}
	
	public int getRandomNumberUsingNextInt(int min, int max) {
	    return random.nextInt(max - min) + min;
	}

	/**
	 * @param string
	 */
	private void showTransactionEntries(String user) {
		System.out.println("Show entries for user '" + user + "'");
		Badge badge = meritService.getBadge(user);
		List<TransactionEntry> entries = transactionEntryService.getTransactionEntry(badge.getId());
		entries.stream().filter(Predicate.not(AssetTransactionEntry.class::isInstance)).forEach(te->{
			System.out.println(textProvider.provideText(te, null));
		});
	}

	/**
	 * @param user1
	 * @param user2
	 * @param amount
	 */
	private void bet(String user1, String user2, int amount) {
		String betId = user1 + "_" + user2;
		try {
			if (meritService.validateBadge(user1, amount) && meritService.validateBadge(user2, amount)) {
				meritService.placeBet(user1, betId, amount);
				meritService.placeBet(user2, betId, amount);
				System.out.println("Places a bet for users: " + user1 + ", " + user2 + " with amount of " + amount + " merits");
			} else {
				System.out.println("Cannot place a bet because at least one user has not enough merit points");
			}
			meritService.setBetResult(user1, betId, amount, BetResultType.LOSE);
			meritService.setBetResult(user2, betId, amount, BetResultType.WIN);
			System.out.println("User " + user2 + " won the match");
		} catch (Exception e) {
			System.out.println("Cannot bet: " + e.getMessage());
		}
	}

	/**
	 * @param string
	 * @param i
	 */
	private void earnMeritPoints(String user, int amount) {
		try {
			Badge badge = meritService.getBadge(user);
			System.out.println("Badge for user '" + user + "' has merits: " + badge.getMeritPoints() + "; earning " + amount);
			badge = meritService.earnMerits(user, amount, "Because you stayed with us for so long");
			System.out.println("Badge for user '" + user + "' has now merits: " + badge.getMeritPoints() + " after the achievement");
		} catch (Exception e) {
			System.out.println("Error purchasing merits: " + e.getMessage());
		}
	}

	/**
	 * Example how to earn some merit points
	 * @param string
	 * @param i
	 */
	private void purchaseMerits(String user, int amount) {
		try {
			Badge badge = meritService.getBadge(user);
			System.out.println("Badge for user '" + user + "' has merits: " + badge.getMeritPoints() + "; purchasing " + amount);
			badge = meritService.purchaseMerits(user, amount, PurchaseProvider.GOOGLE);
			System.out.println("Badge for user '" + user + "' has now merits: " + badge.getMeritPoints() + " after the purchase");
		} catch (Exception e) {
			System.out.println("Error purchasing merits: " + e.getMessage());
		}
	}

	/**
	 * 
	 */
	private void createParticipants() {
		ParticipantDefinition p1 = participantService.getDefinition("user1");
		if (p1 == null) {
			p1 = participantService.createSimpleParticipant("User One", "This is user one", "user1");
		}
		setupTransaction(p1);
		setupBadge(p1);
		ParticipantDefinition p2 = participantService.getDefinition("user2");
		if (p2 == null) {
			p2 = participantService.createSimpleParticipant("User Two", "This is user two", "user2");
		}
		setupTransaction(p2);
		setupBadge(p2);
	}

	/**
	 * @param participant
	 */
	private void setupBadge(ParticipantDefinition participant) {
		Badge badge = (Badge) assetService.getAssetByParticipant(participant.getId(), participant.getId(), MeritPackage.Literals.BADGE);
		if (badge == null) {
			badge = MeritFactory.eINSTANCE.createBadge();
			badge.setId(participant.getId());
			badge.setCreatorId(participant.getId());
			badge.setOwnerId(participant.getId());
			badge.setOwnerName(participant.getParticipant().getName());
			AssetInfo badgeInfo = NotaryFactory.eINSTANCE.createAssetInfo();
			badgeInfo.setLabel("Merit Badge of user " + badge.getOwnerName());
			badge.setInfo(badgeInfo);
			assetService.updateAsset(badge);
		}
	}

	/**
	 * @param participant
	 */
	private void setupTransaction(ParticipantDefinition participant) {
		Transaction step1 = transactionService.getTransactionById(MeritPackage.Literals.PURCHASE.getName());
		if (step1 == null) {
			step1 = transactionService.createSimpleTransaction(participant, TransactionType.GENESIS);
			step1.setId(MeritPackage.Literals.PURCHASE.getName());
			step1.setDescription("Purchase Merit Points");
			transactionService.updateTransaction(participant.getId(), step1);
		}
		
		Transaction step2 = transactionService.getTransactionById(MeritPackage.Literals.ACHIEVEMENT.getName());
		if (step2 == null) {
			step2 = transactionService.createSimpleTransaction(participant, TransactionType.GENESIS);
			step2.setId(MeritPackage.Literals.ACHIEVEMENT.getName());
			step2.setDescription("Earn some merit points through achievements");
			transactionService.updateTransaction(participant.getId(), step2);
		}

		Transaction step3 = transactionService.getTransactionById(MeritPackage.Literals.BET.getName());
		if (step3 == null) {
			step3 = transactionService.createSimpleTransaction(participant, TransactionType.GENESIS);
			step3.setId(MeritPackage.Literals.BET.getName());
			step3.setDescription("Bet against an player");
			transactionService.updateTransaction(participant.getId(), step3);
		}
	}

}

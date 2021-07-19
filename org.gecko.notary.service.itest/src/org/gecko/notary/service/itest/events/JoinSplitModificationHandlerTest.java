/**
 * Copyright (c) 2012 - 2018 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notary.service.itest.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.TransactionEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;


/**
 * <p>
 * This is an integration test for the context service
 * </p>
 * 
 * @since 1.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class JoinSplitModificationHandlerTest {

	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) throws InvalidSyntaxException {
		Dictionary<String, Object> entryProperties = new Hashtable<String, Object>();
		entryProperties.put(Constants.SERVICE_RANKING, 1000);
		entryProperties.put(Constants.SERVICE_SCOPE, Constants.SCOPE_PROTOTYPE);
		TransactionEntryService entryService = mock(TransactionEntryService.class);
		bc.registerService(TransactionEntryService.class, entryService, entryProperties);
		Dictionary<String, Object> repoProperties = new Hashtable<String, Object>();
		repoProperties.put(Constants.SERVICE_RANKING, 1000);
		repoProperties.put("repo_id", "notary.notary");
		repoProperties.put(Constants.SERVICE_SCOPE, Constants.SCOPE_PROTOTYPE);
		EMFRepository repository = mock(EMFRepository.class);
		bc.registerService(EMFRepository.class, repository, repoProperties);
	}

	@Test
	public void testJoinSplit_Empty(@InjectService(filter = "(component.name=JoinSplitTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = EMFRepository.class) EMFRepository repository,
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);

		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", new HashMap<String, Object>()));
		Mockito.verify(entryService, Mockito.never()).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.never()).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.never()).detach(Mockito.any(EObject.class));

		Map<String, Object> eventProps = new HashMap<String, Object>();
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		eventProps.put("joinData", new ArrayList<String>());
		eventProps.put("splitData", new ArrayList<String>());
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.never()).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.never()).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.never()).detach(Mockito.any(EObject.class));
	}

	@Test
	public void testJoinSplit_Invalid(@InjectService(filter = "(component.name=JoinSplitTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = EMFRepository.class) EMFRepository repository,
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);

		Map<String, Object> eventProps = new HashMap<String, Object>();
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		eventProps.put("joinData", new ArrayList<String>());
		eventProps.put("splitData", new ArrayList<String>());

		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.never()).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.never()).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.never()).detach(Mockito.any(EObject.class));

		eventProps = new HashMap<String, Object>();
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		eventProps.put("joinData", new ArrayList<String>());
		eventProps.put("splitData", new ArrayList<String>());
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.never()).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.never()).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.never()).detach(Mockito.any(EObject.class));

		eventProps = new HashMap<String, Object>();
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("joinData", new ArrayList<String>());
		eventProps.put("splitData", new ArrayList<String>());
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.never()).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.never()).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.never()).detach(Mockito.any(EObject.class));

		eventProps = new HashMap<String, Object>();
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		eventProps.put("splitData", new ArrayList<String>());
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.never()).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.never()).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.never()).detach(Mockito.any(EObject.class));

		eventProps = new HashMap<String, Object>();
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		eventProps.put("joinData", new ArrayList<String>());
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.never()).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.never()).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.never()).detach(Mockito.any(EObject.class));
	}

	@Test
	public void testJoinSplit_NewOnly(@InjectService(filter = "(component.name=JoinSplitTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = EMFRepository.class) EMFRepository repository,
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		Map<String, Object> eventProps = new HashMap<String, Object>();
		Asset asset1 = NotaryFactory.eINSTANCE.createAsset();
		asset1.setId("test1");
		Asset asset2 = NotaryFactory.eINSTANCE.createAsset();
		asset2.setId("test2");
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		List<String> removal = new ArrayList<String>();
		List<String> adds = new ArrayList<String>();
		adds.add("test1");
		eventProps.put("joinData", adds);
		eventProps.put("splitData", removal);

		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(asset1);

		EventHandler notificationHandler = notificationHandlerAware.getService();
		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.times(1)).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), Mockito.any(TransactionEntry.class));
		Mockito.verify(repository, Mockito.times(1)).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.times(1)).detach(Mockito.any(EObject.class));
	}

	@Test
	public void testJoinSplit_NewMany(@InjectService(filter = "(component.name=JoinSplitTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = EMFRepository.class) EMFRepository repository,
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		Map<String, Object> eventProps = new HashMap<String, Object>();
		Asset asset1 = NotaryFactory.eINSTANCE.createAsset();
		asset1.setId("test1");
		Asset asset2 = NotaryFactory.eINSTANCE.createAsset();
		asset2.setId("test2");
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		List<String> removal = new ArrayList<String>();
		List<String> adds = new ArrayList<String>();
		adds.add("test1");
		adds.add("test2");
		eventProps.put("joinData", adds);
		eventProps.put("splitData", removal);

		EventHandler notificationHandler = notificationHandlerAware.getService();
		ArgumentCaptor<TransactionEntry> entry = ArgumentCaptor.forClass(TransactionEntry.class);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(asset1);

		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.times(2)).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), entry.capture());
		assertEquals(2, entry.getAllValues().stream().filter(e->((AssetTransactionEntry)e).getChangeType().equals(AssetChangeType.JOIN)).count());
		assertEquals(0, entry.getAllValues().stream().filter(e->((AssetTransactionEntry)e).getChangeType().equals(AssetChangeType.SPLIT)).count());
		Mockito.verify(repository, Mockito.times(2)).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.times(2)).detach(Mockito.any(EObject.class));
	}

	@Test
	public void testJoinSplit_Many(@InjectService(filter = "(component.name=JoinSplitTransactionEntryWorker)")ServiceAware<EventHandler> notificationHandlerAware, 
			@InjectService(service = EMFRepository.class) EMFRepository repository,
			@InjectService(service = TransactionEntryService.class) TransactionEntryService entryService) {
		assertThat(notificationHandlerAware.getServices()).hasSize(1);
		Map<String, Object> eventProps = new HashMap<String, Object>();
		Asset asset1 = NotaryFactory.eINSTANCE.createAsset();
		asset1.setId("test1");
		Asset asset2 = NotaryFactory.eINSTANCE.createAsset();
		asset2.setId("test2");
		eventProps.put("joinSplitType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetType", NotaryPackage.Literals.ASSET);
		eventProps.put("parentAssetId", "treatmelikeagood");
		List<String> removal = new ArrayList<String>();
		removal.add("test3");
		List<String> adds = new ArrayList<String>();
		adds.add("test1");
		adds.add("test2");
		eventProps.put("joinData", adds);
		eventProps.put("splitData", removal);

		EventHandler notificationHandler = notificationHandlerAware.getService();
		ArgumentCaptor<TransactionEntry> entry = ArgumentCaptor.forClass(TransactionEntry.class);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(asset1);

		notificationHandler.handleEvent(new Event("test", eventProps));
		Mockito.verify(entryService, Mockito.times(3)).createTransactionEntry(Mockito.anyString(), Mockito.any(EClass.class), entry.capture());

		assertEquals(2, entry.getAllValues().stream().filter(e->((AssetTransactionEntry)e).getChangeType().equals(AssetChangeType.JOIN)).count());
		assertEquals(1, entry.getAllValues().stream().filter(e->((AssetTransactionEntry)e).getChangeType().equals(AssetChangeType.SPLIT)).count());
		Mockito.verify(repository, Mockito.times(3)).getEObject(Mockito.any(EClass.class), Mockito.anyString());
		Mockito.verify(repository, Mockito.times(3)).detach(Mockito.any(EObject.class));
	}
	
}

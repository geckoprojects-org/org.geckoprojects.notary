/**
 * Copyright (c) 2012 - 2018 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.notaryservice.itest;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetChangeType;
import org.gecko.notary.model.notary.AssetLog;
import org.gecko.notary.model.notary.AssetTransactionEntry;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.TransactionEntryService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
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
public class TransactionEntryServiceIntegrationTest {
	
	public interface QueryRepositoryMock extends EMFRepository, QueryRepository {

	}
	
	private IQueryBuilder builder;
	private IQuery query;
	
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		Dictionary<String, Object> eaProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		EventAdmin eventAdmin = mock(EventAdmin.class);
		bc.registerService(EventAdmin.class, eventAdmin, eaProperties);
		
		Dictionary<String, Object> tpProperties = new Hashtable<String, Object>();
		tpProperties.put(Constants.SERVICE_RANKING, 1000);
		tpProperties.put("object", "TransactionEntry");
		tpProperties.put("target", "Asset");
		TextProvider textProvider = mock(TextProvider.class);
		bc.registerService(TextProvider.class, textProvider, tpProperties);
		Dictionary<String, Object> teProperties = new Hashtable<String, Object>();
		teProperties.put(Constants.SERVICE_RANKING, 1000);
		teProperties.put("object", "TransactionEntry");
		teProperties.put("target", "TransactionEntry");
		TextProvider entryTextProvider = mock(TextProvider.class);
		bc.registerService(TextProvider.class, entryTextProvider, teProperties);
		
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("repo_id", "notary.notary");
		QueryRepositoryMock repository = mock(QueryRepositoryMock.class);
		bc.registerService(EMFRepository.class, new PrototypeServiceFactory<EMFRepository>() {
	
			@Override
			public EMFRepository getService(Bundle bundle, ServiceRegistration<EMFRepository> registration) {
				return repository;
			}
	
			@Override
			public void ungetService(Bundle bundle, ServiceRegistration<EMFRepository> registration, EMFRepository service) {
				repository.dispose();
			}
		}, properties);
		
		builder = mock(IQueryBuilder.class);
		query = mock(IQuery.class);
	}

	@Test
	public void testGetLastEntry_NoAsset(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.getLastTransactionEntry(null);
		});
	}
	
	@Test
	public void testGetLastEntry_NoEntry(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId("test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		assertNull(transactionEntryService.getLastTransactionEntry("test"));
	}
	
	@Test
	public void testGetLastEntry_Entry(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId("test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		assertEquals(ate1, transactionEntryService.getLastTransactionEntry("test"));
	}
	
	@Test
	public void testGetEntries_NoAsset(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.getTransactionEntry(null);
		});
	}
	
	@Test
	public void testGetEntries_NoEntry(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId("test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		assertTrue(transactionEntryService.getTransactionEntry("test").isEmpty());
	}
	
	@Test
	public void testGetEntries_Entry(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId("test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.getEntry().add(ate1);
		AssetTransactionEntry ate2 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate2.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate2);
		assetLog.getEntry().add(ate2);
		assertEquals(2, transactionEntryService.getTransactionEntry("test").size());
	}
	
	@Test
	public void testGetEntriesByPartAndType_NoParticipant(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.getTransactionEntryByParticipantAndType(null, null);
		});
	}

	@Test
	public void testGetEntriesByPartAndType_NoEntryNoType(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.sort(Mockito.any(), Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(Collections.emptyList());
		assertTrue(transactionEntryService.getTransactionEntryByParticipantAndType("test", null).isEmpty());
		Mockito.verify(builder, Mockito.never()).column(Mockito.anyString());
		Mockito.verify(builder, Mockito.never()).and(Mockito.any(IQuery.class));
		Mockito.verify(builder, Mockito.times(1)).column(Mockito.any(EAttribute.class));
		Mockito.verify(builder, Mockito.times(1)).simpleValue(Mockito.any());
	}
	
	@Test
	public void testGetEntriesByPartAndType_NoEntry(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.column(Mockito.anyString())).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.sort(Mockito.any(), Mockito.any())).thenReturn(builder);
		Mockito.when(builder.and(Mockito.any(), Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(Collections.emptyList());
		assertTrue(transactionEntryService.getTransactionEntryByParticipantAndType("test", "bla").isEmpty());
		Mockito.verify(builder, Mockito.times(1)).and(Mockito.any(IQuery.class), Mockito.any(IQuery.class));
		Mockito.verify(builder, Mockito.times(1)).column(Mockito.anyString());
		Mockito.verify(builder, Mockito.times(1)).column(Mockito.any(EAttribute.class));
		Mockito.verify(builder, Mockito.times(2)).simpleValue(Mockito.any());
	}
	
	@Test
	public void testGetEntriesByPartAndType_Entry(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
		TransactionEntry e1 = NotaryFactory.eINSTANCE.createTransactionEntry();
		TransactionEntry e2 = NotaryFactory.eINSTANCE.createTransactionEntry();
		List<EObject> tel = new ArrayList<>(2);
		tel.add(e1);
		tel.add(e2);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.column(Mockito.anyString())).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.sort(Mockito.any(), Mockito.any())).thenReturn(builder);
		Mockito.when(builder.and(Mockito.any(), Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(tel);
		
		assertEquals(2, transactionEntryService.getTransactionEntryByParticipantAndType("test", "bla").size());
		
		Mockito.verify(builder, Mockito.times(1)).and(Mockito.any(IQuery.class), Mockito.any(IQuery.class));
		Mockito.verify(builder, Mockito.times(1)).column(Mockito.anyString());
		Mockito.verify(builder, Mockito.times(1)).column(Mockito.any(EAttribute.class));
		Mockito.verify(builder, Mockito.times(2)).simpleValue(Mockito.any());
	}
	
	@Test
	public void testUpdateAsset_NotSameId(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("test");
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("test2");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createAssetModificationTransaction(a1, a2);
		});
	}

	@Test
	public void testUpdateAsset_NoAssetFound(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET_LOG)) {
				return assetLog;
			} else {
				return null;
			}
		});
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createAssetModificationTransaction(null, a1);
		});
	}
	
	@Test
	public void testUpdateAsset_NoDifferentAsset(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId(ASSET_ID + "2");
		assetLog.setAsset(a2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET_LOG)) {
				return assetLog;
			} else {
				return null;
			}
		});
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createAssetModificationTransaction(null, a1);
		});
	}
	
	@Test
	public void testUpdateAsset_NewLog(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(null);
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(null, p1);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.CREATION, entry.getChangeType());
		assertTrue(EcoreUtil.equals(p1, entry.getAsset()));
		
		AssetLog assetLog = (AssetLog) eoCL.getValue();
		assertEquals(1, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
		assertEquals(ASSET_ID, entry.getAssetId());
	}
	
	@Test
	public void testUpdateAsset_NewLogModify(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		a1.setOwnerId(ASSET_ID);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId(ASSET_ID);
		a2.setOwnerId(ASSET_ID);
		a2.setCreatorBehalf("123");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET)) {
				return a1;
			} else {
				return null;
			}
		});
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(a1, a2);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.MODIFICATION, entry.getChangeType());
		assertTrue(EcoreUtil.equals(a2, entry.getAsset()));
		
		AssetLog assetLog = (AssetLog) eoCL.getValue();
		assertEquals(1, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
	}
	
	@Test
	public void testUpdateAsset_NewLogOwnerChange(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		a1.setOwnerId(ASSET_ID);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId(ASSET_ID);
		a2.setOwnerId(ASSET_ID + "2");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET)) {
				return a1;
			} else {
				return null;
			}
		});
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(a1, a2);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.OWNERSHIP, entry.getChangeType());
		assertTrue(EcoreUtil.equals(a2, entry.getAsset()));
		
		AssetLog assetLog = (AssetLog) eoCL.getValue();
		assertEquals(1, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
	}
	
	@Test
	public void testUpdateAsset_NewLogModifyInactive(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		a1.setOwnerId(ASSET_ID);
		a1.setInactive(true);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId(ASSET_ID);
		a2.setOwnerId(ASSET_ID);
		a2.setCreatorBehalf("123");
		a2.setInactive(true);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET)) {
				return a1;
			} else {
				return null;
			}
		});
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(a1, a2);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.MODIFICATION, entry.getChangeType());
		assertTrue(EcoreUtil.equals(a2, entry.getAsset()));
		
		AssetLog assetLog = (AssetLog) eoCL.getValue();
		assertEquals(1, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
	}
	
	@Test
	public void testUpdateAsset_NewLogSetInactive(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId(ASSET_ID);
		a2.setCreatorBehalf("123");
		a2.setInactive(true);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(null);
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(a1, a2);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.DESTRUCTION, entry.getChangeType());
		assertTrue(EcoreUtil.equals(a2, entry.getAsset()));
		
		AssetLog assetLog = (AssetLog) eoCL.getValue();
		assertEquals(1, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
	}
	
	@Test
	public void testUpdateAsset_ExistingLog(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(null, a1);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.CREATION, entry.getChangeType());
		assertTrue(EcoreUtil.equals(a1, entry.getAsset()));
		
		AssetLog assetLogResult = (AssetLog) eoCL.getValue();
		assertEquals(assetLog, assetLogResult);
		assertEquals(2, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
	}
	
	@Test
	public void testUpdateAsset_ExistingLogModify(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId(ASSET_ID);
		a1.setOwnerId(ASSET_ID);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId(ASSET_ID);
		a2.setOwnerId(ASSET_ID);
		a2.setCreatorBehalf("123");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(a1, a2);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.MODIFICATION, entry.getChangeType());
		assertTrue(EcoreUtil.equals(a2, entry.getAsset()));
		
		AssetLog assetLogResult = (AssetLog) eoCL.getValue();
		assertEquals(assetLog, assetLogResult);
		assertEquals(2, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
		
	}
	
	@Test
	public void testUpdateAsset_ExistingLogOwner(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		p1.setOwnerId(ASSET_ID);
		Asset p2 = NotaryFactory.eINSTANCE.createAsset();
		p2.setId(ASSET_ID);
		p2.setOwnerId(ASSET_ID + "2");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(p1, p2);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entry = (AssetTransactionEntry) eoCE.getValue();
		assertEquals(AssetChangeType.OWNERSHIP, entry.getChangeType());
		assertTrue(EcoreUtil.equals(p2, entry.getAsset()));
		
		AssetLog assetLogResult = (AssetLog) eoCL.getValue();
		assertEquals(assetLog, assetLogResult);
		assertEquals(2, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
		
	}
	
	
	@Test
	public void testUpdateAsset_ExistingLogAndOwner(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		p1.setOwnerId(ASSET_ID);
		Asset p2 = NotaryFactory.eINSTANCE.createAsset();
		p2.setId(ASSET_ID);
		p2.setOwnerId(ASSET_ID + "2");
		p2.setCreatorBehalf("1234");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createAssetModificationTransaction(p1, p2);
		
		Mockito.verify(repository, Mockito.times(2)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(2)).save(eoCE.capture(), Mockito.anyMap());
		assertEquals(2, eoCE.getAllValues().size());
		assertTrue(eoCE.getAllValues().get(0) instanceof AssetTransactionEntry);
		assertTrue(eoCE.getAllValues().get(1) instanceof AssetTransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		AssetTransactionEntry entryOwn = (AssetTransactionEntry) eoCE.getAllValues().get(0);
		AssetTransactionEntry entryMod = (AssetTransactionEntry) eoCE.getAllValues().get(1);
		assertEquals(AssetChangeType.OWNERSHIP, entryOwn.getChangeType());
		assertEquals(AssetChangeType.MODIFICATION, entryMod.getChangeType());
		assertTrue(EcoreUtil.equals(p2, entryOwn.getAsset()));
		assertTrue(EcoreUtil.equals(p2, entryMod.getAsset()));
		
		AssetLog assetLogResult = (AssetLog) eoCL.getValue();
		assertEquals(assetLog, assetLogResult);
		assertEquals(3, assetLog.getEntry().size());
		assertEquals(entryMod, assetLog.getLastEntry());
		
	}
	
	@Test
	public void testCreateEntry_Nothing(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createTransactionEntry(null, null, null);
		});
	}
	
	@Test
	public void testCreateEntry_NoAssetId(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createTransactionEntry(null, NotaryPackage.Literals.ASSET, te);
		});
	}
	
	@Test
	public void testCreateEntry_NoType(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createTransactionEntry("test", null, te);
		});
	}
	
	@Test
	public void testCreateEntry_NoEntry(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createTransactionEntry("test", NotaryPackage.Literals.ASSET, null);
		});
	}
	
	@Test
	public void testCreateEntry_NoAssetLogAndAsset(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(null);
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createTransactionEntry("test", NotaryPackage.Literals.ASSET, te);
		});
	}
	
	@Test
	public void testCreateEntry_NoDifferentAsset(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);

		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Asset p2 = NotaryFactory.eINSTANCE.createAsset();
		p2.setId(ASSET_ID + "2");
		assetLog.setAsset(p2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET_LOG)) {
				return assetLog;
			} else {
				return null;
			}
		});
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te);
		});
	}
	
	@Test
	public void testCreateEntry_NoAssetFound(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET_LOG)) {
				return assetLog;
			} else {
				return null;
			}
		});
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te);
		});
	}
	
	@Test
	public void testCreateEntry_NewAssetLog(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EventAdmin eventAdmin,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(eventAdmin);
		assertNotNull(transactionEntryService);
		String ASSET_ID = "test";
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenAnswer((a)-> {
			if (a.getArguments()[0].equals(NotaryPackage.Literals.ASSET)) {
				return p1;
			} else {
				return null;
			}
		});
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		Mockito.verify(eventAdmin, Mockito.times(1)).postEvent(Mockito.any(Event.class));
		assertTrue(eoCE.getValue() instanceof TransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		TransactionEntry entry = (TransactionEntry) eoCE.getValue();
		
		AssetLog assetLog = (AssetLog) eoCL.getValue();
		assertEquals(1, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
		assertNull(entry.getPrecedingEntry());
		assertNull(entry.getPrecedingEntryId());
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("type"));
		assertTrue(event.containsProperty("assetLog"));
		assertTrue(event.getProperty("assetLog") instanceof AssetLog);
		assertTrue(event.containsProperty("entry"));
		assertTrue(event.getProperty("entry") instanceof TransactionEntry);
		assertEquals("transactionEntry/notification", event.getTopic());
	}
	
	@Test
	public void testCreateEntry_ExistingAssetLog(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EventAdmin eventAdmin,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertNotNull(eventAdmin);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof TransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		TransactionEntry entry = (TransactionEntry) eoCE.getValue();
		
		AssetLog assetLogResult = (AssetLog) eoCL.getValue();
		assertEquals(assetLog, assetLogResult);
		assertEquals(2, assetLog.getEntry().size());
		assertEquals(entry, assetLog.getLastEntry());
		assertEquals(ate1, entry.getPrecedingEntry());
		assertEquals(ate1.getId(), entry.getPrecedingEntryId());
		assertEquals(ASSET_ID, entry.getAssetId());
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("type"));
		assertTrue(event.containsProperty("assetLog"));
		assertTrue(event.getProperty("assetLog") instanceof AssetLog);
		assertTrue(event.containsProperty("entry"));
		assertTrue(event.getProperty("entry") instanceof TransactionEntry);
		assertEquals("transactionEntry/notification", event.getTopic());
	}
	
	@Test
	public void testCreateEntry_ExistingAssetLogAppend(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EventAdmin eventAdmin,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertNotNull(eventAdmin);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setId("ate1");
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		TransactionEntry te1 = NotaryFactory.eINSTANCE.createTransactionEntry();
		te1.setId("te1");
		
		ArgumentCaptor<EObject> eoCL = ArgumentCaptor.forClass(EObject.class);
		ArgumentCaptor<EObject> eoCE = ArgumentCaptor.forClass(EObject.class);
		
		transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te1);
		
		Mockito.verify(repository, Mockito.times(1)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(1)).save(eoCE.capture(), Mockito.anyMap());
		assertTrue(eoCE.getValue() instanceof TransactionEntry);
		assertTrue(eoCL.getValue() instanceof AssetLog);
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("type"));
		assertTrue(event.containsProperty("assetLog"));
		assertTrue(event.getProperty("assetLog") instanceof AssetLog);
		assertTrue(event.containsProperty("entry"));
		assertTrue(event.getProperty("entry") instanceof TransactionEntry);
		assertEquals("transactionEntry/notification", event.getTopic());
		
		TransactionEntry te1Entry = (TransactionEntry) eoCE.getValue();
		
		AssetLog assetLogResult = (AssetLog) eoCL.getValue();
		assertEquals(assetLog, assetLogResult);
		assertEquals(2, assetLog.getEntry().size());
		assertEquals(te1Entry, assetLog.getLastEntry());
		assertEquals(ate1, te1Entry.getPrecedingEntry());
		assertEquals(ate1.getId(), te1Entry.getPrecedingEntryId());
		
		eoCL = ArgumentCaptor.forClass(EObject.class);
		eoCE = ArgumentCaptor.forClass(EObject.class);
		TransactionEntry te2 = NotaryFactory.eINSTANCE.createTransactionEntry();
		te2.setId("te2");
		transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te2);
		Mockito.verify(repository, Mockito.times(2)).save(eoCL.capture());
		Mockito.verify(repository, Mockito.times(2)).save(eoCE.capture(), Mockito.anyMap());
		
		assertTrue(eoCE.getAllValues().get(1) instanceof TransactionEntry);
		assertTrue(eoCL.getAllValues().get(1) instanceof AssetLog);
		TransactionEntry te2Entry = (TransactionEntry) eoCE.getAllValues().get(1);
		assertEquals("te2", te2Entry.getId());
		
		assertEquals(assetLog, assetLogResult);
		assertEquals(3, assetLog.getEntry().size());
		assertEquals(te2Entry, assetLog.getLastEntry());
		assertEquals(te1Entry, te2Entry.getPrecedingEntry());
		assertEquals(te1Entry.getId(), te2Entry.getPrecedingEntryId());
	}
	
	@Test
	public void testCreateEntry_TextProviderNull(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EventAdmin eventAdmin,
			@InjectService(filter = "(object=TransactionEntry)") TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertNotNull(textProvider);
		assertNotNull(eventAdmin);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId(ASSET_ID);
		assetLog.setAsset(asset);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setId("ate1");
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Mockito.when(textProvider.provideText(Mockito.any(EObject.class), Mockito.anyMap())).thenReturn(null);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		
		TransactionEntry te1 = NotaryFactory.eINSTANCE.createTransactionEntry();
		te1.setId("te1");
		
		assertTrue(asset.getTransactionDesc().isEmpty());
		transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te1);
		assertTrue(asset.getTransactionDesc().isEmpty());
		// ordinary save, but not the one from asset
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
	}
	
	@Test
	public void testCreateEntry_TextProviderResult(@InjectService TransactionEntryService transactionEntryService,
			@InjectService EventAdmin eventAdmin,
			@InjectService(filter = "(object=TransactionEntry)") TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(transactionEntryService);
		assertNotNull(textProvider);
		assertNotNull(eventAdmin);
		String ASSET_ID = "test";
		AssetLog assetLog = NotaryFactory.eINSTANCE.createAssetLog();
		assetLog.setId(ASSET_ID);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId(ASSET_ID);
		assetLog.setAsset(asset);
		AssetTransactionEntry ate1 = NotaryFactory.eINSTANCE.createAssetTransactionEntry();
		ate1.setId("ate1");
		ate1.setChangeType(AssetChangeType.MODIFICATION);
		assetLog.setLastEntry(ate1);
		assetLog.getEntry().add(ate1);
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId(ASSET_ID);
		Mockito.when(textProvider.provideText(Mockito.any(EObject.class), Mockito.any())).thenReturn("this.test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(assetLog);
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).postEvent(eventC.capture());
		
		TransactionEntry te = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.setId("te");
		
		assertTrue(asset.getTransactionDesc().isEmpty());
		transactionEntryService.createTransactionEntry(ASSET_ID, NotaryPackage.Literals.ASSET, te);
		assertEquals(1, asset.getTransactionDesc().size());
		assertEquals("this.test", asset.getTransactionDesc().get(0));
		// two save, one from the ordinary process and second for asset
		Mockito.verify(repository, Mockito.times(2)).save(Mockito.any(EObject.class), Mockito.anyMap());
		
	}
	
}

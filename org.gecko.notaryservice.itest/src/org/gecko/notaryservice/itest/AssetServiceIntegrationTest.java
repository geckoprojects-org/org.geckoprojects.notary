package org.gecko.notaryservice.itest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.AssetInfo;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.NotaryPackage;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.model.notary.TransactionEntry;
import org.gecko.notary.service.api.AssetService;
import org.gecko.notary.service.api.ParticipantService;
import org.gecko.notary.service.api.TransactionEntryService;
import org.gecko.notary.service.api.textprovider.TextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * <p>
 * This is an integration test for the context service
 * </p>
 * 
 * @since 1.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class AssetServiceIntegrationTest {
	
	public interface QueryRepositoryMock extends EMFRepository, QueryRepository {

	}
	
	private IQueryBuilder builder;
	private IQuery query;
	
	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
		Dictionary<String, Object> textProviderProperties = new Hashtable<String, Object>();
		textProviderProperties.put(Constants.SERVICE_RANKING, 1000);
		textProviderProperties.put("object", "Asset");
		textProviderProperties.put("target", "Asset");
		TextProvider textProvider = mock(TextProvider.class);
		bc.registerService(TextProvider.class, textProvider, textProviderProperties);
		
		Dictionary<String, Object> entryProperties = new Hashtable<String, Object>();
		entryProperties.put(Constants.SERVICE_RANKING, 1000);
		entryProperties.put(Constants.SERVICE_SCOPE, Constants.SCOPE_PROTOTYPE);
		TransactionEntryService transactionEntryService = mock(TransactionEntryService.class);
		bc.registerService(TransactionEntryService.class, new PrototypeServiceFactory<TransactionEntryService>() {
	
			@Override
			public TransactionEntryService getService(Bundle bundle, ServiceRegistration<TransactionEntryService> registration) {
				return transactionEntryService;
			}
	
			@Override
			public void ungetService(Bundle bundle, ServiceRegistration<TransactionEntryService> registration, TransactionEntryService service) {
			}
		}, entryProperties);
		
		Dictionary<String, Object> eaProperties = Dictionaries.dictionaryOf(Constants.SERVICE_RANKING, 1000);
		EventAdmin eventAdmin = mock(EventAdmin.class);
		bc.registerService(EventAdmin.class, eventAdmin, eaProperties);
		
		Dictionary<String, Object> partProperties = new Hashtable<String, Object>();
		partProperties.put(Constants.SERVICE_RANKING, 1000);
		partProperties.put(Constants.SERVICE_SCOPE, Constants.SCOPE_PROTOTYPE);
		ParticipantService participantService = mock(ParticipantService.class);
		bc.registerService(ParticipantService.class, new PrototypeServiceFactory<ParticipantService>() {
	
			@Override
			public ParticipantService getService(Bundle bundle, ServiceRegistration<ParticipantService> registration) {
				return participantService;
			}
	
			@Override
			public void ungetService(Bundle bundle, ServiceRegistration<ParticipantService> registration, ParticipantService service) {
			}
		}, partProperties);
		
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
	public void testUpdateCompareAsset_Null(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateCompareAsset(null, null);
		});
	}
	
	@Test
	public void testUpdateCompareAsset_NoCreatorId(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateCompareAsset(a, null);
		});
	}
	
	@Test
	public void testUpdateCompareAsset_NoDef(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertNotNull(assetService);
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		a.setCreatorId("test");
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(null);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateCompareAsset(null, a);
		});
	}
	
	@Test
	public void testUpdateCompareAsset_OtherExistingAsset(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(eventAdmin);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(assetService);
		
		String participantId = "myParticipant";
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(participantId);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId("test");
		
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("asset1");
		a2.setCreatorId("test");
		AssetInfo ai = NotaryFactory.eINSTANCE.createAssetInfo();
		ai.setDescription("bla");
		a2.setInfo(ai);
		
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).sendEvent(eventC.capture());
		
		Asset resultAsset = assetService.updateCompareAsset(a1, a2);
		
		assertEquals(a2, resultAsset);
		assertTrue(def.getAsset().isEmpty());
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("type"));
		assertTrue(event.containsProperty("current"));
		assertTrue(event.getProperty("current") instanceof Asset);
		assertTrue(event.containsProperty("new"));
		assertTrue(event.getProperty("new") instanceof Asset);
		assertEquals("asset/modification", event.getTopic());
		
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.never()).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testUpdateCompareAsset_SameAsset(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(eventAdmin);
		assertNotNull(textProvider);
		assertNotNull(transactionEntryService);
		assertNotNull(participantService);
		assertNotNull(assetService);
		
		String participantId = "myParticipant";
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(participantId);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId("test");
		
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setCreatorId("test");
		a2.setId("asset1");
		
		Asset resultAsset = assetService.updateCompareAsset(a1, a2);
		assertEquals(a2, resultAsset);
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
		Mockito.verify(participantService, Mockito.never()).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
		Mockito.verify(participantService, Mockito.never()).appendAsset(Mockito.anyString(), Mockito.any(Asset.class));
		Mockito.verify(transactionEntryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testUpdateCompareAsset_NewAsset(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(eventAdmin);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(assetService);
		String participantId = "myParticipant";
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(participantId);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		a.setCreatorId("test");
		AssetInfo ai = NotaryFactory.eINSTANCE.createAssetInfo();
		ai.setDescription("bla");
		a.setInfo(ai);
		assertTrue(def.getAsset().isEmpty());
		
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).sendEvent(eventC.capture());
		
		Asset resultAsset = assetService.updateCompareAsset(null, a);
		
		assertEquals(a, resultAsset);
		assertEquals(1, def.getAsset().size());
		assertEquals(a, def.getAsset().get(0));
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("type"));
		assertFalse(event.containsProperty("current"));
		assertTrue(event.containsProperty("new"));
		assertTrue(event.getProperty("new") instanceof Asset);
		assertEquals("asset/modification", event.getTopic());
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.times(1)).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testUpdateAsset_Null(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateAsset(null);
		});
	}
	
	@Test
	public void testUpdateAsset_NoCreatorId(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateAsset(a);
		});
	}
	
	@Test
	public void testUpdateAsset_NoDef(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		a.setCreatorId("test");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateAsset(a);
		});
	}
	
	@Test
	public void testUpdateAsset_OtherExistingAsset(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(eventAdmin);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(assetService);
		
		String participantId = "myParticipant";
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(participantId);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId("test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("asset1");
		a2.setCreatorId("test");
		AssetInfo ai = NotaryFactory.eINSTANCE.createAssetInfo();
		ai.setDescription("bla");
		a2.setInfo(ai);
		
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).sendEvent(eventC.capture());
		
		Asset resultAsset = assetService.updateAsset(a2);
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("type"));
		assertTrue(event.containsProperty("current"));
		assertTrue(event.getProperty("current") instanceof Asset);
		assertTrue(event.containsProperty("new"));
		assertTrue(event.getProperty("new") instanceof Asset);
		assertEquals("asset/modification", event.getTopic());
		
		assertEquals(a2, resultAsset);
		assertTrue(def.getAsset().isEmpty());
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.never()).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.any());
	}

	@Test
	public void testUpdateAsset_SameAsset(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(eventAdmin);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(transactionEntryService);
		assertNotNull(assetService);
		String participantId = "myParticipant";
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(participantId);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId("test");
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setCreatorId("test");
		a2.setId("asset1");
		
		Asset resultAsset = assetService.updateAsset(a2);
		assertEquals(a2, resultAsset);
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
		Mockito.verify(participantService, Mockito.never()).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
		Mockito.verify(participantService, Mockito.never()).appendAsset(Mockito.anyString(), Mockito.any(Asset.class));
		Mockito.verify(transactionEntryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
		Mockito.verify(eventAdmin, Mockito.never()).postEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.any());
	}

	@Test
	public void testUpdateAsset_NewAsset(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(eventAdmin);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(assetService);
		String participantId = "myParticipant";
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(participantId);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(def);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(null);
		
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		a.setCreatorId("test");
		AssetInfo ai = NotaryFactory.eINSTANCE.createAssetInfo();
		ai.setDescription("bla");
		a.setInfo(ai);
		assertTrue(def.getAsset().isEmpty());
		
		ArgumentCaptor<Event> eventC = ArgumentCaptor.forClass(Event.class);
		Mockito.doNothing().when(eventAdmin).sendEvent(eventC.capture());
		
		Asset resultAsset = assetService.updateAsset(a);
		
		assertNotNull(eventC.getValue());
		Event event = eventC.getValue();
		assertTrue(event.containsProperty("type"));
		assertFalse(event.containsProperty("current"));
		assertTrue(event.containsProperty("new"));
		assertTrue(event.getProperty("new") instanceof Asset);
		assertEquals("asset/modification", event.getTopic());
		
		assertEquals(a, resultAsset);
		assertEquals(1, def.getAsset().size());
		assertEquals(a, def.getAsset().get(0));
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(participantService, Mockito.times(1)).updateParticipantDefinition(Mockito.any(ParticipantDefinition.class));
		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testSearchAsset_NullParameters(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAsset(null, null, null);
		});
	}
	
	@Test
	public void testSearchAsset_OnlyType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAsset(null, null, NotaryPackage.Literals.ASSET);
		});
	}
	
	@Test
	public void testSearchAsset_ResultTextProvider(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
		assertNotNull(assetService);
		String ownerId = "myOwner";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("a1");
		a1.setCreatorId(ownerId);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("a2");
		a2.setCreatorId(ownerId);
		List<EObject> pl = new ArrayList<>();
		pl.add(a1);
		pl.add(a2);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(pl);
		
		// always return two results, we just want to check the text provider
		List<Asset> product = assetService.searchAsset(NotaryPackage.Literals.ASSET__OWNER_NAME, "2", NotaryPackage.Literals.ASSET);
		assertNotNull(product);
		assertEquals(2, product.size());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a1")).count());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a2")).count());
		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testSearchAsset_ResultDefaultType(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
		assertNotNull(assetService);
		String ownerId = "myOwner";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("a1");
		a1.setCreatorId(ownerId);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("a2");
		a2.setCreatorId(ownerId);
		List<EObject> pl = new ArrayList<>();
		pl.add(a1);
		pl.add(a2);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(queryRepository.getEObjectsByQuery(ecC.capture(), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(pl);
		
		// always return two results, we just want to check the text provider
		List<Asset> product = assetService.searchAsset(NotaryPackage.Literals.ASSET__OWNER_NAME, "2", null);
		assertNotNull(product);
		assertEquals(2, product.size());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a1")).count());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a2")).count());
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
//	@Test(expected = IllegalStateException.class)
//	public void testSearchAssetInEntry_NullParameters() {
//		setupServices();
//		assetService.searchAssetInEntry(null, null);
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testSearchAssetInEntry_OnlyValue() {
//		setupServices();
//		assetService.searchAssetInEntry(null, "test");
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testSearchAssetInEntry_NoEntries() {
//		List<TransactionEntry> te = new ArrayList<TransactionEntry>();
//		Mockito.when(repository.createQueryBuilder()).thenReturn(builder);
//		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
//		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
//		Mockito.when(builder.build()).thenReturn(query);
//		Mockito.when(repository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(te);
//		
//		setupServices();
//		List<Asset> assets = assetService.searchAssetInEntry(DiamantPackage.Literals.OUTBOUND_LOGISTIC__TRANSPORTATION_TRACKING_ID, "2");
//		assertNotNull(assets);
//		assertTrue(assets.isEmpty());
//		
//		Mockito.verify(repository, Mockito.times(1)).getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap());
//		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testSearchAssetInEntry_EntriesNoAssetIds() {
//		List<TransactionEntry> te = new ArrayList<TransactionEntry>();
//		TransactionEntry te1 = DiamantFactory.eINSTANCE.createTransactionEntry();
//		te.add(te1);
//		Mockito.when(repository.createQueryBuilder()).thenReturn(builder);
//		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
//		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
//		Mockito.when(builder.build()).thenReturn(query);
//		Mockito.when(repository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(te);
//		
//		setupServices();
//		List<Asset> assets = assetService.searchAssetInEntry(DiamantPackage.Literals.OUTBOUND_LOGISTIC__TRANSPORTATION_TRACKING_ID, "2");
//		assertNotNull(assets);
//		assertTrue(assets.isEmpty());
//		Mockito.verify(repository, Mockito.times(1)).getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap());
//		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testSearchAssetInEntry_Result() {
//		List<TransactionEntry> te = new ArrayList<TransactionEntry>();
//		TransactionEntry te1 = DiamantFactory.eINSTANCE.createTransactionEntry();
//		te1.setAssetId("123");
//		te.add(te1);
//		Mockito.when(repository.createQueryBuilder()).thenReturn(builder);
//		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
//		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
//		Mockito.when(builder.in(Mockito.any(Object[].class))).thenReturn(builder);
//		Mockito.when(builder.build()).thenReturn(query);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Product p = DiamantFactory.eINSTANCE.createProduct();
//		p.setId("123");
//		List<Asset> al = new ArrayList<Asset>();
//		al.add(p);
//		Mockito.when(repository.getEObjectsByQuery(ecC.capture(), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(te, al);
//		
//		setupServices();
//		List<Asset> assets = assetService.searchAssetInEntry(DiamantPackage.Literals.OUTBOUND_LOGISTIC__TRANSPORTATION_TRACKING_ID, "2");
//		assertNotNull(assets);
//		assertEquals(1, assets.size());
//		assertEquals(1, assets.stream().filter(a->a.getId().equals("123")).count());
//		assertEquals(DiamantPackage.Literals.TRANSACTION_ENTRY, ecC.getAllValues().get(0));
//		assertEquals(DiamantPackage.Literals.ASSET, ecC.getAllValues().get(1));
//		
//		Mockito.verify(repository, Mockito.times(2)).getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap());
//		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testGetAssetByOwner_NoOwnerNoType() {
//		setupServices();
//		assetService.getAssetsByOwner(null, null);
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testGetAssetByOwner_NoOwner() {
//		setupServices();
//		assetService.getAssetsByOwner(null, DiamantPackage.Literals.PRODUCT);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testGetAssetByOwner_Result() {
//		String ownerId = "myOwner";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("p1");
//		p1.setCreatorId(ownerId);
//		Product p2 = DiamantFactory.eINSTANCE.createProduct();
//		p2.setId("p2");
//		p2.setCreatorId(ownerId);
//		List<Product> pl = new ArrayList<Product>();
//		pl.add(p1);
//		pl.add(p2);
//		Mockito.when(repository.createQueryBuilder()).thenReturn(builder);
//		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
//		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
//		Mockito.when(builder.build()).thenReturn(query);
//		Mockito.when(repository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(pl);
//		
//		setupServices();
//		List<Asset> product = assetService.getAssetsByOwner(ownerId, DiamantPackage.Literals.PRODUCT);
//		assertNotNull(product);
//		assertEquals(2, product.size());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p1")).count());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p2")).count());
//		
//		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testGetAssetByOwner_ResultDefaultType() {
//		String ownerId = "myOwner";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("p1");
//		p1.setCreatorId(ownerId);
//		Product p2 = DiamantFactory.eINSTANCE.createProduct();
//		p2.setId("p2");
//		p2.setCreatorId(ownerId);
//		List<Product> pl = new ArrayList<Product>();
//		pl.add(p1);
//		pl.add(p2);
//		Mockito.when(repository.createQueryBuilder()).thenReturn(builder);
//		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
//		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
//		Mockito.when(builder.build()).thenReturn(query);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObjectsByQuery(ecC.capture(), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(pl);
//		
//		setupServices();
//		List<Asset> product = assetService.getAssetsByOwner(ownerId, null);
//		assertNotNull(product);
//		assertEquals(2, product.size());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p1")).count());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p2")).count());
//		assertEquals(DiamantPackage.Literals.ASSET, ecC.getValue());
//		
//		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testGetAssetByParticipant_NoOwnerNoType() {
//		setupServices();
//		assetService.getAssetsByParticipant(null, null);
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testGetAssetByParticipant_NoOwner() {
//		setupServices();
//		assetService.getAssetsByParticipant(null, DiamantPackage.Literals.PRODUCT);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testGetAssetByParticipant_Result() {
//		String ownerId = "myOwner";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("p1");
//		p1.setCreatorId(ownerId);
//		Product p2 = DiamantFactory.eINSTANCE.createProduct();
//		p2.setId("p2");
//		p2.setCreatorId(ownerId);
//		List<Product> pl = new ArrayList<Product>();
//		pl.add(p1);
//		pl.add(p2);
//		Mockito.when(repository.createQueryBuilder()).thenReturn(builder);
//		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
//		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
//		Mockito.when(builder.build()).thenReturn(query);
//		Mockito.when(repository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(pl);
//		
//		setupServices();
//		List<Asset> product = assetService.getAssetsByParticipant(ownerId, DiamantPackage.Literals.PRODUCT);
//		assertNotNull(product);
//		assertEquals(2, product.size());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p1")).count());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p2")).count());
//		
//		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testGetAssetByParticipant_ResultDefaultType() {
//		String ownerId = "myOwner";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("p1");
//		p1.setCreatorId(ownerId);
//		Product p2 = DiamantFactory.eINSTANCE.createProduct();
//		p2.setId("p2");
//		p2.setCreatorId(ownerId);
//		List<Product> pl = new ArrayList<Product>();
//		pl.add(p1);
//		pl.add(p2);
//		Mockito.when(repository.createQueryBuilder()).thenReturn(builder);
//		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
//		Mockito.when(builder.simpleValue(Mockito.anyObject())).thenReturn(builder);
//		Mockito.when(builder.build()).thenReturn(query);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObjectsByQuery(ecC.capture(), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(pl);
//		
//		setupServices();
//		List<Asset> product = assetService.getAssetsByParticipant(ownerId, null);
//		assertNotNull(product);
//		assertEquals(2, product.size());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p1")).count());
//		assertEquals(1, product.stream().filter(p->p.getId().equals("p2")).count());
//		assertEquals(DiamantPackage.Literals.ASSET, ecC.getValue());
//		
//		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testUpdateOwner_NullAssetNullOwnerNullType() {
//		setupServices();
//		assetService.updateOwner(null, null, null);
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testUpdateOwner_NoAssetNullOwnerNullType() {
//		setupServices();
//		assetService.updateOwner(null, "test", null);
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testUpdateOwner_NullAssetNoOwnerNullType() {
//		setupServices();
//		assetService.updateOwner("test", null, null);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testUpdateOwner_NullTypeNoAsset() {
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(null);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setId("test");
//		p.setName("Emil Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		setupServices();
//		Asset resultAsset = assetService.updateOwner("test", "asset1", null);
//		assertNull(resultAsset);
//		assertEquals(DiamantPackage.Literals.ASSET, ecC.getValue());
//		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
//		Mockito.verify(participantService, Mockito.never()).appendAsset(Mockito.anyString(), Mockito.any(Asset.class));
//		Mockito.verify(transactionEntryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
//		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testUpdateOwner_NullTypeNoParticipant() {
//		String creatorId = "myCreator";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("asset1");
//		p1.setCreatorId(creatorId);
//		p1.setOwnerId(creatorId);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setId("test");
//		p.setName("Emil Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		setupServices();
//		assertEquals(creatorId, p1.getOwnerId());
//		Asset resultAsset = assetService.updateOwner("test", "asset1", null);
//		assertEquals("test", resultAsset.getOwnerId());
//		assertEquals(creatorId, p1.getOwnerId());
//		assertNotEquals(p1, resultAsset);
//		assertEquals(DiamantPackage.Literals.ASSET, ecC.getValue());
//		
//		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
//		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
//		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testUpdateOwner_NoParticipant() {
//		String creatorId = "myCreator";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("asset1");
//		p1.setCreatorId(creatorId);
//		p1.setOwnerId(creatorId);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setId("test");
//		p.setName("Emil Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		setupServices();
//		assertEquals(creatorId, p1.getOwnerId());
//		Asset resultAsset = assetService.updateOwner("test", "asset1", DiamantPackage.Literals.PRODUCT);
//		assertEquals("test", resultAsset.getOwnerId());
//		assertEquals(creatorId, p1.getOwnerId());
//		assertNotEquals(p1, resultAsset);
//		assertEquals(DiamantPackage.Literals.PRODUCT, ecC.getValue());
//		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
//		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
//		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@Test(expected = IllegalStateException.class)
//	public void testUpdateOwner_NoValidParticipant() {
//		String creatorId = "myCreator";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("asset1");
//		p1.setCreatorId(creatorId);
//		p1.setOwnerId(creatorId);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
//		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(null);
//		setupServices();
//		assertEquals(creatorId, p1.getOwnerId());
//		assetService.updateOwner("test", "asset1", DiamantPackage.Literals.PRODUCT);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testUpdateOwner_Participant() {
//		String creatorId = "myCreator";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("asset1");
//		p1.setCreatorId(creatorId);
//		p1.setOwnerId(creatorId);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setId("test");
//		p.setName("Emil Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		setupServices();
//		assertEquals(creatorId, p1.getOwnerId());
//		Asset resultAsset = assetService.updateOwner("test", "asset1", null);
//		assertEquals(creatorId, p1.getOwnerId());
//		assertEquals("test", resultAsset.getOwnerId());
//		assertEquals(p.getDescription(), resultAsset.getOwnerName());
//		assertNotEquals(p1, resultAsset);
//		assertEquals(DiamantPackage.Literals.ASSET, ecC.getValue());
//		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
//		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
//		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testUpdateOwner_ParticipantSameOwner() {
//		String creatorId = "myCreator";
//		Product p1 = DiamantFactory.eINSTANCE.createProduct();
//		p1.setId("asset1");
//		p1.setCreatorId(creatorId);
//		p1.setOwnerId(creatorId);
//		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
//		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
//		Participant p = DiamantFactory.eINSTANCE.createParticipant();
//		p.setId("test");
//		p.setName("Emil Tester");
//		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
//		setupServices();
//		assertEquals(creatorId, p1.getOwnerId());
//		Asset resultAsset = assetService.updateOwner(creatorId, "asset1", null);
//		assertEquals(creatorId, p1.getOwnerId());
//		assertEquals(p1, resultAsset);
//		assertEquals(DiamantPackage.Literals.ASSET, ecC.getValue());
//		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
//		Mockito.verify(participantService, Mockito.never()).appendAsset(Mockito.anyString(), Mockito.any(Asset.class));
//		Mockito.verify(transactionEntryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
//		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
//	}
	
}

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
package org.gecko.notary.service.itest;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

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
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAsset(NotaryPackage.Literals.ASSET__CREATOR_ID, null, null);
		});
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAsset(null, "test", null);
		});
	}
	
	@Test
	public void testSearchAsset_OnlyType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAsset(null, null, NotaryPackage.Literals.ASSET);
		});
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAsset(NotaryPackage.Literals.ASSET__CREATOR_ID, null, NotaryPackage.Literals.ASSET);
		});
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAsset(null, "test", NotaryPackage.Literals.ASSET);
		});
	}
	
	@Test
	public void testSearchAsset_ResultTextProvider(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
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
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
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
	
	@Test
	public void testSearchAssetInEntry_NullParameters(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAssetInEntry(null, null);
		});
	}
	
	@Test
	public void testSearchAssetInEntry_OnlyValue(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.searchAssetInEntry(null, "test");
		});
	}
	
	@Test
	public void testSearchAssetInEntry_NoEntries(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;
		
		List<EObject> te = new ArrayList<>();
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(te);
		// always return the whole list
		List<Asset> assets = assetService.searchAssetInEntry(NotaryPackage.Literals.TRANSACTION_ENTRY__COMMENT, "test");
		assertNotNull(assets);
		assertTrue(assets.isEmpty());
		
		Mockito.verify(queryRepository, Mockito.times(1)).getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap());
		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testSearchAssetInEntry_EntriesNoAssetIds(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		List<EObject> te = new ArrayList<>();
		TransactionEntry te1 = NotaryFactory.eINSTANCE.createTransactionEntry();
		te.add(te1);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(te);
		// always return whole list
		List<Asset> assets = assetService.searchAssetInEntry(NotaryPackage.Literals.TRANSACTION_ENTRY__COMMENT, "test");
		assertNotNull(assets);
		assertTrue(assets.isEmpty());
		Mockito.verify(queryRepository, Mockito.times(1)).getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap());
		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchAssetInEntry_Result(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		List<EObject> te = new ArrayList<>();
		TransactionEntry te1 = NotaryFactory.eINSTANCE.createTransactionEntry();
		te1.setAssetId("123");
		te.add(te1);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.in(Mockito.any(Object[].class))).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Asset a = NotaryFactory.eINSTANCE.createAsset();
		a.setId("123");
		List<EObject> al = new ArrayList<EObject>();
		al.add(a);
		Mockito.when(queryRepository.getEObjectsByQuery(ecC.capture(), Mockito.any(IQuery.class), Mockito.any())).thenReturn(te, al);
		
		List<Asset> assets = assetService.searchAssetInEntry(NotaryPackage.Literals.TRANSACTION_ENTRY__COMMENT, "test");
		assertNotNull(assets);
		assertEquals(1, assets.size());
		assertEquals(1, assets.stream().filter(as->as.getId().equals("123")).count());
		assertEquals(NotaryPackage.Literals.TRANSACTION_ENTRY, ecC.getAllValues().get(0));
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getAllValues().get(1));
		
		Mockito.verify(queryRepository, Mockito.times(2)).getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap());
		Mockito.verify(textProvider, Mockito.times(1)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testGetAssetsByOwner_NoOwnerNoType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetsByOwner(null, null);
		});
	}
	
	@Test
	public void testGetAssetsByOwner_NoOwner(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetsByOwner(null, NotaryPackage.Literals.ASSET);
		});
	}
	
	@Test
	public void testGetAssetsByOwner_Result(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ownerId = "myOwner";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("a1");
		a1.setCreatorId(ownerId);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("a2");
		a2.setCreatorId(ownerId);
		List<EObject> al = new ArrayList<>();
		al.add(a1);
		al.add(a2);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(al);
		
		List<Asset> product = assetService.getAssetsByOwner(ownerId, NotaryPackage.Literals.ASSET);
		assertNotNull(product);
		assertEquals(2, product.size());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a1")).count());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a2")).count());
		
		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testGetAssetsByOwner_ResultDefaultType(@InjectService AssetService assetService, 
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ownerId = "myOwner";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("a1");
		a1.setCreatorId(ownerId);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("a2");
		a2.setCreatorId(ownerId);
		List<EObject> al = new ArrayList<>();
		al.add(a1);
		al.add(a2);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(queryRepository.getEObjectsByQuery(ecC.capture(), Mockito.any(IQuery.class), Mockito.any())).thenReturn(al);
		
		List<Asset> assets = assetService.getAssetsByOwner(ownerId, null);
		assertNotNull(assets);
		assertEquals(2, assets.size());
		assertEquals(1, assets.stream().filter(p->p.getId().equals("a1")).count());
		assertEquals(1, assets.stream().filter(p->p.getId().equals("a2")).count());
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		
		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testGetAssetByParticipant_NoIdOwnerNoType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByParticipant(null, null, null);
		});
	}
	
	@Test
	public void testGetAssetByParticipant_NoOwner(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByParticipant("test", null, NotaryPackage.Literals.ASSET);
		});
	}
	
	@Test
	public void testGetAssetByParticipant_NoType(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(null);
		assertNull(assetService.getAssetByParticipant("test", "user", null));
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	@Test
	public void testGetAssetByParticipant_WrongParticipant1(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("test");
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(asset);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByParticipant("test", "user", NotaryPackage.Literals.ASSET);
		});
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	@Test
	public void testGetAssetByParticipant_WrongParticipant2(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("test");
		asset.setCreatorId("knut");
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(asset);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByParticipant("test", "user", NotaryPackage.Literals.ASSET);
		});
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	@Test
	public void testGetAssetByParticipant(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("test");
		asset.setCreatorId("user");
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(asset);
		assertEquals(asset, assetService.getAssetByParticipant("test", "user", NotaryPackage.Literals.ASSET));
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	
	@Test
	public void testGetAssetByOwner_NoIdNoOwnerNoType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByOwner(null, null, null);
		});
	}
	
	@Test
	public void testGetAssetByOwner_NoOwner(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByOwner("test", null, NotaryPackage.Literals.ASSET);
		});
	}
	
	@Test
	public void testGetAssetByOwner_NoType(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(null);
		assertNull(assetService.getAssetByOwner("test", "user", null));
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	@Test
	public void testGetAssetByOwner_WrongParticipant1(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("test");
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(asset);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByOwner("test", "user", NotaryPackage.Literals.ASSET);
		});
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	@Test
	public void testGetAssetByOwner_WrongOwner2(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("test");
		asset.setOwnerId("knut");
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(asset);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetByOwner("test", "user", NotaryPackage.Literals.ASSET);
		});
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	@Test
	public void testGetAssetByOwner(@InjectService AssetService assetService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("test");
		asset.setOwnerId("user");
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.any(String.class), Mockito.anyMap())).thenReturn(asset);
		assertEquals(asset, assetService.getAssetByOwner("test", "user", NotaryPackage.Literals.ASSET));
		assertEquals(ecC.getValue(), NotaryPackage.Literals.ASSET);
	}
	
	@Test
	public void testGetAssetsByParticipant_NoOwnerNoType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetsByParticipant(null, null);
		});
	}
	
	@Test
	public void testGetAssetsByParticipant_NoOwner(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.getAssetsByParticipant(null, NotaryPackage.Literals.ASSET);
		});
	}
	
	@Test
	public void testGetAssetsByParticipant_Result(@InjectService AssetService assetService,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ownerId = "myOwner";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("a1");
		a1.setCreatorId(ownerId);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("a2");
		a2.setCreatorId(ownerId);
		List<EObject> al = new ArrayList<>();
		al.add(a1);
		al.add(a2);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectsByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.any())).thenReturn(al);
		
		List<Asset> product = assetService.getAssetsByParticipant(ownerId, NotaryPackage.Literals.ASSET);
		assertNotNull(product);
		assertEquals(2, product.size());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a1")).count());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a2")).count());
		
		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testGetAssetsByParticipant_ResultDefaultType(@InjectService AssetService assetService,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(assetService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ownerId = "myOwner";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("a1");
		a1.setCreatorId(ownerId);
		Asset a2 = NotaryFactory.eINSTANCE.createAsset();
		a2.setId("a2");
		a2.setCreatorId(ownerId);
		List<EObject> al = new ArrayList<>();
		al.add(a1);
		al.add(a2);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(queryRepository.getEObjectsByQuery(ecC.capture(), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(al);
		
		List<Asset> product = assetService.getAssetsByParticipant(ownerId, null);
		assertNotNull(product);
		assertEquals(2, product.size());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a1")).count());
		assertEquals(1, product.stream().filter(p->p.getId().equals("a2")).count());
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		
		Mockito.verify(textProvider, Mockito.times(2)).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testUpdateOwner_NullAssetNullOwnerNullType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateOwner(null, null, null);
		});
	}
	
	@Test
	public void testUpdateOwner_NoAssetNullOwnerNullType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateOwner(null, "test", null);
		});
	}
	
	@Test
	public void testUpdateOwner_NullAssetNoOwnerNullType(@InjectService AssetService assetService, @InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateOwner("test", null, null);
		});
	}
	
	@Test
	public void testUpdateOwner_NullTypeNoAsset(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(transactionEntryService);
		assertNotNull(assetService);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(null);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId("test");
		p.setName("Emil Tester");
		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
		Asset resultAsset = assetService.updateOwner("test", "asset1", null);
		assertNull(resultAsset);
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
		Mockito.verify(participantService, Mockito.never()).appendAsset(Mockito.anyString(), Mockito.any(Asset.class));
		Mockito.verify(transactionEntryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.anyMap());
	}
	
	@Test
	public void testUpdateOwner_NullTypeNoParticipant(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(eventAdmin);
		assertNotNull(assetService);
		String creatorId = "myCreator";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId(creatorId);
		a1.setOwnerId(creatorId);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId("test");
		p.setName("Emil Tester");
		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
		assertEquals(creatorId, a1.getOwnerId());
		Asset resultAsset = assetService.updateOwner("test", "asset1", null);
		assertEquals("test", resultAsset.getOwnerId());
		assertEquals(creatorId, a1.getOwnerId());
		assertNotEquals(a1, resultAsset);
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testUpdateOwner_NoParticipant(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(eventAdmin);
		assertNotNull(assetService);
		String creatorId = "myCreator";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId(creatorId);
		a1.setOwnerId(creatorId);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId("test");
		p.setName("Emil Tester");
		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
		assertEquals(creatorId, a1.getOwnerId());
		Asset resultAsset = assetService.updateOwner("test", "asset1", NotaryPackage.Literals.ASSET);
		assertEquals("test", resultAsset.getOwnerId());
		assertEquals(creatorId, a1.getOwnerId());
		assertNotEquals(a1, resultAsset);
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testUpdateOwner_NoValidParticipant(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(assetService);
		assertNotNull(participantService);
		String creatorId = "myCreator";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId(creatorId);
		a1.setOwnerId(creatorId);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		Mockito.when(participantService.getDefinition(Mockito.anyString())).thenReturn(null);
		assertEquals(creatorId, a1.getOwnerId());
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			assetService.updateOwner("test", "asset1", NotaryPackage.Literals.ASSET);
		});
	}
	
	@Test
	public void testUpdateOwner_Participant(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(eventAdmin);
		assertNotNull(assetService);
		String creatorId = "myCreator";
		Asset a1 = NotaryFactory.eINSTANCE.createAsset();
		a1.setId("asset1");
		a1.setCreatorId(creatorId);
		a1.setOwnerId(creatorId);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(a1);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId("test");
		p.setName("Emil Tester");
		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
		assertEquals(creatorId, a1.getOwnerId());
		Asset resultAsset = assetService.updateOwner("test", "asset1", null);
		assertEquals(creatorId, a1.getOwnerId());
		assertEquals("test", resultAsset.getOwnerId());
		assertEquals(p.getDescription(), resultAsset.getOwnerName());
		assertNotEquals(a1, resultAsset);
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class), Mockito.anyMap());
		Mockito.verify(eventAdmin, Mockito.times(1)).sendEvent(Mockito.any(Event.class));
		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
	@Test
	public void testUpdateOwner_ParticipantSameOwner(@InjectService AssetService assetService, 
			@InjectService ParticipantService participantService,
			@InjectService TransactionEntryService transactionEntryService,
			@InjectService EventAdmin eventAdmin,
			@InjectService TextProvider textProvider,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(textProvider);
		assertNotNull(participantService);
		assertNotNull(transactionEntryService);
		assertNotNull(eventAdmin);
		assertNotNull(assetService);
		String creatorId = "myCreator";
		Asset p1 = NotaryFactory.eINSTANCE.createAsset();
		p1.setId("asset1");
		p1.setCreatorId(creatorId);
		p1.setOwnerId(creatorId);
		ArgumentCaptor<EClass> ecC = ArgumentCaptor.forClass(EClass.class);
		Mockito.when(repository.getEObject(ecC.capture(), Mockito.anyString(), Mockito.anyMap())).thenReturn(p1);
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId("test");
		p.setName("Emil Tester");
		Mockito.when(participantService.getParticipant(Mockito.anyString())).thenReturn(p);
		assertEquals(creatorId, p1.getOwnerId());
		Asset resultAsset = assetService.updateOwner(creatorId, "asset1", null);
		assertEquals(creatorId, p1.getOwnerId());
		assertEquals(p1, resultAsset);
		assertEquals(NotaryPackage.Literals.ASSET, ecC.getValue());
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
		Mockito.verify(participantService, Mockito.never()).appendAsset(Mockito.anyString(), Mockito.any(Asset.class));
		Mockito.verify(transactionEntryService, Mockito.never()).createAssetModificationTransaction(Mockito.any(Asset.class), Mockito.any(Asset.class));
		Mockito.verify(textProvider, Mockito.never()).provideText(Mockito.any(EObject.class), Mockito.any());
	}
	
}

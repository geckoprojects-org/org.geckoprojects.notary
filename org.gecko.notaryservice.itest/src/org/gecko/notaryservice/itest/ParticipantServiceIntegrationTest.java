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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.IQuery;
import org.gecko.emf.repository.query.IQueryBuilder;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.Address;
import org.gecko.notary.model.notary.Asset;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.service.api.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
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
public class ParticipantServiceIntegrationTest {

	public interface QueryRepositoryMock extends EMFRepository, QueryRepository {

	}

	private IQueryBuilder builder;
	private IQuery query;

	@BeforeEach
	private void setupServices(@InjectBundleContext BundleContext bc) {
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
	public void testSimple_NullName(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.createSimpleParticipant(null, null);
		});
	}

	@Test
	public void testSimple_NullDescriptionNew(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ID = "1234";
		String NAME = "test";
		String DESCRIPTION = null;
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(null);
		Mockito.doAnswer(new Answer<EObject>() {

			@Override
			public EObject answer(InvocationOnMock invocation) throws Throwable {
				EObject param = invocation.getArgument(0, EObject.class);
				if (param instanceof Participant) {
					Participant p = (Participant) param;
					p.setId(ID);
				}
				return param;
			}
		}).when(repository).save(Mockito.any(EObject.class));

		ParticipantDefinition definition = participantService.createSimpleParticipant(NAME, DESCRIPTION);
		assertNotNull(definition);
		assertEquals(ID, definition.getId());

		Participant participant = definition.getParticipant();
		assertNotNull(participant);
		assertEquals(ID, participant.getId());
		assertEquals(NAME, participant.getName());
		assertNull(participant.getDescription());

		Mockito.verify(repository, Mockito.times(2)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testSimple_DescriptionNew(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ID = "1234";
		String NAME = "test";
		String DESCRIPTION = "mydescription";
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.anyMap())).thenReturn(null);
		Mockito.doAnswer(new Answer<EObject>() {

			@Override
			public EObject answer(InvocationOnMock invocation) throws Throwable {
				EObject param = invocation.getArgument(0, EObject.class);
				if (param instanceof Participant) {
					Participant p = (Participant) param;
					p.setId(ID);
				}
				return param;
			}
		}).when(repository).save(Mockito.any(EObject.class));

		ParticipantDefinition definition = participantService.createSimpleParticipant(NAME, DESCRIPTION);
		assertNotNull(definition);
		assertEquals(ID, definition.getId());

		Participant participant = definition.getParticipant();
		assertNotNull(participant);
		assertEquals(ID, participant.getId());
		assertEquals(NAME, participant.getName());
		assertEquals(DESCRIPTION, participant.getDescription());

		Mockito.verify(repository, Mockito.times(2)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testSimple_NullDescriptionExisting(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ID = "1222";
		String NAME = "test";
		String DESCRIPTION = null;
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.any())).thenReturn(part, def);

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.createSimpleParticipant(NAME, DESCRIPTION);
		});
	}

	@Test
	public void testSimple_DescriptionExisting(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertTrue(repository instanceof QueryRepository);
		QueryRepository queryRepository = (QueryRepository) repository;	
		String ID = "1222";
		String NAME = "test";
		String DESCRIPTION = "bla";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Mockito.when(queryRepository.createQueryBuilder()).thenReturn(builder);
		Mockito.when(builder.column(Mockito.any(EAttribute.class))).thenReturn(builder);
		Mockito.when(builder.simpleValue(Mockito.any())).thenReturn(builder);
		Mockito.when(builder.build()).thenReturn(query);
		Mockito.when(queryRepository.getEObjectByQuery(Mockito.any(EClass.class), Mockito.any(IQuery.class), Mockito.any())).thenReturn(part, def);

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.createSimpleParticipant(NAME, DESCRIPTION);
		});
	}

	@Test
	public void testGetDefinition_Null(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.getDefinition(null);
		});
	}	

	@Test
	public void testGetDefinition_NoExist(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertNull(participantService.getDefinition("test"));
	}		

	@Test
	public void testGetDefinition_Exist(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);

		ParticipantDefinition definition = participantService.getDefinition(ID);
		assertNotNull(definition);
		assertEquals(def, definition);
	}

	@Test
	public void testGetDefinition_ThrowNPE(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenThrow(new NullPointerException("hello"));
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.getDefinition("test");
		});
	}	

	@Test
	public void testGetParticipant_Null(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.getParticipant(null);
		});
	}	

	@Test
	public void testGetParticipant_NoExist(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		assertNull(participantService.getParticipant("test"));
	}	

	@Test
	public void testGetParticipant_Exist(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(part);

		Participant participant = participantService.getParticipant(ID);
		assertNotNull(participant);
		assertEquals(part, participant);
	}

	@Test
	public void testGetParticipant_ThrowNPE(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenThrow(new NullPointerException("hello"));
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.getParticipant("test");
		});
	}	

	@Test
	public void testUpdateAddress_Null(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Participant updated = participantService.updateAddress(null, null);
		assertNull(updated);
	}	


	@Test
	public void testUpdateAddress_NoParticipant(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Address a = NotaryFactory.eINSTANCE.createAddress();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.updateAddress(null, a);
		});
	}

	@Test
	public void testUpdateAddress_ParticipantNoExist(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		Address a = NotaryFactory.eINSTANCE.createAddress();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.updateAddress("test", a);
		});
	}	

	@Test
	public void testUpdateAddress_NoCurrentAddress(@InjectService ParticipantService participantService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(rs);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(p);

		assertNull(p.getAddress());

		Address a = NotaryFactory.eINSTANCE.createAddress();
		a.setCity("Testtown");
		Participant result = participantService.updateAddress(ID, a);
		assertNotNull(result);
		assertNotNull(result.getAddress());
		assertEquals("Testtown", result.getAddress().getCity());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateAddress_ExistingAddress(@InjectService ParticipantService participantService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(rs);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		Address a1 = NotaryFactory.eINSTANCE.createAddress();
		a1.setCity("Testtown");
		p.setAddress(a1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(p);

		assertNotNull(p.getAddress());
		assertEquals("Testtown", p.getAddress().getCity());

		Address a2 = NotaryFactory.eINSTANCE.createAddress();
		a2.setCity("Mytown");

		Participant result = participantService.updateAddress(ID, a2);
		assertNotNull(result);
		assertNotNull(result.getAddress());
		assertEquals(a2, result.getAddress());
		assertEquals("Mytown", result.getAddress().getCity());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateAddress_NoChange(@InjectService ParticipantService participantService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(rs);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		Address a1 = NotaryFactory.eINSTANCE.createAddress();
		a1.setCity("Testtown");
		p.setAddress(a1);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(p);

		assertNotNull(p.getAddress());
		assertEquals("Testtown", p.getAddress().getCity());

		Address a2 = NotaryFactory.eINSTANCE.createAddress();
		a2.setCity("Testtown");

		Participant result = participantService.updateAddress(ID, a2);
		assertNotNull(result);
		assertNotNull(result.getAddress());

		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateParticipant_Null(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Participant updated = participantService.updateParticipant(null);
		assertNull(updated);
	}

	@Test
	public void testUpdateParticipant_NoName(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Participant update = NotaryFactory.eINSTANCE.createParticipant();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.updateParticipant(update);
		});
	}

	@Test
	public void testUpdateParticipant_NoId(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Mockito.doAnswer(new Answer<EObject>() {

			@Override
			public EObject answer(InvocationOnMock invocation) throws Throwable {
				EObject param = invocation.getArgument(0, EObject.class);
				if (param instanceof Participant) {
					Participant p = (Participant) param;
					p.setId(ID);
				}
				return param;
			}
		}).when(repository).save(Mockito.any(EObject.class));
		Participant update = NotaryFactory.eINSTANCE.createParticipant();
		update.setName(NAME);

		assertNull(update.getId());
		Participant updated = participantService.updateParticipant(update);
		assertEquals(ID, updated.getId());
		assertEquals(NAME, updated.getName());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateParticipant_ExistingSame(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Participant existing = NotaryFactory.eINSTANCE.createParticipant();
		existing.setName(NAME);
		existing.setId(ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenAnswer(m->EcoreUtil.copy(existing));
		Participant update = EcoreUtil.copy(existing);

		assertNotEquals(existing, update);

		Participant updated = participantService.updateParticipant(update);
		assertEquals(ID, updated.getId());
		assertEquals(NAME, updated.getName());
		assertEquals(update, updated);

		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateParticipant_ExistingNotSame(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		String NAME_UPD = "test-me";
		Participant existing = NotaryFactory.eINSTANCE.createParticipant();
		existing.setName(NAME);
		existing.setId(ID);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenAnswer(m->EcoreUtil.copy(existing));
		Participant update = EcoreUtil.copy(existing);
		update.setName(NAME_UPD);

		assertNotEquals(existing, update);

		Participant updated = participantService.updateParticipant(update);
		assertEquals(ID, updated.getId());
		assertEquals(NAME_UPD, updated.getName());
		assertEquals(update, updated);

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateDefinition_Null(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		ParticipantDefinition updated = participantService.updateParticipantDefinition(null);
		assertNull(updated);
	}

	@Test
	public void testUpdateDefinition_NoParticipant(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.updateParticipantDefinition(update);
		});
	}

	@Test
	public void testUpdateDefinition_NoParticipantId(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		update.setParticipant(p);
		update.setId(ID);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.updateParticipantDefinition(update);
		});
	}

	@Test
	public void testUpdateDefinition_NoDefinitionIdNoParticipantResource(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Participant pp = NotaryFactory.eINSTANCE.createParticipant();
		InternalEObject ipp = (InternalEObject) pp;
		ipp.eSetProxyURI(URI.createURI("test/" + ID + "diamant"));
		Mockito.when(repository.createProxy(Mockito.any(EClass.class), Mockito.anyString())).thenReturn(ipp);

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		update.setParticipant(p);

		ParticipantDefinition updated = participantService.updateParticipantDefinition(update);

		assertEquals(ID, updated.getId());
		assertNotNull(update.getParticipant());
		assertTrue(update.getParticipant().eIsProxy());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateDefinition_NoDefinitionIdWithParticipantResource(@InjectService ParticipantService participantService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(rs);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setParticipant(p);

		ParticipantDefinition updated = participantService.updateParticipantDefinition(update);

		assertEquals(ID, updated.getId());
		assertNotNull(update.getParticipant());
		assertNotNull(update.getParticipant().eResource());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateDefinition_DefinitionIdWithParticipantResource(@InjectService ParticipantService participantService,
			@InjectService ResourceSet rs,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(rs);
		assertNotNull(participantService);
		String ID = "1222";
		String DEFID = "2111";
		String NAME = "test";
		Resource r = rs.createResource(URI.createURI("test/" + ID + "diamant"));

		ParticipantDefinition update = NotaryFactory.eINSTANCE.createParticipantDefinition();
		Participant p = NotaryFactory.eINSTANCE.createParticipant();
		p.setId(ID);
		p.setName(NAME);
		r.getContents().add(p);
		update.setId(DEFID);
		update.setParticipant(p);

		ParticipantDefinition updated = participantService.updateParticipantDefinition(update);

		assertEquals(DEFID, updated.getId());
		assertNotNull(update.getParticipant());
		assertEquals(ID, updated.getParticipant().getId());
		assertNotNull(update.getParticipant().eResource());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testAppendAsset_NullParticipantIdNullAsset(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.appendAsset(null, null);
		});
	}

	@Test
	public void testAppendAsset_NullParticipantId(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("asset1");
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			participantService.appendAsset(null, asset);
		});
	}

	@Test
	public void testAppendAsset_NoParticipant(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String NAME = "test";
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		Asset asset = NotaryFactory.eINSTANCE.createAsset();
		asset.setId("asset1");

		assertNull(participantService.getDefinition(NAME));
		assertNull(participantService.appendAsset(NAME, asset));
	}

	@Test
	public void testAppendAsset_NullAsset(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);

		ParticipantDefinition resultDef = participantService.appendAsset(ID, null);
		assertEquals(def, resultDef);
		assertTrue(resultDef.getAsset().isEmpty());
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}

	@Test
	public void testAppendAsset_NewAsset(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Asset asset1 = NotaryFactory.eINSTANCE.createAsset();
		asset1.setId("asset1");
		Asset asset2 = NotaryFactory.eINSTANCE.createAsset();
		asset2.setId("asset2");
		def.getAsset().add(asset1);
		def.getAsset().add(asset2);

		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);

		assertEquals(2, def.getAsset().size());
		Asset asset3 = NotaryFactory.eINSTANCE.createAsset();
		asset3.setId("asset3");
		ParticipantDefinition resultDef = participantService.appendAsset(ID, asset3);
		assertEquals(def, resultDef);
		assertEquals(3, resultDef.getAsset().size());
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testAppendAsset_ExistingAsset(@InjectService ParticipantService participantService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(participantService);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Asset asset1 = NotaryFactory.eINSTANCE.createAsset();
		asset1.setId("asset1");
		Asset asset2 = NotaryFactory.eINSTANCE.createAsset();
		asset2.setId("asset2");
		def.getAsset().add(asset1);
		def.getAsset().add(asset2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);

		Asset asset3 = NotaryFactory.eINSTANCE.createAsset();
		asset3.setId("asset1");
		assertEquals(2, def.getAsset().size());

		ParticipantDefinition resultDef = participantService.appendAsset(ID, asset3);
		assertEquals(def, resultDef);
		assertEquals(2, resultDef.getAsset().size());
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}

}

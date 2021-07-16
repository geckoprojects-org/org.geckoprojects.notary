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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.repository.EMFRepository;
import org.gecko.emf.repository.query.QueryRepository;
import org.gecko.notary.model.notary.Contact;
import org.gecko.notary.model.notary.ContactType;
import org.gecko.notary.model.notary.NotaryFactory;
import org.gecko.notary.model.notary.Participant;
import org.gecko.notary.model.notary.ParticipantDefinition;
import org.gecko.notary.service.api.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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
public class ContactServiceIntegrationTest {
	
	public interface QueryRepositoryMock extends EMFRepository, QueryRepository {

	}
	
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
	}

	@Test
	public void testRemoveContact_NoParticipant(@InjectService ContactService contactService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertFalse(contactService.removeContact(null, null));
	}
	
	@Test
	public void testRemoveContact_NoContactId(@InjectService ContactService contactService,
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertFalse(contactService.removeContact("test", null));
	}
	
	@Test
	public void testRemoveContact_UnknownParticipant(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(contactService);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			contactService.removeContact("test", "c1");
		});
	}
	
	@Test
	public void testRemoveContacts_NoContactFound(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
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
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		update.getContact().add(c1);
		update.getContact().add(c2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);
		
		assertEquals(2, update.getContact().size());
		assertFalse(contactService.removeContact(ID, "c3"));
		assertEquals(2, update.getContact().size());
		
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testRemoveContacts(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
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
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		update.getContact().add(c1);
		update.getContact().add(c2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);
		
		assertEquals(2, update.getContact().size());
		
		assertTrue(contactService.removeContact(ID, "c1"));
		
		assertEquals(1, update.getContact().size());
		assertEquals(c2, update.getContact().get(0));
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testUpdateContact_NoParticipant(@InjectService ContactService contactService) {
		assertNotNull(contactService);
		assertNull(contactService.updateContact(null, null));
	}
	
	@Test
	public void testUpdateContacts_NoContact(@InjectService ContactService contactService) {
		assertNotNull(contactService);
		assertNull(contactService.updateContact("test", null));
	}
	
	@Test
	public void testUpdateContacts_UnknownParticipant(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(contactService);
		Contact c = NotaryFactory.eINSTANCE.createContact();
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			contactService.updateContact("test", c);
		});
	}

	@Test
	public void testUpdateContacts(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
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
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);
		
		assertTrue(update.getContact().isEmpty());
		
		Contact c = NotaryFactory.eINSTANCE.createContact();
		c.setId("c1");
		contactService.updateContact(ID, c);
		
		assertEquals(1, update.getContact().size());
		assertEquals(update.getContact().get(0), c);
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testUpdateContacts_NoContactId(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
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
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);
		
		assertTrue(update.getContact().isEmpty());
		
		Contact c = NotaryFactory.eINSTANCE.createContact();
		contactService.updateContact(ID, c);
		
		assertEquals(1, update.getContact().size());
		assertEquals(update.getContact().get(0), c);
		assertNotNull(c.getId());
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}

	@Test
	public void testUpdateContacts_Existing(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
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
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		c1.setType(ContactType.APP);
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		c2.setType(ContactType.EMAIL);
		update.getContact().add(c1);
		update.getContact().add(c2);
		assertEquals(ContactType.EMAIL, update.getContact().get(1).getType());
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);
		
		assertEquals(2, update.getContact().size());
		assertEquals("c2", update.getContact().get(1).getId());
		
		Contact c3 = NotaryFactory.eINSTANCE.createContact();
		c3.setId("c2");
		c3.setType(ContactType.NOTIFICATION);
		
		contactService.updateContact(ID, c3);
		
		assertEquals(2, update.getContact().size());
		assertEquals(ContactType.NOTIFICATION, update.getContact().get(1).getType());
		assertEquals("c2", update.getContact().get(1).getId());
		
		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testUpdateContacts_NoChange(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
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
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		c1.setType(ContactType.APP);
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		c2.setType(ContactType.EMAIL);
		update.getContact().add(c1);
		update.getContact().add(c2);
		assertEquals(ContactType.EMAIL, update.getContact().get(1).getType());
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(update);
		
		assertEquals(2, update.getContact().size());
		assertEquals("c2", update.getContact().get(1).getId());
		
		Contact c3 = NotaryFactory.eINSTANCE.createContact();
		c3.setId("c2");
		c3.setType(ContactType.EMAIL);
		
		contactService.updateContact(ID, c3);
		
		assertEquals(2, update.getContact().size());
		assertEquals(c2, update.getContact().get(1));
		
		Mockito.verify(repository, Mockito.never()).save(Mockito.any(EObject.class));
	}
	
	@Test
	public void testUpdateContacts_NoParticipantContact(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(contactService);
		Contact c = NotaryFactory.eINSTANCE.createContact();
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			contactService.updateContact(null, c);
		});

	}
	
	@Test
	public void testGetContacts_NoParticipant(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			contactService.getContacts(null);
		});
		
	}
	
	@Test
	public void testGetContacts_ParticipantNotExist(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository) {
		assertNotNull(repository);
		assertNotNull(contactService);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(null);
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
			contactService.getContacts("test");
		});
		
	}

	@Test
	public void testGetContacts_Exist(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		Contact c1 = NotaryFactory.eINSTANCE.createContact();
		c1.setId("c1");
		c1.setType(ContactType.EMAIL);
		Contact c2 = NotaryFactory.eINSTANCE.createContact();
		c2.setId("c2");
		c2.setType(ContactType.APP);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		def.getContact().add(c1);
		def.getContact().add(c2);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);
		
		List<Contact> contacts = contactService.getContacts(ID);
		assertNotNull(contacts);
		assertEquals(2, contacts.size());
	}
	
	@Test
	public void testGetContacts_ExistNoContent(@InjectService ContactService contactService, 
			@InjectService EMFRepository repository,
			@InjectService ResourceSet rs) {
		assertNotNull(repository);
		assertNotNull(contactService);
		assertNotNull(rs);
		String ID = "1222";
		String NAME = "test";
		Participant part = NotaryFactory.eINSTANCE.createParticipant();
		part.setId(ID);
		part.setName(NAME);
		ParticipantDefinition def = NotaryFactory.eINSTANCE.createParticipantDefinition();
		def.setId(ID);
		def.setParticipant(part);
		Mockito.when(repository.getEObject(Mockito.any(EClass.class), Mockito.any())).thenReturn(def);
		
		List<Contact> contacts = contactService.getContacts(ID);
		assertTrue(contacts.isEmpty());
	}
	
}

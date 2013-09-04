package org.commonpatriots.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.commonpatriots.CPRuntimeException;
import org.commonpatriots.proto.CPData.ContactInfo;
import org.commonpatriots.proto.CPData.State;
import org.commonpatriots.proto.CPData.User;
import org.commonpatriots.proto.CPData.User.UserType;
import org.commonpatriots.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.inject.util.Providers;

public class UserBoTest {
	private UserBo bo;
	@Mock private BaseDAO dao;

	private User user = User.newBuilder().setType(UserType.CUSTOMER).setId("TEST_ID")
			.setContactInfo(ContactInfo.newBuilder().setEmail("test@blank.com").setPhone("555-555-5555")
					.setAddress("123 Main St").setCity("Nowheresville").setState(State.TX).setZip(11111)).build();
	private com.google.appengine.api.users.User googleUser = null;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		bo = new UserBo(Providers.of(dao));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOpen() {
		bo.open();
		assertEquals(User.newBuilder().build(), bo.toDataObject());
		assertNull(bo.getGoogleUser());
	}

	@Test
	public void testOpen_existingId() {
		final String id = "TEST_ID";
		Mockito.when(dao.getUserById(id)).thenReturn(Pair.of(user, googleUser));
		bo.open(id);
		assertEquals(user, bo.toDataObject());
		assertEquals(googleUser, bo.getGoogleUser());
	}

	@Test
	public void testOpen_newId() {
		final String id = "TEST_ID";
		Mockito.when(dao.getUserById(id)).thenThrow(dao.new CPDAOException("Test"));
		bo.open(id);
		assertEquals(User.newBuilder().setId(id).build(), bo.toDataObject());
		assertNull(bo.getGoogleUser());
	}

	@Test
	public void testOpen_newGaiaId() {
		Mockito.when(dao.getUserByGoogleUser(googleUser)).thenThrow(dao.new CPDAOException("Test"));
		bo.open(googleUser);
		assertEquals(User.getDefaultInstance(), bo.toDataObject());
		assertNull(bo.getGoogleUser());
	}

	@Test
	public void testOpenFromGaiaId_existingGaiaId() {
		Mockito.when(dao.getUserByGoogleUser(googleUser)).thenReturn(Pair.of(user, googleUser));
		bo.open(googleUser);
		assertEquals(user, bo.toDataObject());
		assertEquals(googleUser, bo.getGoogleUser());
	}

	@Test
	public void testOpen_existing() {
		bo.open(user);
		assertEquals(user, bo.toDataObject());
		assertNull(bo.getGoogleUser());
	}

	@Test
	public void testOpen_pair() {
		bo.open(Pair.of(user, googleUser));
		assertEquals(user, bo.toDataObject());
		assertEquals(googleUser, bo.getGoogleUser());
	}

	@Test
	public void testSave_notOpen() {
		try {
			bo.save();
			fail("Tried to save while not open");
		} catch (CPRuntimeException ex) {
			// Normal execution
		}
	}

	public void testSave() {
		bo.open(user);
		bo.save();
		Mockito.verify(dao).upsertUser(user, null);
	}
}

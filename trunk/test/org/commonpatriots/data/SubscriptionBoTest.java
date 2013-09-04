package org.commonpatriots.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.commonpatriots.CPRuntimeException;
import org.commonpatriots.proto.CPData.Subscription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.inject.util.Providers;

public class SubscriptionBoTest {
	private SubscriptionBo bo;
	@Mock private BaseDAO dao;

	private Subscription sub = Subscription.newBuilder().setId("TEST_ID").setServiceUnitId("TEST_SU_ID")
			.setActive(true).setNumFlags(1).build();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		bo = new SubscriptionBo(Providers.of(dao));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOpen() {
		bo.open();
		assertEquals(Subscription.newBuilder().build(), bo.toDataObject());
	}

	@Test
	public void testOpen_existingId() {
		final String id = "TEST_ID";
		Mockito.when(dao.getSubscriptionById(id)).thenReturn(sub);
		bo.open(id);
		assertEquals(sub, bo.toDataObject());
	}

	@Test
	public void testOpen_newId() {
		final String id = "TEST_ID";
		Mockito.when(dao.getSubscriptionById(id)).thenThrow(dao.new CPDAOException("Test"));
		bo.open(id);
		assertEquals(Subscription.newBuilder().setId(id).build(), bo.toDataObject());
	}

	@Test
	public void testOpen_existing() {
		bo.open(sub);
		assertEquals(sub, bo.toDataObject());
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
		bo.open(sub);
		bo.save();
		Mockito.verify(dao).upsertSubscription(sub);
	}
}

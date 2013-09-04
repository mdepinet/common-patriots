package org.commonpatriots.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.commonpatriots.CPRuntimeException;
import org.commonpatriots.proto.CPData.ContactInfo;
import org.commonpatriots.proto.CPData.ServiceUnit;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon;
import org.commonpatriots.proto.CPData.ServiceUnit.Polygon.Point;
import org.commonpatriots.proto.CPData.State;
import org.commonpatriots.util.CPUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.inject.util.Providers;

public class ServiceUnitBoTest {
	private ServiceUnitBo bo;
	@Mock private BaseDAO dao;
	@Mock private SubscriptionCollectionBo subColBo;

	private ServiceUnit unit = ServiceUnit.newBuilder().setName("TEST_NAME").setColor("#FFFFFF").setId("TEST_ID")
			.setContactInfo(ContactInfo.newBuilder().setEmail("test@blank.com").setPhone("555-555-5555")
					.setAddress("123 Main St").setCity("Nowheresville").setState(State.TX).setZip(11111))
			.addAllDistributionZones(CPUtil.newArrayList(Polygon.newBuilder().addAllPoints(
					CPUtil.newArrayList(Point.newBuilder().setLatitude(0).setLongitude(0).build(),
							Point.newBuilder().setLatitude(0).setLongitude(1).build(),
							Point.newBuilder().setLatitude(1).setLongitude(1).build(),
							Point.newBuilder().setLatitude(1).setLongitude(0).build())).build())).build();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		bo = new ServiceUnitBo(Providers.of(dao), Providers.of(subColBo));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOpen() {
		bo.open();
		assertEquals(ServiceUnit.newBuilder().build(), bo.toDataObject());
	}

	@Test
	public void testOpen_existingId() {
		final String id = "TEST_ID";
		Mockito.when(dao.getServiceUnitById(id)).thenReturn(unit);
		bo.open(id);
		assertEquals(unit, bo.toDataObject());
	}

	@Test
	public void testOpen_newId() {
		final String id = "TEST_ID";
		Mockito.when(dao.getServiceUnitById(id)).thenThrow(dao.new CPDAOException("Test"));
		bo.open(id);
		assertEquals(ServiceUnit.newBuilder().setId(id).build(), bo.toDataObject());
	}

	@Test
	public void testOpen_existing() {
		bo.open(unit);
		assertEquals(unit, bo.toDataObject());
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
		bo.open(unit);
		bo.save();
		Mockito.verify(dao).upsertServiceUnit(unit);
	}

	@Test
	public void testServesLocation() {
		bo.open(unit);
		assertTrue(bo.servesLocation(0.5, 0.5));
		assertTrue(bo.servesLocation(0.9, 0));
		assertTrue(bo.servesLocation(0, 0.9));
		assertTrue(bo.servesLocation(0.1, 0));
		assertTrue(bo.servesLocation(0, 0.1));
		assertTrue(bo.servesLocation(0.9, 0.9));
		assertTrue(bo.servesLocation(0.1, 0.1));
		assertFalse(bo.servesLocation(0,2));
		assertFalse(bo.servesLocation(2,0));
		assertFalse(bo.servesLocation(-1, 0));
		assertFalse(bo.servesLocation(0, -1));
		assertFalse(bo.servesLocation(0.5, 1.1));
		assertFalse(bo.servesLocation(0.5, -0.1));
		assertFalse(bo.servesLocation(1.1, 0.5));
		assertFalse(bo.servesLocation(-0.1, 0.5));
	}

}

package org.commonpatriots.mail;


import junit.framework.TestCase;

import org.commonpatriots.util.Pair;
import org.junit.Test;

public class CPMailerTest extends TestCase {
	@Test
	public void testGetUserFromAddAdminSubject() {
		String userEmail = "john.doe@gmail.com";
		String subject = String.format(CPMailer.ADD_ADMINISTRATOR_SUBJECT_FORMAT, userEmail);
		assertEquals(userEmail, CPMailer.getUserFromAddAdminSubject(subject));
		userEmail = "J0hn.do%e+1@custom.domain.eu";
		subject = "Re: " + String.format(CPMailer.ADD_ADMINISTRATOR_SUBJECT_FORMAT, userEmail);
		assertEquals(userEmail.toLowerCase(), CPMailer.getUserFromAddAdminSubject(subject));
	}

	@Test
	public void testGetUserFromAddServiceUnitCoordinatorSubject() {
		String userEmail = "john.doe@gmail.com";
		String suName = "Troop 001";
		String subject = String.format(CPMailer.ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT, userEmail, suName);
		assertEquals(Pair.of(userEmail, suName), CPMailer.getUserFromAddServiceUnitCoordinatorSubject(subject));
		userEmail = "J0hn.do%e+1@custom.domain.eu";
		suName = "Some0ddTr00p 15";
		subject = "Re: " + String.format(CPMailer.ADD_SERVICE_UNIT_COORDINATOR_SUBJECT_FORMAT, userEmail, suName);
		assertEquals(Pair.of(userEmail.toLowerCase(), suName),
				CPMailer.getUserFromAddServiceUnitCoordinatorSubject(subject));
	}

}

package com.tle.webtests.test.webservices.soap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.framework.soap.SoapInterfaceV2;
import com.tle.webtests.framework.soap.SoapService51;
import com.tle.webtests.test.AbstractTest;

@TestInstitution("vanilla")
public class SoapServicesTest extends AbstractTest
{
	private String ss_uuid;
	private SoapInterfaceV2 soapService;
	private SoapService51 soapService50;

	private static final String userUuid = "b09a4042-b091-87ed-eba9-6fb3c0fbe9a6";
	private static final String groupUuid = "ca306da2-8c44-6c71-36a9-b702aceec5c2";
	private static final String groupName = "Testing Soap Group";

	@Test
	public void localUserExists() throws Exception
	{
		assertFalse(soapService.userExists(ss_uuid, "none"));
		assertTrue(soapService.userExists(ss_uuid, userUuid));

		assertFalse(soapService50.userExists("none"));
		assertTrue(soapService50.userExists(userUuid));
	}

	@Test
	public void localUserNameExists() throws Exception
	{
		assertFalse(soapService.userNameExists(ss_uuid, "none"));
		assertTrue(soapService.userNameExists(ss_uuid, "AutoTest"));
		assertTrue(soapService.userNameExists(ss_uuid, "QuotaTest"));
		assertTrue(soapService.userNameExists(ss_uuid, "tokenuser"));

		assertFalse(soapService50.userNameExists("none"));
		assertTrue(soapService50.userNameExists("AutoTest"));
		assertTrue(soapService50.userNameExists("QuotaTest"));
		assertTrue(soapService50.userNameExists("tokenuser"));
	}

	@Test(dependsOnMethods = "addRemoveUsers")
	public void removeGroup() throws Exception
	{
		String groupId = soapService.getGroupUuidForName(ss_uuid, groupName);
		soapService.removeGroup(ss_uuid, groupId);
	}

	@Test
	public void addGroup() throws Exception
	{
		soapService.addGroup(ss_uuid, groupUuid, groupName);
		assertEquals(soapService.getGroupUuidForName(ss_uuid, groupName), groupUuid);
	}

	@Test(dependsOnMethods = "addGroup")
	public void addRemoveUsers() throws Exception
	{
		String groupId = soapService.getGroupUuidForName(ss_uuid, groupName);
		// Add users to this group using old soap service method
		soapService.addUserToGroup(ss_uuid, userUuid, groupId);
		soapService.removeAllUsersFromGroup(ss_uuid, groupId);

		groupId = soapService50.getGroupUuidForName(groupName);
		// Add users to this group using old soap service method
		soapService50.addUserToGroup(userUuid, groupId);
		soapService50.removeAllUsersFromGroup(groupId);
	}

	@BeforeClass
	public void setUp() throws Exception
	{
		SoapHelper soapHelper = new SoapHelper(context);
		soapService = soapHelper.createSoap(SoapInterfaceV2.class, "services/SoapInterfaceV2",
			"http://remoting.core.tle.com", null);
		ss_uuid = soapService.login("AutoTest", "automated");

		soapService50 = soapHelper.createSoap(SoapService51.class, "services/SoapService50",
			"http://soap.remoting.web.tle.com", soapService);
	}
}

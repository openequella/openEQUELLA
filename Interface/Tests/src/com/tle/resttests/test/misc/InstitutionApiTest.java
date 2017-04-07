package com.tle.resttests.test.misc;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.requests.InstitutionRequests;
import com.tle.resttests.AbstractEntityCreatorTest;

@SuppressWarnings("nls")
public class InstitutionApiTest extends AbstractEntityCreatorTest
{
	private InstitutionRequests institutions;
	private InstitutionRequests institutionsNoAuth;

	private String instId;
	private static final String INST_NAME = "institutionapitest";

	@Override
	public void customisePageContext()
	{
		super.customisePageContext();
		institutions = builder().institutions();
		institutionsNoAuth = builder().institutions(this);
	}

	@Override
	public boolean isInstitutional()
	{
		return false;
	}

	@Test
	public void createTest()
	{
		ObjectNode wrong = institutions.jsonAppendBaseUrl(INST_NAME, null, INST_NAME, INST_NAME, true);
		assertValidationError(wrong, "Password must not be left blank");
		wrong.put("password", context.getTestConfig().getAdminPassword());
		wrong.remove("name");
		assertValidationError(wrong, "Institution name must not be left blank");
		wrong.put("name", "AutoTest");
		assertValidationError(wrong, "Institution name 'AutoTest' is already in use by another institution");
		wrong.put("name", INST_NAME);
		wrong.put("filestoreId", "autotest");
		assertValidationError(wrong, "Filestore ID 'autotest' is already in use by another institution");
		wrong.put("filestoreId", INST_NAME);

		institutionsNoAuth.createFail(institutionsNoAuth.accessDeniedRequest(), wrong);
		ObjectNode inst = institutions.create(wrong);
		instId = institutions.getId(inst);
	}

	private void assertValidationError(ObjectNode wrong, String desc)
	{
		ObjectNode response = institutions.createWithError(wrong);
		Assert.assertEquals(response.get("error_description").asText(), "Validation errors: " + desc);
	}

	@Test(dependsOnMethods = "createTest")
	public void listTest()
	{
		ArrayNode all = institutions.list();

		boolean scratchyFound = false;
		boolean newFound = false;

		for( JsonNode inst : all )
		{
			if( "AutoTest".equals(inst.get("name").asText()) )
			{
				scratchyFound = true;
				Assert.assertEquals(inst.get("filestoreId").asText(), "autotest");
				Assert.assertTrue(inst.get("enabled").asBoolean());
				Assert.assertTrue(inst.get("url").asText().endsWith("/autotest/"));
				Assert.assertNull(inst.get("password"));
			}
			else if( INST_NAME.equals(inst.get("name").asText()) )
			{
				newFound = true;
				Assert.assertEquals(inst.get("filestoreId").asText(), INST_NAME);
				Assert.assertTrue(inst.get("enabled").asBoolean());
				Assert.assertTrue(inst.get("url").asText().endsWith("/" + INST_NAME + "/"));
				Assert.assertNull(inst.get("password"));
			}
			if( scratchyFound && newFound )
			{
				break;
			}
		}
		Assert.assertTrue(scratchyFound, "Scratchy not found");
		Assert.assertTrue(newFound, "New institution not found");
	}

	@Test(dependsOnMethods = "createTest")
	public void getTest()
	{
		ObjectNode inst = institutions.get(instId);
		Assert.assertEquals(inst.get("filestoreId").asText(), INST_NAME);
		Assert.assertTrue(inst.get("enabled").asBoolean());
		Assert.assertTrue(inst.get("url").asText().endsWith("/" + INST_NAME + "/"));
		Assert.assertNull(inst.get("password"));
		institutionsNoAuth.get(instId);
	}

	@Test(dependsOnMethods = {"getTest", "listTest"})
	public void editTest()
	{
		ObjectNode inst = institutions.jsonAppendBaseUrl("new name", "newpass", "newfsid", "newurl", false);
		inst.put("uniqueId", instId);
		institutionsNoAuth.editNoPermission(inst);
		institutions.editId(inst);

		inst = institutions.get(instId);
		Assert.assertEquals(inst.get("name").asText(), "new name");
		Assert.assertEquals(inst.get("filestoreId").asText(), "newfsid");
		Assert.assertFalse(inst.get("enabled").asBoolean());
		Assert.assertTrue(inst.get("url").asText().endsWith("/newurl/"));
	}

	@Override
	public String getDefaultUser()
	{
		// Guest
		return null;
	}
}

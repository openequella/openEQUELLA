package com.tle.resttests.test.acl;

import java.net.URI;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.Users;
import com.tle.json.requests.InstitutionRequests;
import com.tle.json.requests.UserRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.OAuthTokenCache;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.UserRequestsBuilder;

@SuppressWarnings("nls")
public class GlobalAclsTest extends AbstractRestAssuredTest
{
	private InstitutionRequests institutions;

	/*
	private CollectionRequests collections;
	private SchemaRequests schemas;
	private ItemRequests items;
	private AclRequests acls;
	private ObjectNode itemJson;
	private SearchRequests searches;
	private ItemId itemId;
	private String itemName;
	private String collectionUuid;
	 */

	@Override
	protected boolean isInstitutional()
	{
		return false;
	}

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		institutions = builder().institutions();

		ObjectNode byUrl = institutions.getByUrl("acls");
		if( byUrl != null )
		{
			institutions.cleanupDelete(byUrl.get("uniqueId").asText());
		}
	}

	@Test
	public void setup()
	{
		String name = context.getFullName("ACL inst");
		ObjectNode created = institutions.create(institutions.jsonAppendBaseUrl(name, "tle010", "blah", "acls", true));
		RequestsBuilder builder = new RequestsBuilder(this, OAuthTokenCache.ADMIN_TOKEN, URI.create(created.get("url")
			.asText()));
		//acls = builder.acls();
		UserRequestsBuilder userRequests = new UserRequestsBuilder(builder);
		UserRequests users = userRequests.users();
		users.createId(Users.json(RestTestConstants.USERID_AUTOTEST, "AutoTest", "Auto", "Test", "auto@test.com",
			"automated"));
		//OAuthTokenCache tokenCache = builder.tokenCache();
		//RequestsBuilder autoTestBuilder = builder.user(tokenCache.getProvider(RestTestConstants.USERID_AUTOTEST));

		//collections = autoTestBuilder.collections();
		//schemas = autoTestBuilder.schemas();
		//items = autoTestBuilder.items();
		//searches = autoTestBuilder.searches();
	}

	/* Equella doesn't handle metadata privs at the moment
	 * 
	 * 
	@Test(dependsOnMethods = "setup", groups = "eps")
	public void globalPrivs()
	{
		ObjectNode schemaJson = Schemas.basicJson(context.getFullName("ACL Schema"));
		schemas.createFail(schemas.accessDeniedRequest(), schemaJson);
		ObjectNode aclList = addGlobalRule(null, autoTestGrant("CREATE_SCHEMA"));
		aclList = addGlobalRule(aclList, autoTestGrant("CREATE_COLLECTION"));
		aclList = addGlobalRule(aclList, autoTestGrant("DELETE_COLLECTION"));
		aclList = addGlobalRule(aclList, autoTestGrant("DELETE_SCHEMA"));
		aclList = addGlobalRule(aclList, autoTestGrant("VIEW_SCHEMA"));
		aclList = addGlobalRule(aclList, autoTestGrant("VIEW_COLLECTION"));
		acls.edit(aclList);

		final ObjectNode schemaJsonInp = schemaJson;
		schemaJson = schemas.untilSuccess(new Callable<ObjectNode>()
		{
			@Override
			public ObjectNode call() throws Exception
			{
				return schemas.create(schemaJsonInp);
			}
		});
		String schemaUuid = schemas.getId(schemaJson);
		ObjectNode collectionJson = collections.create(CollectionJson.json(context.getFullName("ACL Collection"),
			schemaUuid, null));

		collectionUuid = collections.getId(collectionJson);
		itemJson = Items.json(collectionUuid, "item/name", context.getFullName("ACL Item"));
		items.createFail(items.accessDeniedRequest(), itemJson);

		collections.listAclsFail(collections.accessDeniedRequest());

		schemaJson.put("name", context.getFullName("Edited ACL Schema"));
		schemas.editNoPermission(schemaJson);

		aclList = addGlobalRule(aclList, autoTestGrant("EDIT_SECURITY_TREE"));
		acls.edit(aclList);
		ObjectNode schemaAcls = schemas.listAcls();
		addAllRule(schemaAcls, autoTestGrant("EDIT_SCHEMA"));
		schemas.editAcls(schemaAcls);
		schemas.editId(schemaJson);
		ObjectNode collectionAcls = collections.listAcls();
		addAllRule(collectionAcls, autoTestGrant("EDIT_COLLECTION"));
		addAllRule(collectionAcls, autoTestGrant("CREATE_ITEM"));
		addAllRule(collectionAcls, autoTestGrant("DELETE_ITEM"));
		addAllRule(collectionAcls, autoTestGrant("VIEW_ITEM"));
		collections.editAcls(collectionAcls);
		itemJson = items.create(itemJson);
	}

	private ObjectNode autoTestGrant(String priv)
	{
		return autoTestRule(priv, true, false);
	}

	private ObjectNode autoTestRule(String priv, boolean grant, boolean override)
	{
		return AclLists.userRule(priv, grant, override, RestTestConstants.USERID_AUTOTEST);
	}
	
	@Test(dependsOnMethods = "globalPrivs", groups = "eps")
	public void globalStatusPrivs()
	{
		itemName = context.getFullName("New name");
		Items.editMetadata(itemJson, "item/name", itemName);
		items.editNoPermission(itemJson);
		ObjectNode collectionAcls = collections.listAcls();
		addStatusRule(collectionAcls, "live", autoTestGrant("EDIT_ITEM"));
		collections.editAcls(collectionAcls);
		itemId = items.editId(itemJson);
	}

	@Test(dependsOnMethods = "globalStatusPrivs", groups = "eps")
	public void reindexTest()
	{
		RequestSpecification request = searches.searchRequest('"' + itemName + '"', null);
		searches.waitUntilIgnoreError(request, SearchRequests.resultUnavailable(itemId));
		ObjectNode aclList = addGlobalRule(acls.list(), autoTestGrant("DISCOVER_ITEM"));
		acls.edit(aclList);
		searches.waitUntil(request, SearchRequests.resultAvailable(itemId));
		ObjectNode collectionAcls = collections.listAcls();
		addStatusRule(collectionAcls, "live", autoTestRule("DISCOVER_ITEM", false, false));
		collections.editAcls(collectionAcls);
		searches.waitUntil(request, SearchRequests.resultUnavailable(itemId));
		addAllRule(collectionAcls, autoTestGrant("DISCOVER_ITEM"));
		collections.editAcls(collectionAcls);
		searches.waitUntil(request, SearchRequests.resultAvailable(itemId));
		ObjectNode collectionJson = collections.get(collectionUuid);
		ObjectNode collectionSecurity = collectionJson.with("security");
		addAllRule(collectionSecurity, autoTestRule("DISCOVER_ITEM", false, false));
		collections.editId(collectionJson);
		searches.waitUntil(request, SearchRequests.resultUnavailable(itemId));
		addStatusRule(collectionSecurity, "live", autoTestGrant("DISCOVER_ITEM"));
		collections.editId(collectionJson);
		searches.waitUntil(request, SearchRequests.resultAvailable(itemId));
	}

	@Test(dependsOnMethods = "reindexTest", groups = "eps")
	public void listAndViewEntity()
	{
		String schemaId = schemas.getId(schemas.create(Schemas.basicJson("listViewEntity")));
		String collectionId = collections.getId(collections.create(CollectionJson.json("", schemaId, null)));

		schemas.listFail(schemas.accessDeniedRequest());
		collections.listFail(collections.accessDeniedRequest());

		ObjectNode aclList = addGlobalRule(null, autoTestGrant("LIST_COLLECTION"));
		// Resets everything
		aclList = addGlobalRule(aclList, autoTestGrant("LIST_SCHEMA"));
		acls.edit(aclList);

		Assert.assertTrue(schemas.list().get("results").get(0).get("definition") == null,
			"Shouldn't have permissions to see the definition");
		Assert.assertTrue(collections.list().get("results").get(0).get("schema") == null,
			"Shouldn't have permissions to see the schema");
		Assert.assertTrue(schemas.get(schemaId).get("definition") == null,
			"Shouldn't have permissions to see the definition");
		Assert.assertTrue(collections.get(collectionId).get("schema") == null,
			"Shouldn't have permissions to see the schema");

		aclList = addGlobalRule(aclList, autoTestGrant("VIEW_COLLECTION"));
		aclList = addGlobalRule(aclList, autoTestGrant("VIEW_SCHEMA"));
		acls.edit(aclList);

		Assert.assertTrue(schemas.list().get("results").get(0).get("definition") != null,
			"Should have permissions to see the definition");
		Assert.assertTrue(collections.list().get("results").get(0).get("schema").get("uuid").asText() != null,
			"Should have permissions to see the schema");
		Assert.assertTrue(schemas.get(schemaId).get("definition") != null,
			"Should have permissions to see the definition");
		Assert.assertTrue(collections.get(collectionId).get("schema").get("uuid").asText() != null,
			"Should have permissions to see the schema");
	}
	*/
}

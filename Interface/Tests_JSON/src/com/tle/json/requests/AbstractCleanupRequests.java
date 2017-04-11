package com.tle.json.requests;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import org.hamcrest.Matchers;
import org.testng.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.CleanupAfter;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;

public abstract class AbstractCleanupRequests<ID> extends AuthorizedRequests
{
	private CleanupController cleanUpController;

	public AbstractCleanupRequests(URI uri, TokenProvider tokens, ObjectMapper mapper, PageContext pageContext,
		CleanupController cleanUpController, TestConfig testConfig)
	{
		super(uri, tokens, mapper, pageContext, testConfig);
		this.cleanUpController = cleanUpController;
	}

	public ObjectNode create(ObjectNode object)
	{
		return create(createRequest(object));
	}

	public Response createFail(RequestSpecification request)
	{
		return request.post(getResolvedPath());
	}

	public Response createFail(RequestSpecification request, ObjectNode object)
	{
		return jsonBody(request, object).post(getResolvedPath());
	}

	public RequestSpecification importRequest(ObjectNode object)
	{
		return createRequest(object).queryParam("import", true);
	}

	public Response importer(ObjectNode object)
	{
		return importRequest(object).post(getResolvedPath());
	}

	public Response importer(RequestSpecification req)
	{
		return req.post(getResolvedPath());
	}

	public ObjectNode create(RequestSpecification request)
	{
		final String resolvedPath = getResolvedPath();
		Response response = request.post(resolvedPath);
		String location = response.getHeader("Location");
		Assert.assertNotNull(location, "Location on create request to " + resolvedPath + " is null");
		ObjectNode object = object(successfulRequest().get(location));
		final ID id = getId(object);
		cleanUpController.addCleanup(new StandardCleanupAfter(id));
		return object;
	}

	protected RequestSpecification createRequest(ObjectNode object)
	{
		return jsonBody(createRequest(), object);
	}

	public ID createId(ObjectNode object)
	{
		return getId(create(object));
	}

	public ID editId(ObjectNode object)
	{
		ID id = getId(object);
		RequestSpecification request = editRequest(successfulRequest(), object);
		editResponse(request, id);
		return id;
	}

	public Response editResponse(RequestSpecification request, ID id)
	{
		return request.put(getResolvedEntityUri(), getIdParams(id));
	}

	public ID editId(ObjectNode object, String param, Object value, Object... otherParams)
	{
		ID id = getId(object);
		RequestSpecification request = editRequest(successfulRequest(), object);
		request.queryParameters(param, value, otherParams);
		editResponse(request, id);
		return id;
	}

	public Response editNoPermission(ObjectNode object)
	{
		ID id = getId(object);
		return editNoPermissionRequest(object).put(getResolvedEntityUri(), getIdParams(id));
	}

	public Response editResponse(ObjectNode object, ID id)
	{
		return editRequest(successfulRequest(), object).put(getResolvedEntityUri(), getIdParams(id));
	}

	private RequestSpecification editNoPermissionRequest(ObjectNode object)
	{
		return jsonBody(accessDeniedRequest(), object);
	}

	public RequestSpecification editRequest(ObjectNode object)
	{
		return jsonBody(successfulRequest().header("Location", Matchers.notNullValue()).with(), object);
	}

	public RequestSpecification editRequest(RequestSpecification request, ObjectNode object)
	{
		return jsonBody(request, object);
	}

	protected String getResolvedEntityUri()
	{
		return getResolvedPath() + getIdPathString();
	}

	protected String getIdPathString()
	{
		return "/{id}";
	}

	public Response getResponse(RequestSpecification request, ID id)
	{
		return request.get(getResolvedEntityUri(), getIdParams(id));
	}

	public ObjectNode get(RequestSpecification request, ID id)
	{
		return object(getResponse(request, id));
	}

	public ObjectNode get(ID id)
	{
		return get(successfulRequest(), id);
	}

	public void delete(ID id)
	{
		deleteRequest(true).delete(getResolvedEntityUri(), getIdParams(id));
	}

	public void delete(RequestSpecification request, ID id)
	{
		request.delete(getResolvedEntityUri(), getIdParams(id));
	}

	protected RequestSpecification deleteRequest(boolean requireSuccess)
	{
		if( requireSuccess )
		{
			return successfulDelete();
		}
		return auth().expect().with();
	}

	protected Object[] getIdParams(ID id)
	{
		return new Object[]{id};
	}

	public abstract ID getId(ObjectNode entity);

	public void cleanupDelete(ID id)
	{
		Collection<Integer> acceptableCleanupDeletes = new HashSet<Integer>();
		acceptableCleanupDeletes.add(204);
		acceptableCleanupDeletes.add(404);
		RequestSpecification deleteRequest = deleteRequest(false).expect()
			.statusCode(Matchers.<Integer> isIn(acceptableCleanupDeletes)).with();
		try
		{
			delete(deleteRequest, id);
		}
		catch( AssertionError assertion )
		{
			throw new AssertionError("Error cleaning up after " + getResolvedPath(), assertion);
		}
	}

	protected class StandardCleanupAfter implements CleanupAfter
	{
		private final ID id;

		public StandardCleanupAfter(ID id)
		{
			this.id = id;
		}

		@Override
		public void cleanUp()
		{
			cleanupDelete(id);
		}
	}
}

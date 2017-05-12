package com.tle.resttests.setup;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.tle.json.framework.AdminTokenProvider;
import com.tle.json.framework.CleanupAfter;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.requests.AclRequests;
import com.tle.json.requests.BaseEntityRequests;
import com.tle.json.requests.CollectionRequests;
import com.tle.json.requests.GroupRequests;
import com.tle.json.requests.InstitutionRequests;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.OAuthRequests;
import com.tle.json.requests.RoleRequests;
import com.tle.json.requests.SchemaRequests;
import com.tle.json.requests.SearchRequests;
import com.tle.json.requests.StagingRequests;
import com.tle.json.requests.UserRequests;
import com.tle.json.requests.WorkflowRequests;

public abstract class AbstractImportExport implements CleanupController
{
	private static final ObjectMapper createMapper()
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper;
	}

	protected static final ObjectMapper MAPPER = createMapper();

	protected final TestConfig testConfig;
	protected final PageContext pageContext;
	protected final String password;
	protected final Map<String, BaseEntityRequests> entityRequests;
	protected final SearchRequests searchRequests;

	protected final ItemRequests itemRequests;
	protected final UserRequests userRequests;
	protected final GroupRequests groupRequests;
	protected final RoleRequests roleRequests;
	protected final AclRequests aclRequests;
	protected final File baseDir;
	protected final InstitutionRequests instRequests;
	protected final String shortName;
	protected final String fullName;
	protected final StagingRequests stagingRequests;

	public AbstractImportExport(TestConfig testConfig, String fullName, String shortName, String password, File baseDir)
	{
		this(testConfig, fullName, shortName, password, password, baseDir, testConfig
			.getInstitutionUrlFromShortName(shortName));
	}

	public AbstractImportExport(TestConfig testConfig, String fullName, String shortName, String password,
		String adminPassword, File baseDir, String fullUrl)
	{
		this.testConfig = testConfig;
		this.baseDir = baseDir;
		this.shortName = shortName;
		this.fullName = fullName;
		pageContext = new PageContext(testConfig, fullUrl);
		this.password = password;
		URI baseURI = pageContext.getBaseURI();
		AdminTokenProvider token = new AdminTokenProvider(password);
		Builder<String, BaseEntityRequests> entityTypesBuilder = ImmutableMap.builder();
		// @formatter:off
		entityTypesBuilder.putAll(ImmutableMap.of(
			"schema", new SchemaRequests(baseURI, token, MAPPER, pageContext, this, testConfig),
			"collection", new CollectionRequests(baseURI, token, MAPPER, pageContext, this, testConfig),
			"oauth", new OAuthRequests(baseURI, token, MAPPER, pageContext, this, testConfig),
			"workflow", new WorkflowRequests(baseURI, token, MAPPER, pageContext, this, testConfig)));
		// @formatter:on

		entityRequests = entityTypesBuilder.build();
		searchRequests = new SearchRequests(baseURI, token, MAPPER, pageContext, testConfig);
		itemRequests = new ItemRequests(baseURI, token, MAPPER, pageContext, this, testConfig);
		userRequests = new UserRequests(baseURI, token, MAPPER, pageContext, this, testConfig);
		groupRequests = new GroupRequests(baseURI, token, MAPPER, pageContext, this, testConfig);
		roleRequests = new RoleRequests(baseURI, token, MAPPER, pageContext, this, testConfig);
		aclRequests = new AclRequests(baseURI, token, MAPPER, pageContext, testConfig);
		stagingRequests = new StagingRequests(baseURI, token, MAPPER, pageContext, testConfig);
		URI adminUri = URI.create(testConfig.getAdminUrl());
		instRequests = new InstitutionRequests(adminUri, MAPPER, pageContext, this, testConfig, adminPassword);
		String proxyHost = testConfig.getProperty("proxy.host");
		String proxyPortString = testConfig.getProperty("proxy.port");
		if( proxyPortString != null )
		{
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", proxyPortString);
		}
	}

	@Override
	public void addCleanup(CleanupAfter cleanup)
	{
		// no cleanup required
	}

	public File getInstitutionAclFile(File aclDir)
	{
		return new File(aclDir, "institution.json");
	}

	public File getAclDir()
	{
		return new File(baseDir, "acl");
	}

	protected ObjectNode readJson(File file)
	{
		try
		{
			return (ObjectNode) MAPPER.readValue(file, JsonNode.class);
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	protected void writeJson(ObjectNode entity, File file) throws IOException
	{
		MAPPER.writeValue(file, entity);
	}

	public File[] getJsonFiles(File dir)
	{
		return dir.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				return file.isFile() && file.getName().endsWith(".json");
			}
		});
	}

	public File getEntityAclsFile(File entityDir)
	{
		File aclFile = new File(entityDir, "acls/all.json");
		aclFile.getParentFile().mkdirs();
		return aclFile;
	}

	public File getUserDir()
	{
		return new File(baseDir, "localuser");
	}

	public File getGroupDir()
	{
		return new File(baseDir, "localgroup");
	}

	public File getRoleDir()
	{
		return new File(baseDir, "localrole");
	}

	public File getEntityDir(String key)
	{
		return new File(baseDir, key);
	}

	public File getItemDir()
	{
		return new File(baseDir, "item");
	}

	protected File getFilesDir(File file)
	{
		String name = file.getName();
		int indexOf = name.indexOf(".json");
		if( indexOf >= 0 )
		{
			String filesName = name.substring(0, indexOf);
			File filesDir = new File(file.getParentFile(), filesName);
			if( filesDir.isDirectory() )
			{
				return filesDir;
			}
		}
		return null;
	}

	public Set<String> getEntityTypes()
	{
		return entityRequests.keySet();
	}

}

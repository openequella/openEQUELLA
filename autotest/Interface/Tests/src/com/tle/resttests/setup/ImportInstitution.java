package com.tle.resttests.setup;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.Institutions;
import com.tle.json.framework.TestConfig;
import com.tle.json.requests.BaseEntityRequests;
import com.tle.json.requests.SearchRequests;

public class ImportInstitution extends AbstractImportExport
{
	public ImportInstitution(String fullName, String shortName, String password, File baseDir)
	{
		super(new TestConfig(ImportInstitution.class, true), fullName, shortName, password, baseDir);
	}

	public ImportInstitution(TestConfig testConfig, String fullName, String shortName, String password,
		String adminPassword, File baseDir, String fullUrl)
	{
		super(testConfig, fullName, shortName, password, adminPassword, baseDir, fullUrl);
	}

	public static void main(String[] args) throws Exception
	{
		ImportInstitution inst = new ImportInstitution(args[1], args[2], args[3], new File(args[0]));
		boolean deleteFirst = true;
		if( args.length > 4 )
		{
			deleteFirst = Boolean.parseBoolean(args[4]);
		}
		if( deleteFirst )
		{
			inst.deleteIfExists();
			inst.createInstitution();
		}
		else if( inst.existingId() == null )
		{
			inst.createInstitution();
		}
		inst.importAll();
	}

	public void createInstitution()
	{
		ObjectNode instObj = Institutions.json(fullName, password, shortName,
			testConfig.getInstitutionUrlFromShortName(shortName), true);
		instRequests.create(instObj);
	}

	public String existingId()
	{
		ArrayNode insts = instRequests.list();
		String instId = null;
		for( JsonNode institution : insts )
		{
			if( institution.get("filestoreId").asText().equalsIgnoreCase(shortName) )
			{
				instId = institution.get("uniqueId").asText();
				break;
			}
		}
		return instId;
	}

	public void deleteIfExists()
	{
		String instId = existingId();
		if( instId != null )
		{
			instRequests.delete(instId);
		}
	}

	public void importAll() throws IOException
	{
		importAcls();
		importUsers();
		importRoles();
		importGroups();
		importAllEntities();
		importItems();
	}

	public void importAllEntities()
	{
		for( Entry<String, BaseEntityRequests> entry : entityRequests.entrySet() )
		{
			importEntities(entry.getKey());
		}
	}

	private void importEntities(String key)
	{
		File entityDir = getEntityDir(key);
		File[] entities = getJsonFiles(entityDir);
		for( File file : entities )
		{
			importEntity(key, file);
		}
		importEntityAcls(key);
	}

	public void importEntityAcls(String type)
	{
		BaseEntityRequests requests = entityRequests.get(type);
		requests.editAcls(readJson(getEntityAclsFile(getEntityDir(type))));
	}

	public void importEntity(String type, File file)
	{
		BaseEntityRequests requests = entityRequests.get(type);
		ObjectNode entity = readJson(file);
		requests.importEntity(entity);
	}

	public void importUsers()
	{
		File[] userFiles = getJsonFiles(getUserDir());
		for( File user : userFiles )
		{
			importUser(user);
		}

	}

	public void importUser(File user)
	{
		userRequests.importer(readJson(user));
	}

	public void importGroups()
	{
		File[] userFiles = getJsonFiles(getGroupDir());
		for( File group : userFiles )
		{
			importGroup(group);
		}
	}

	public void importGroup(File group)
	{
		groupRequests.importer(readJson(group));
	}

	public void importRoles()
	{
		File[] roleFiles = getJsonFiles(getRoleDir());
		for( File role : roleFiles )
		{
			importRole(role);
		}
	}

	public void importRole(File role)
	{
		roleRequests.importer(readJson(role));
	}

	public void importAcls()
	{
		ObjectNode acls = readJson(getInstitutionAclFile(getAclDir()));
		aclRequests.edit(acls);
	}

	public void importItems() throws IOException
	{
		File itemDir = getItemDir();
		File[] files = getJsonFiles(itemDir);
		for( File file : files )
		{
			importItem(file);
		}
	}

	public void importItem(File file) throws IOException
	{
		ObjectNode itemJson = readJson(file);
		String stagingId = importItemFiles(file);
		itemRequests.importer(itemJson, stagingId);
	}

	protected String importItemFiles(File file) throws IOException
	{
		File filesDir = getFilesDir(file);
		String stagingId = null;
		if( filesDir != null )
		{
			stagingId = stagingRequests.createId();
			String stagingUrl = stagingRequests.toStagingUrl(stagingId);
			uploadContents(filesDir, stagingUrl, "");
		}
		return stagingId;
	}

	protected void uploadContents(File folder, String stagingUrl, String parent) throws IOException
	{
		for( File file : folder.listFiles() )
		{
			if( file.isFile() )
			{
				stagingRequests.putFile(stagingUrl, parent + file.getName(), file);
			}
			else
			{
				uploadContents(file, stagingUrl, parent + file.getName() + "/");
			}
		}
	}

	public int getItemCount()
	{
		return getJsonFiles(getItemDir()).length;
	}

	public SearchRequests searches()
	{
		return searchRequests;
	}
}

package com.tle.webtests.test.adminconsole;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collection;

import org.fest.swing.annotation.GUITest;
import org.fest.swing.security.NoExitSecurityManager;
import org.fest.swing.testng.listener.ScreenshotOnFailureListener;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;
import com.tle.client.harness.HarnessConfig;
import com.tle.client.harness.ServerProfile;
import com.tle.webtests.pageobject.adminconsole.AdminConsoleWindow;
import com.tle.webtests.pageobject.adminconsole.collection.CollectionDetailsTab;
import com.tle.webtests.pageobject.adminconsole.remoterepo.RemoteRepoDetailsTab;
import com.tle.webtests.pageobject.adminconsole.schema.SchemaDetailsTab;
import com.tle.webtests.pageobject.adminconsole.schema.SchemaEditorTab;
import com.tle.webtests.test.AbstractTest;

@GUITest
@Listeners({ScreenshotOnFailureListener.class})
public class AdminConsoleTest extends AbstractTest
{
	private static final String INSTITUTION_FILE = "institution.tar.gz";
	private static final String SERVER_XML = "server.xml"; //$NON-NLS-1$
	private AdminConsoleWindow admin;

	@Test
	public void replaceRemoteRepoUrl() throws Exception
	{
		RemoteRepoDetailsTab repoDetailsTab = admin.editRemoteRepo("fiveo");
		repoDetailsTab.setUrl(context.getBaseUrl() + "fiveo/");
		repoDetailsTab.save();
	}

	@Test
	public void schemaTest()
	{
		SchemaDetailsTab detailsTab = admin.editSchema("Basic Schema");
		detailsTab.close();

		detailsTab = admin.addSchema();
		detailsTab.setName("A name");
		detailsTab.setDescription("A description");

		SchemaEditorTab editorTab = detailsTab.editor();
		editorTab.addChild("xml", "itembody");
		editorTab.addChild("xml/itembody", "name", true, true);
		editorTab.addChild("xml/itembody", "description", true, false);
		editorTab.addChild("xml/itembody", "attachment");

		detailsTab = editorTab.details();
		detailsTab.setNameXml("xml/itembody/name");
		detailsTab.setDescriptionXml("xml/itembody/description");

		detailsTab.save();

		admin.deleteSchema("A name");
	}

	@Test
	public void collectionTest()
	{
		CollectionDetailsTab detailsTab = admin.addCollection();
		detailsTab.setName("A Collection");
		detailsTab.setDescription("A Description");
		detailsTab.setMetadata("Basic Schema");
		detailsTab.save();
		admin.deleteCollection("A Collection");
	}

	@Test
	public void toDo()
	{
		admin.toDo();
	}

	@Override
	protected boolean isInstitutional()
	{
		return false;
	}

	@Override
	protected void customisePageContext()
	{
		HarnessConfig config = new HarnessConfig();
		Collection<ServerProfile> servers = config.getServers();
		File[] institutions = new File(testConfig.getTestFolder(), "tests").listFiles();
		for( File instDir : institutions )
		{
			if( new File(instDir, INSTITUTION_FILE).exists() )
			{
				ServerProfile serverProfile = new ServerProfile();
				String shortName = instDir.getName();
				serverProfile.setName(shortName);
				serverProfile.setUsername("TLE_ADMINISTRATOR");
				serverProfile.setPassword(testConfig.getAdminPassword());
				serverProfile.setServer(context.getBaseUrl() + shortName + '/');
				servers.add(serverProfile);
			}
		}

		File newFile = new File(SERVER_XML);
		if( newFile.exists() && !newFile.delete() )
		{
			throw new Error("Could not delete existing server.xml");
		}

		// Write the configuration back to the file.
		try
		{
			Writer out = new BufferedWriter(new FileWriter(newFile));
			new XStream().toXML(config, out);
		}
		catch( Exception e )
		{
			throw new Error(e);
		}

		System.setSecurityManager(new NoExitSecurityManager());

		admin = new AdminConsoleWindow(context);
		admin.launch("contribute");
	}

	@Override
	@AfterClass(alwaysRun = true)
	public void finishedClass(ITestContext testContext) throws Exception
	{
		System.setSecurityManager(null);
		super.finishedClass(testContext);
	}
}

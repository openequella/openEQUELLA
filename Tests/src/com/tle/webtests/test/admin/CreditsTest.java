package com.tle.webtests.test.admin;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.LicencesPage;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("fiveo")
public class CreditsTest extends AbstractCleanupTest
{
	// it was a slow week when this list was created
	private static final String[] licenses = {"Aetrion LLC", "Aha-Soft", "Android Icons", "Antlr",
			"Apache Commons (various)", "Apache HttpClient", "Apache Lucene", "Apache PDF Box", "Apache POI",
			"Apache Tika", "Apache XML-RPC", "Bootstrap", "CONCISE", "CQL-Java", "Curator", "Eclipse BIRT", "EZMorph",
			"Farm-fresh Web Icons", "Flamingo Swing", "FreeHEP", "Freemarker", "Google Guava libraries",
			"Google Guice", "Hibernate", "HikariCP", "iTunesU API", "Jackson", "Jafer Z39.50 Client",
			"Java Plug-in Framework", "JBoss JTA", "JDom", "JQTI+", "jQuery", "JSON-Lib", "JSoup", "Log4j", "MathJax",
			"METS Java Toolkit", "Microsoft SQL Server JDBC Driver", "MigLayout", "Mozilla Rhino", "OAICat",
			"Oracle JDBC Driver", "PostgreSQL JDBC Driver", "RestEasy", "Rome", "SLF4J", "Spring", "SRU", "SRW",
			"Swagger", "SWFObject", "TagSoup", "Text Mining", "TinyMCE", "Tomcat", "Video.js", "W3C Tidy", "Xstream",
			"ZooKeeper"};

	@Test
	public void testPageExists()
	{
		LicencesPage credits = new LicencesPage(context).load();
		for( String lic : licenses )
		{
			Assert.assertTrue(credits.doesLicenceExists(lic), "Licence for " + lic + " wasn't found");
		}

	}

	@Test
	public void testLinkAndLicenceCount()
	{
		HomePage home = logon("autotest", "automated");
		LicencesPage credits = home.clickCreditsLink();
		Assert.assertEquals(credits.getLicenceCount(), licenses.length, "Licence(s) added or removed");
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		// do nothing
	}
}

package com.tle.webtests.test;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.tle.webtests.pageobject.institution.*;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.tle.common.Check;
import com.tle.webtests.pageobject.UndeterminedPage;

public class SyncTest extends AbstractTest
{
	private static final String INSTITUTION_FILE = "institution";

	@Override
	protected boolean isInstitutional()
	{
		return false;
	}

	@DataProvider(name = "institutes")
	public Object[][] listInstitutes(ITestContext context) throws Exception
	{
		Set<String> includedInsts = null;
		String included = testConfig.getProperty("synctest.included");
		if( included != null )
		{
			includedInsts = Sets.newHashSet(Arrays.asList(included.split(",")));
		}
		else
		{
			String paramIncluded = context.getCurrentXmlTest().getParameter("institutions");
			if( !Check.isEmpty(paramIncluded) )
			{
				includedInsts = Sets.newHashSet(Arrays.asList(paramIncluded.split(",")));
			}
		}

		File testsFolder = new File(testConfig.getTestFolder(), "tests");
		File[] institutions = testsFolder.listFiles();
		List<Object[]> instDirs = new ArrayList<Object[]>();
		for( File instDir : institutions )
		{
			if( new File(instDir, INSTITUTION_FILE).isDirectory()
				&& (includedInsts == null || includedInsts.contains(instDir.getName())) )
			{
				instDirs.add(new Object[]{instDir});
			}
		}
		return instDirs.toArray(new Object[instDirs.size()][]);
	}

	@Override
	protected void prepareBrowserSession() {
		UndeterminedPage<InstitutionTabInterface> choice = new UndeterminedPage<InstitutionTabInterface>(context,
				new InstitutionListTab(context), new ImportTab(context));
		new ServerAdminLogonPage(context).load().logon(testConfig.getAdminPassword(), choice);
	}

	@Test(dataProvider = "institutes")
	public void syncInstitution(File instFolder) throws Exception
	{
		String shortName = instFolder.getName();

		String instutionUrl = testConfig.getInstitutionUrl(shortName);

		InstitutionListTab listTab = new InstitutionListTab(context);
		ImportTab importTab = new ImportTab(context);
		UndeterminedPage<InstitutionTabInterface> choice = new UndeterminedPage<InstitutionTabInterface>(context,
			listTab, importTab);
		InstitutionTabInterface currentTab = choice.load();
		if( currentTab == listTab )
		{
			if( listTab.institutionExists(instutionUrl) )
			{
				StatusPage<InstitutionTabInterface> statusPage = listTab.delete(instutionUrl, choice);
				assertTrue(statusPage.waitForFinish());
				currentTab = statusPage.back();
			}
			if( currentTab != importTab )
			{
				importTab = listTab.importTab();
			}
		}

		assertTrue(importTab.importInstitution(instutionUrl, shortName, new File(instFolder, INSTITUTION_FILE).toPath())
			.waitForFinish());
	}
}

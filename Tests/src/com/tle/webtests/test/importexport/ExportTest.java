package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tle.webtests.pageobject.institution.ExportPage;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.StatusPage;

public class ExportTest extends AbstractInstTest
{
	@Test(dataProvider = "toExport")
	public void exportInstitution(File instFolder)
	{
		String shortName = instFolder.getName();
		String instutionUrl = context.getBaseUrl() + shortName + '/';

		InstitutionListTab listTab = new InstitutionListTab(context, testConfig.getAdminPassword()).load();
		if( listTab.institutionExists(instutionUrl) )
		{
			ExportPage export = listTab.export(instutionUrl);
			StatusPage<InstitutionListTab> statusPage = export.export();
			assertTrue(statusPage.waitForFinish(), statusPage.getErrorText());
			statusPage.back();
		}
	}

	@DataProvider(parallel = false)
	public Object[][] toExport() throws Exception
	{
		File[] institutions = new File(testConfig.getTestFolder(), "tests").listFiles();
		List<Object[]> instDirs = new ArrayList<Object[]>();
		for( File instDir : institutions )
		{
			if( new File(instDir, INSTITUTION_FILE).exists() )
			{
				instDirs.add(new Object[]{instDir});
			}
		}
		return instDirs.toArray(new Object[instDirs.size()][]);
	}
}

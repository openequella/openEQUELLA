package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ClassPrefixedName;
import com.tle.webtests.pageobject.ClonedPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditRegionPage;
import com.tle.webtests.pageobject.payment.backend.ShowRegionsPage;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @author Aaron
 */
@TestInstitution("ecommerce")
public class RegionTest extends AbstractCleanupTest
{
	@Name("DVD region codes are a digital-rights management technique designed to allow film distributors to control aspects of a release, including content, release date, and price")
	private static PrefixedName REGION1;
	@Name("¸")
	private static PrefixedName REGION1_NEWNAME;
	@Name("Normal Sort Of Name")
	private static PrefixedName REGION2;
	@Name("Weirdness ╩ë")
	private static PrefixedName REGION3;

	/**
	 * DTEC 17888
	 */
	@Test
	public void testRegionList()
	{
		PrefixedName clonedRegion2 = new ClonedPrefixedName((ClassPrefixedName) REGION2);

		logon("AutoTest", "automated");

		ShowRegionsPage page = new SettingsPage(context).load().showRegionPage();

		// "A table displays with the heading label Regions"

		// "Click on the add new region link"
		page = createRegion(
			page,
			REGION1,
			"The Veronicas are a rock band from Brisbane, Australia, formed in 1999 by twin sisters, Jessica and Lisa Origliasso. In addition to music, the sisters market their own line of clothing which debuted in 2007.  In 2005, The Veronicas released their first studio album entitled The Secret Life of... which peaked at #2 on the Australian charts and gained an Australian ARIA certification of 4x platinum for 280,000+ sales. In total, the album spawned five singles, three of which were top ten singles in Australia.  In 2007, the duo released their second studio album, Hook Me Up, which also peaked at #2 on the Australian charts and earned an Australian ARIA certification of 2x platinum for 140,000+ sales. To date, the album has garnered four Australian top ten singles. The album's title track, \"Hook Me Up\", was The Veronicas' first #1 single in Australia.[2]",
			"AF", "AG", "BR", "EG", "CU");
		page = createRegion(page, REGION2, "My description", "AF", "AG", "BR", "TV", "VI");

		page = createRegion(page, REGION3, "╔Fæ", "MH", "MQ", "MR", "MU", "YT", "MX");

		// "The new added region should be visible along with the edit delete links"
		assertTrue(page.entityExists(REGION1));
		assertTrue(page.actionExists(REGION1, "Disable"));
		assertTrue(page.actionExists(REGION1, "Edit"));
		assertTrue(page.entityExists(REGION2));
		assertTrue(page.actionExists(REGION2, "Disable"));
		assertTrue(page.actionExists(REGION2, "Edit"));
		assertTrue(page.entityExists(REGION3));
		assertTrue(page.actionExists(REGION3, "Disable"));
		assertTrue(page.actionExists(REGION3, "Edit"));

		// "Click on edit link"
		EditRegionPage editPage = page.editRegion(REGION1);

		// "Select a predefined region"
		editPage.selectPredefinedRegion("North America");

		// "The left side of the shuffle box should populate the correct countries according to the regions selected"
		List<String> countries = editPage.getSelectedCountries();
		assertEquals(countries.size(), 3, "Predefined North America country list is not length 3");
		assertTrue(countries.contains("US"), "US not found in predefined list");
		assertTrue(countries.contains("CA"), "CA not found in predefined list");
		assertTrue(countries.contains("MX"), "MX not found in predefined list");

		// "edit region title  and click on save"
		editPage.setName(REGION1_NEWNAME);

		// "User should be directed back to the region page and the changes done should be reflected"
		page = editPage.save();
		assertTrue(page.entityExists(REGION1_NEWNAME));
		assertTrue(page.actionExists(REGION1_NEWNAME, "Disable"));
		assertTrue(page.actionExists(REGION1_NEWNAME, "Edit"));

		// "Click on the delete link"
		page.disableEntity(REGION1_NEWNAME);
		page.deleteEntity(REGION1_NEWNAME);

		// "The region added should get deleted"
		assertFalse(page.entityExists(REGION1_NEWNAME));

		// "Click on the clone link"
		editPage = page.cloneRegion(REGION2);

		// "user should be directed to the region create page. It should reflect (region 2's settings)"
		assertEquals(editPage.getName(), clonedRegion2.toString(), "Clone has incorrect name");
		assertEquals(editPage.getDescription(), "My description", "Clone has incorrect description");
		countries = editPage.getSelectedCountries();
		assertEquals(countries.size(), 5, "Cloned country list is not length 5");
		assertTrue(countries.contains("AF"), "AF not found in country list");
		assertTrue(countries.contains("AG"), "AG not found in country list");
		assertTrue(countries.contains("BR"), "BR not found in country list");
		assertTrue(countries.contains("TV"), "TV not found in country list");
		assertTrue(countries.contains("VI"), "VI not found in country list");

		// "Click on save"
		page = editPage.save();

		// "Another instance of the region that was cloned should be available on the regions page"
		assertTrue(page.entityExists(clonedRegion2));

		// edit it again to verify fields
		editPage = page.editRegion(clonedRegion2);
		assertEquals(editPage.getName(), clonedRegion2.toString(), "Clone has incorrect name");
		assertEquals(editPage.getDescription(), "My description", "Clone has incorrect description");
		countries = editPage.getSelectedCountries();
		assertEquals(countries.size(), 5, "Cloned country list is not length 5");
		assertTrue(countries.contains("AF"), "AF not found in country list");
		assertTrue(countries.contains("AG"), "AG not found in country list");
		assertTrue(countries.contains("BR"), "BR not found in country list");
		assertTrue(countries.contains("TV"), "TV not found in country list");
		assertTrue(countries.contains("VI"), "VI not found in country list");

		page = editPage.cancel();
	}

	private ShowRegionsPage createRegion(ShowRegionsPage page, PrefixedName name, String description,
		String... countries)
	{
		EditRegionPage editPage = page.createRegion();
		editPage.setName(name);
		editPage.setDescription(description);
		editPage.selectCountries(countries);
		return editPage.save();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		super.cleanupAfterClass();

		logon("AutoTest", "automated");

		ShowRegionsPage page = new SettingsPage(context).load().showRegionPage();
		page.deleteAllNamed(getNames());
		page.deleteAllNamed(new ClonedPrefixedName((ClassPrefixedName) REGION2));
	}
}

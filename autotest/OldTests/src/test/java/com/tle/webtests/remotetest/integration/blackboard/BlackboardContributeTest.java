package com.tle.webtests.remotetest.integration.blackboard;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardContentPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardCoursePage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardEditItemPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardLoginPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardMyInstitutionPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.ImagePage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

@Test(groups = "blackboardTest")
@TestInstitution("blackboard")
public class BlackboardContributeTest extends AbstractBlackboardTest
{
	private static final String FOLDER1 = "SpecialCharacters1";
	private static final String FOLDER2 = "SpecialCharacters2";
	private static final String ITEM_TO_MOVE = "ItemToSelectAndThenMove";
	private static final String WEIRD_ITEM = "UnicodeItem - Name with <>'\" ? & chars";

	private static final String ITEM_SPECIAL_CHARACTERS = "SpecialCharacters - Attachments";
	private static final String FOLDER_SPECIAL_CHARACTERS = "SpecialCharacters - Folder";

	@Test
	public void selectItemAndEdit()
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();
		SelectionSession selection = content.addEquellaResource();
		ItemListPage items = selection.getSearchPage().exactQuery("UnicodeItem");
		SummaryPage summary = items.viewFromTitle(WEIRD_ITEM);
		summary.selectItemSummary();
		content = content.finishAddEquellaResource(selection);
		BlackboardEditItemPage editPage = content.editResource(WEIRD_ITEM);
		Assert.assertEquals(editPage.getName(), WEIRD_ITEM);
	}

	@Test
	public void selectItemAndMoveCopy()
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage information = coursePage.information();
		assertFalse(information.hasResource(WEIRD_ITEM));
		BlackboardContentPage content = coursePage.content();
		SelectionSession selection = content.addEquellaResource();
		ItemListPage items = selection.getSearchPage().exactQuery(ITEM_TO_MOVE);
		SummaryPage summary = items.viewFromTitle(ITEM_TO_MOVE);
		summary.selectItemSummary();
		content = content.finishAddEquellaResource(selection);

		assertTrue(content.hasResource(ITEM_TO_MOVE));
		content.moveResource(ITEM_TO_MOVE, COURSE_NAME, "Information");
		assertFalse(content.hasResource(ITEM_TO_MOVE));
		information = coursePage.information();
		assertTrue(information.hasResource(ITEM_TO_MOVE));

		information.copyResource(ITEM_TO_MOVE, COURSE_NAME, "Content");
		assertTrue(information.hasResource(ITEM_TO_MOVE));
		content = coursePage.content();
		assertTrue(content.hasResource(ITEM_TO_MOVE));
		SummaryPage itemSummary = content.viewResource(ITEM_TO_MOVE, new SummaryPage(context));
		assertEquals(itemSummary.getItemTitle(), ITEM_TO_MOVE);

		content = content.returnFromResource("Content");
		information = coursePage.information();
		itemSummary = information.viewResource(ITEM_TO_MOVE, new SummaryPage(context));
		assertEquals(itemSummary.getItemTitle(), ITEM_TO_MOVE);

	}

	@Test
	public void attachmentsWithSpecialCharacters()
	{
		testWithItemAndFolder(ITEM_SPECIAL_CHARACTERS, FOLDER1);
	}

	@Test
	public void attachmentFolderWithSpecialCharacters()
	{
		testWithItemAndFolder(FOLDER_SPECIAL_CHARACTERS, FOLDER2);
	}

	private void testWithItemAndFolder(String item, String folder)
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();

		content = content.addFolder(folder);
		content = content.enterFolder(folder);

		SelectionSession selection = content.addEquellaResource();
		ItemListPage items = selection.getSearchPage().exactQuery(item);
		SummaryPage summary = items.viewFromTitle(item);
		AttachmentsPage attachmentsSection = summary.attachments();
		List<String> attachments = attachmentsSection.attachmentOrder();
		for( String attachment : attachments )
		{
			attachmentsSection.selectAttachmentStructured(attachment);
		}
		content = content.finishAddEquellaResource(selection);

		for( String attachment : attachments )
		{
			assertTrue(content.hasAttachment(attachment));
		}

		for( String attachment : attachments )
		{
			if( attachment.endsWith("png") )
			{
				ImagePage imagePage = content.viewResource(attachment, new ImagePage(context));
				assertTrue(imagePage.imageSource().contains("google"));
				content = content.returnFromResource(folder);
			}
		}
		content = coursePage.content();
		content.deleteResource(folder);
		assertFalse(content.hasResource(folder));
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();
		content.deleteResourceIfExists(WEIRD_ITEM);
		content.deleteResourceIfExists(ITEM_TO_MOVE);
		content.deleteResourceIfExists(FOLDER1);
		content.deleteResourceIfExists(FOLDER2);

		BlackboardContentPage information = coursePage.information();
		information.deleteResourceIfExists(WEIRD_ITEM);
		information.deleteResourceIfExists(ITEM_TO_MOVE);

	}
}

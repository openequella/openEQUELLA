package com.tle.webtests.test.viewing.actions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import com.dytech.common.io.ZipUtils;
import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.LocalWebDriver;
import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.DownloadFilePage;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.searching.BulkSection;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.ExportItemPage;
import com.tle.webtests.pageobject.viewitem.PackageViewer;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.NavigationBuilder;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.WebPagesUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("fiveo")
@LocalWebDriver
public class ItemExportTest extends AbstractCleanupAutoTest
{
	private static final String IMS_PACKAGE_ZIP = "IMS - Air pressure particle model.zip";
	private static final String IMS_PACKAGE_TITLE = "Air pressure: particle model";
	private static final String IMS_PACKAGE_MD5 = "1945e9045110a1cac450dfe1620b6e58";
	private static final String IMS_PACKAGE_ITEM = "IMS Package for Exporting";

	@Name("Mets imported")
	private static PrefixedName METS_IMPORTED;
	@Name("IMS Package - Edited")
	private static PrefixedName IMS_EDITED;
	@Name("IMS imported")
	private static PrefixedName IMS_IMPORTED;
	@Name("New item to export")
	private static PrefixedName NEW_ITEM_TO_EXPORT;
	@Name("New item IMS import")
	private static PrefixedName NEW_ITEM_IMS_IMPORT;

	@Name("IMS export")
	private static PrefixedName IMS_EXPORT;
	@Name("Mets zip export")
	private static PrefixedName METS_ZIP_EXPORT;
	@Name("Mets xml export")
	private static PrefixedName METS_XML_EXPORT;

	@Test
	public void metsExportTest() throws Exception
	{
		SummaryPage view = SearchPage.searchAndView(context, IMS_PACKAGE_ITEM);
		ExportItemPage exportPage = view.exportPage();

		DownloadFilePage downloadFilePage = new DownloadFilePage(context, IMS_PACKAGE_ITEM.replace(" ", "_")
			+ "_METS.xml", 2600);
		downloadFilePage.deleteFile();
		exportPage.exportAsMETS(false);
		downloadFilePage.get();

		assertTrue(downloadFilePage.fileIsDownloaded());
		PropBagEx metsXml = new PropBagEx(downloadFilePage.getFile());
		assertEquals(metsXml.getAttributesForNode("structMap/div").get("LABEL"), IMS_PACKAGE_ITEM);
		assertTrue(downloadFilePage.deleteFile());

		downloadFilePage = new DownloadFilePage(context, IMS_PACKAGE_ITEM.replace(" ", "_") + "_METS.zip", 240000);
		downloadFilePage.deleteFile();
		exportPage.get().exportAsMETS(true);
		downloadFilePage.get();
		assertTrue(downloadFilePage.fileIsDownloaded());
		File metsExtracted = new File(FileUtils.getTempDirectory(), "mets-manifest.xml");
		FileUtils.deleteQuietly(metsExtracted);
		ZipUtils.extract(downloadFilePage.getFile(), FileUtils.getTempDirectory());

		assertTrue(metsExtracted.exists());
		metsXml = new PropBagEx(metsExtracted);
		assertEquals(metsXml.getAttributesForNode("structMap/div").get("LABEL"), IMS_PACKAGE_ITEM);
		assertEquals(metsXml.nodeCount("fileSec/fileGrp/file"), 18);

		// re-upload the mets package
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, METS_IMPORTED);
		UniversalControl control = wizard.universalControl(2);
		FileUniversalControlType file = control.addResource(new FileUniversalControlType(control));
		file.uploadMETSOption(downloadFilePage.getFile().toURI().toURL()).save();
		assertEquals(wizard.universalControl(2).resourceCount(), 1, "There should only be one attachment!");
		view = wizard.save().publish();

		// check stuff
		AttachmentsPage attachments = view.attachments();
		assertTrue(attachments.folderExists(IMS_PACKAGE_ITEM));
		assertTrue(attachments.attachmentExists("index.html"));

		PackageViewer packageViewer = attachments.viewFullscreen();
		assertTrue(packageViewer
			.selectedAttachmentContainsText("Curriculum Corporation, 2009, except where indicated under Acknowledgements"));

		FileUtils.deleteQuietly(metsExtracted);
		assertTrue(downloadFilePage.deleteFile());

	}

	@Test(dependsOnMethods = {"metsExportTest"})
	public void imsExportTest() throws Exception
	{
		SummaryPage view = SearchPage.searchAndView(context, IMS_PACKAGE_ITEM);
		ExportItemPage exportPage = view.exportPage();

		// Export the original ims package and make sure its the same md5
		DownloadFilePage downloadFilePage = new DownloadFilePage(context, IMS_PACKAGE_ZIP, IMS_PACKAGE_MD5);
		downloadFilePage.deleteFile();
		exportPage.downloadOriginalIms();
		downloadFilePage.get();
		assertTrue(downloadFilePage.fileIsDownloaded());
		assertTrue(downloadFilePage.deleteFile());

		// Create a new version
		WizardPageTab newVersion = exportPage.newVersion();
		newVersion.editbox(1, IMS_EDITED);
		view = newVersion.save().publish();
		assertEquals(view.getItemTitle(), IMS_EDITED.toString());
		exportPage = view.exportPage();

		// Export the original ims package and make sure its the same md5
		downloadFilePage = new DownloadFilePage(context, IMS_PACKAGE_ZIP, IMS_PACKAGE_MD5);
		downloadFilePage.deleteFile();
		exportPage.downloadOriginalIms();
		downloadFilePage.get();
		assertTrue(downloadFilePage.fileIsDownloaded());

		// Check that the title hasnt changed
		File imsExtracted = new File(FileUtils.getTempDirectory(), IMS_PACKAGE_ZIP.replace(".zip", ""));
		FileUtils.deleteQuietly(imsExtracted);
		ZipUtils.extract(downloadFilePage.getFile(), imsExtracted);
		PropBagEx imsXml = new PropBagEx(new File(imsExtracted, "imsmanifest.xml"));
		assertEquals(imsXml.getNode("metadata/lom/general/title/langstring"), IMS_PACKAGE_TITLE);
		FileUtils.deleteQuietly(imsExtracted);
		assertTrue(downloadFilePage.deleteFile());

		// Export the updated ims package
		downloadFilePage = new DownloadFilePage(context, "export.zip", 210000);
		downloadFilePage.deleteFile();
		exportPage.get().downloadUpdatedIms();
		downloadFilePage.get();
		assertTrue(downloadFilePage.fileIsDownloaded());

		// Check that the title has changed
		imsExtracted = new File(FileUtils.getTempDirectory(), "export");
		FileUtils.deleteQuietly(imsExtracted);
		ZipUtils.extract(downloadFilePage.getFile(), imsExtracted);
		imsXml = new PropBagEx(new File(imsExtracted, "imsmanifest.xml"));
		assertEquals(imsXml.getNode("metadata/lom/general/title/langstring"), IMS_EDITED.toString());

		// re-upload the ims package
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, IMS_IMPORTED);

		UniversalControl control = wizard.universalControl(2);
		FileUniversalControlType file = control.addResource(new FileUniversalControlType(control));
		file.uploadPackageOption(downloadFilePage.getFile().toURI().toURL()).showStructure().save();

		assertEquals(wizard.universalControl(2).resourceCount(), 1, "There should only be one attachment!");
		view = wizard.save().publish();

		// check stuff
		AttachmentsPage attachments = view.attachments();
		assertTrue(attachments.folderExists(IMS_PACKAGE_TITLE));
		assertTrue(attachments.attachmentExists("Start: Air pressure: particle model"));

		PackageViewer packageViewer = attachments.viewFullscreen();
		assertTrue(packageViewer
			.selectedAttachmentContainsText("Curriculum Corporation, 2009, except where indicated under Acknowledgements"));

		FileUtils.deleteQuietly(imsExtracted);
		assertTrue(downloadFilePage.deleteFile());
	}

	@Test
	public void newItemImsExportTest() throws Exception
	{
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, NEW_ITEM_TO_EXPORT);
		wizard.addFiles(2, false, "page.html", "page.html", "page.html");
		NavigationBuilder nav = wizard.next().navigation();
		nav.addTopLevelNode("Parent Node", "page.html");
		nav.addChild("Parent Node", "Child 1", "page(2).html");
		nav.addChild("Parent Node", "Child 2", "page(3).html");
		SummaryPage view = wizard.get().save().publish();

		assertTrue(view.hasAttachmentsSection());
		AttachmentsPage attachments = view.attachments();
		assertTrue(attachments.attachmentExists("Parent Node"));
		assertTrue(attachments.attachmentExists("Child 1"));
		assertTrue(attachments.attachmentExists("Child 2"));

		ExportItemPage exportPage = view.exportPage();
		DownloadFilePage download = new DownloadFilePage(context, "export.zip", 1000);
		download.deleteFile();
		exportPage.downloadUpdatedIms();
		download.get();
		assertTrue(download.fileIsDownloaded());

		wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, NEW_ITEM_IMS_IMPORT);

		UniversalControl control = wizard.universalControl(2);
		FileUniversalControlType file = control.addResource(new FileUniversalControlType(control));
		file.uploadPackageOption(download.getFile().toURI().toURL()).showStructure().save();
		view = wizard.get().save().publish();

		assertTrue(view.hasAttachmentsSection());
		attachments = view.attachments();
		assertTrue(attachments.folderExists(NEW_ITEM_TO_EXPORT.toString()));
		assertTrue(attachments.attachmentExists("Parent Node"));
		assertTrue(attachments.attachmentExists("Child 1"));
		assertTrue(attachments.attachmentExists("Child 2"));
		assertTrue(attachments.viewAttachment("Child 2", new VerifyableAttachment(context)).isVerified());

		// Webpage for exporting to cover
		// MyPagesIMSExporter & MyPagesPackackageExporterUtils
		// ims
		wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, IMS_EXPORT);
		control = wizard.universalControl(2);
		control.addResource(new WebPagesUniversalControlType(control)).addPage(namePrefix, "export me");

		view = wizard.save().publish();
		exportPage = view.exportPage();

		download = new DownloadFilePage(context, "export.zip");
		download.deleteFile();
		exportPage.downloadUpdatedIms();
		download.get();
		assertTrue(download.fileIsDownloaded());
		// mets.zip
		wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, METS_ZIP_EXPORT);
		control = wizard.universalControl(2);
		control.addResource(new WebPagesUniversalControlType(control)).addPage(namePrefix, "export me");

		view = wizard.save().publish();
		exportPage = view.exportPage();

		download = new DownloadFilePage(context, namePrefix + "_-_Mets_zip_export_METS.zip");
		download.deleteFile();
		exportPage.exportAsMETS(true);
		download.get();
		assertTrue(download.fileIsDownloaded());
		// mets.xml
		wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, METS_XML_EXPORT);
		control = wizard.universalControl(2);
		control.addResource(new WebPagesUniversalControlType(control)).addPage(namePrefix, "export me");

		view = wizard.save().publish();
		exportPage = view.exportPage();

		download = new DownloadFilePage(context, namePrefix + "_-_Mets_xml_export_METS.xml");
		download.deleteFile();
		exportPage.exportAsMETS(false);
		download.get();
		assertTrue(download.fileIsDownloaded());
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");
		ItemAdminPage itemAdminPage = new ItemAdminPage(context).load();
		itemAdminPage.exactQuery(IMS_PACKAGE_ITEM);
		BulkSection bulk = itemAdminPage.get().bulk();
		bulk.selectAll();
		bulk.executeCommand("makelive");
		super.cleanupAfterClass();
	}
}

package com.tle.webtests.test.webservices.rest.payment;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.testng.annotations.Test;

import com.google.common.io.Closeables;
import com.tle.common.Pair;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.StoreSetupPage;
import com.tle.webtests.test.files.Attachments;
import com.tle.webtests.test.webservices.rest.AbstractRestApiTest;

/**
 * @see Redmine: #6836
 */

@TestInstitution("storebackend2ssl")
public class StoreImagesApiTest extends AbstractRestApiTest
{
	@Test
	public void setupImages() throws Exception
	{
		logon("autotest", "automated");

		StoreSetupPage page = new SettingsPage(context).load().storeSetupPage();
		URL icon = Attachments.get("shopicon.jpeg");
		URL image = Attachments.get("shopimage.jpeg");
		page.uploadIcon(icon);
		page.uploadImage(image);
		page.save();

		logout();

		checkDownload("api/store/icon/small.jpg", "2690713be65fb4b2563d0c17c380881a",
			"a25f89fb0f4fcff00f841e0c035afa4e", "22ffc42d770acfae7c8e63171c5d0be2");
		checkDownload("api/store/icon/large.jpg", "4e5d86b94d7a60bb334801cdbd479593",
			"b9352bde42eb703ec3adedc7f6bf110b", "bf24a9df9639d255aadb4dd799c7a2d1");
	}

	@Test(dependsOnMethods = {"setupImages"})
	public void deleteImages() throws Exception
	{
		logon("autotest", "automated");

		StoreSetupPage page = new SettingsPage(context).load().storeSetupPage();
		page.deleteIcon();
		page.deleteImage();
		page.save();

		logout();

		checkDownload("api/store/icon/small.jpg", "22ffc42d770acfae7c8e63171c5d0be2");
		checkDownload("api/store/icon/large.jpg", "05712326675ff084252d51fb2dfe591d");
	}

	private void checkDownload(String path, String... hashes) throws Exception
	{

		final File file = File.createTempFile("AAAA", "jpeg");
		file.deleteOnExit();
		download(context.getBaseUrl() + path, file);

		final String echoedMd5 = md5(file);

		// The image hash might be different depending on the conversion
		// environment, possibly the size also

		boolean found = false;
		for( String origMd5 : hashes )
		{
			if( origMd5.equals(echoedMd5) )
			{
				found = true;
				break;
			}
		}
		assertTrue(found,
			"MD5 of image " + file + " was " + echoedMd5 + " and was not found in " + Arrays.toString(hashes));
	}

	private String md5(File file) throws IOException
	{
		InputStream inp = new FileInputStream(file);
		try
		{
			return DigestUtils.md5Hex(inp);
		}
		finally
		{
			Closeables.closeQuietly(inp);
		}
	}

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		// No need
	}

}

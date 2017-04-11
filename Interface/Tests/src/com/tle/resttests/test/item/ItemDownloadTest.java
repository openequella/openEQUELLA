package com.tle.resttests.test.item;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.ItemId;
import com.tle.json.requests.ItemRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.files.Attachments;

public class ItemDownloadTest extends AbstractRestAssuredTest
{
	private static final String AVATAR_PATH = "avatar.png";
	private static final String AVATAR_PNG_ETAG = "\"5a4e69eeae86aa557c4a27d52257b757\"";
	private static final ItemId ITEM_ID = new ItemId("2f6e9be8-897d-45f1-98ea-7aa31b449c0e", 1);

	private ItemRequests staging;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		staging = builder().items();
	}

	@Test
	public void restrictions() throws DateParseException
	{
		RequestSpecification notModified = staging.notModified();
		notModified.header("If-None-Match", AVATAR_PNG_ETAG);
		staging.file(notModified, ITEM_ID, AVATAR_PATH);

		Response response = staging.headFile(ITEM_ID, AVATAR_PATH);
		Date date = DateUtil.parseDate(response.header("Last-Modified"));

		notModified = staging.notModified();
		notModified.header("If-Modified-Since", DateUtil.formatDate(date));
		staging.file(notModified, ITEM_ID, AVATAR_PATH);
	}

	@Test(groups = "eps", dependsOnMethods = "restrictions")
	public void partial() throws IOException
	{
		// put a file to the folder1 content url
		final File file = new File(getPathFromUrl(Attachments.get("avatar.png")));

		RequestSpecification partial = staging.partialContent();
		partial.header("Range", "bytes=100-199");
		partial.expect().header("Content-Range", "bytes 100-199/12627").header("Content-Length", "100");
		byte[] second100 = staging.file(partial, ITEM_ID, AVATAR_PATH).asByteArray();
		byte[] original = new byte[100];
		FileInputStream finp = new FileInputStream(file);
		try
		{
			finp.skip(100);
			ByteStreams.read(finp, original, 0, 100);
			assertEquals(second100, original, "Bytes returned are wrong");
		}
		finally
		{
			finp.close();
		}
	}
}

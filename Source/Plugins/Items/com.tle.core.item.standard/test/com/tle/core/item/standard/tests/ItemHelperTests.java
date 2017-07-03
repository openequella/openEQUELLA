package com.tle.core.item.standard.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentLocale.AbstractCurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.i18n.CurrentTimeZone.AbstractCurrentTimeZone;
import com.tle.core.item.helper.AttachmentHelper;
import com.tle.core.item.helper.ItemDetailsHelper;
import com.tle.common.i18n.LangUtils;

public class ItemHelperTests
{
	@Before
	public void setupItem()
	{
		CurrentTimeZone.initialise(new AbstractCurrentTimeZone()
		{
			@Override
			public TimeZone get()
			{
				return TimeZone.getTimeZone("GMT"); //$NON-NLS-1$
			}
		});

		CurrentLocale.initialise(new AbstractCurrentLocale()
		{
			@Override
			protected Pair<Locale, String> resolveKey(String key)
			{
				return null;
			}

			@Override
			public boolean isRightToLeft()
			{
				return false;
			}

			@Override
			public ResourceBundle getResourceBundle()
			{
				return null;
			}

			@Override
			public Locale getLocale()
			{
				return Locale.getDefault();
			}
		});
	}

	@SuppressWarnings("nls")
	@Test
	public void testAttachmentsLoad()
	{
		Item item = new Item();
		AttachmentHelper attachmentHelper = new AttachmentHelper();
		List<Attachment> attachments = Lists.newArrayList();

		FileAttachment file = new FileAttachment();
		file.setConversion(true);
		file.setSize(1000);
		file.setFilename("frog");
		file.setThumbnail("thumby.png");
		attachments.add(file);

		CustomAttachment custom = new CustomAttachment();
		custom.setType("whatever");
		custom.setData("test", 1);
		custom.setData("val", "val");
		attachments.add(custom);

		ImsAttachment ims = new ImsAttachment();
		ims.setUrl("package.zip");
		ims.setDescription("ims");
		ims.setScormVersion("19");
		attachments.add(ims);

		IMSResourceAttachment imsres = new IMSResourceAttachment();
		imsres.setUrl("resource.txt");
		attachments.add(imsres);

		LinkAttachment link = new LinkAttachment();
		link.setUrl("http://google.com/");
		attachments.add(link);

		ZipAttachment zip = new ZipAttachment();
		zip.setUrl("zipfile.zip");
		attachments.add(zip);

		item.setAttachments(attachments);
		PropBagEx outXml = new PropBagEx();
		attachmentHelper.load(outXml, item);
		PropBagThoroughIterator iter = outXml.iterateAll("attachments/attachment");
		PropBagEx fattach = iter.next();
		assertEquals("true", fattach.getNode("conversion"));
		assertEquals("1000", fattach.getNode("size"));
		assertEquals("thumby.png", fattach.getNode("thumbnail"));
		assertEquals("local", fattach.getNode("@type"));
		assertEquals("frog", fattach.getNode("file"));
		PropBagEx cattach = iter.next();
		assertEquals("custom", cattach.getNode("@type"));
		assertEquals("whatever", cattach.getNode("type"));
		PropBagThoroughIterator dIter = cattach.iterateAll("entry");
		while( dIter.hasNext() )
		{
			PropBagEx entry = dIter.next();
			String name = entry.getNode("string[0]");
			if( name.equals("val") )
			{
				assertEquals("val", entry.getNode("string[1]"));
			}
			else if( name.equals("test") )
			{
				assertEquals("1", entry.getNode("int"));
			}
		}
		PropBagEx imsattach = iter.next();
		assertEquals("imsres", imsattach.getNode("@type"));
		assertEquals("resource.txt", imsattach.getNode("file"));

		PropBagEx linkattach = iter.next();
		assertEquals("remote", linkattach.getNode("@type"));
		assertEquals("http://google.com/", linkattach.getNode("file"));
		assertEquals("false", linkattach.getNode("@disabled"));

		PropBagEx zipattach = iter.next();
		assertEquals("zip", zipattach.getNode("@type"));
		assertEquals("zipfile.zip", zipattach.getNode("file"));

		assertEquals("package.zip", outXml.getNode("itembody/packagefile"));
		assertEquals("ims", outXml.getNode("itembody/packagefile/@name"));
		assertEquals("0", outXml.getNode("itembody/packagefile/@size"));
		assertEquals("19", outXml.getNode("itembody/packagefile/@scorm"));
	}

	@SuppressWarnings("nls")
	@Test
	public void testAttachmentsSave()
	{
		PropBagEx attachXml = new PropBagEx(
			new UnicodeReader(getClass().getResourceAsStream("attachments.xml"), "UTF-8"));
		Item item = new Item();
		new AttachmentHelper().save(attachXml, item, new HashSet<String>());
		List<Attachment> attachments = item.getAttachments();
		FileAttachment fattach = (FileAttachment) attachments.get(0);
		assertEquals("frog", fattach.getFilename());
		assertEquals("thumby.png", fattach.getThumbnail());
		assertEquals(1000, fattach.getSize());
		assertTrue(fattach.isConversion());

		CustomAttachment cattach = (CustomAttachment) attachments.get(1);
		assertEquals("whatever", cattach.getType());
		assertEquals("val", cattach.getData("val"));
		assertEquals(1, cattach.getData("test"));

		IMSResourceAttachment imsres = (IMSResourceAttachment) attachments.get(2);
		assertEquals("resource.txt", imsres.getUrl());

		LinkAttachment linkattach = (LinkAttachment) attachments.get(3);
		assertEquals("http://google.com/", linkattach.getUrl());

		ZipAttachment zipattach = (ZipAttachment) attachments.get(4);
		assertEquals("zipfile.zip", zipattach.getUrl());

		ImsAttachment imsattach = (ImsAttachment) attachments.get(5);
		assertEquals("package.zip", imsattach.getUrl());
		assertEquals("19", imsattach.getScormVersion());
		assertEquals(0, imsattach.getSize());
	}

	@SuppressWarnings("nls")
	@Test
	public void testDetailsLoad()
	{
		Item item = new Item();
		item.setId(100);
		Calendar cal = Calendar.getInstance(CurrentTimeZone.get());
		cal.clear();
		item.setName(LangUtils.createTextTempLangugageBundle("name"));
		item.setDescription(LangUtils.createTextTempLangugageBundle("description"));
		item.setDateCreated(cal.getTime());
		item.setDateModified(cal.getTime());
		item.setDateForIndex(cal.getTime());
		item.setModerating(true);
		item.setVersion(2);
		item.setStatus(ItemStatus.REJECTED);
		item.setOwner("fred");
		PropBagEx itemxml = new PropBagEx();
		new ItemDetailsHelper().load(itemxml, item);
		assertEquals("1970-01-01T00:00:00+0000", itemxml.getNode("datecreated"));
		assertEquals("1970-01-01T00:00:00+0000", itemxml.getNode("datemodified"));
		assertEquals("1970-01-01T00:00:00+0000", itemxml.getNode("dateforindex"));
		assertEquals("100", itemxml.getNode("@key"));
		assertEquals("2", itemxml.getNode("@version"));
		assertEquals("true", itemxml.getNode("@moderating"));
		assertEquals("rejected", itemxml.getNode("@itemstatus"));
		assertEquals("fred", itemxml.getNode("owner"));
		assertEquals("name", itemxml.getNode("name"));
		assertEquals("description", itemxml.getNode("description"));
		assertEquals("-1.0", itemxml.getNode("rating/@average"));
	}

	@SuppressWarnings("nls")
	@Test
	public void testDetailsSave()
	{
		PropBagEx detailsXml = new PropBagEx(new UnicodeReader(getClass().getResourceAsStream("details.xml"), "UTF-8"));
		Item item = new Item();
		new ItemDetailsHelper().save(detailsXml, item, new HashSet<String>());
		assertEquals(100, item.getId());
		Calendar cal = Calendar.getInstance(CurrentTimeZone.get());
		cal.clear();
		Date epoch = cal.getTime();
		assertEquals(epoch, item.getDateCreated());
		assertEquals(epoch, item.getDateModified());
		assertEquals(2, item.getVersion());
		assertEquals("fred", item.getOwner());
		assertEquals((float) -1.0, item.getRating());
		assertEquals(ItemStatus.REJECTED, item.getStatus());
		assertEquals("itemdef-uuid", item.getItemDefinition().getUuid());
	}
}

package com.tle.blackboard.webservice.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import blackboard.data.content.Content;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.content.ContentDbLoader;

import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.content.ContentUtil;
import com.tle.blackboard.common.content.ContentUtil.ContentRegisteredResponse;
import com.tle.blackboard.common.content.ItemInfo;
import com.tle.blackboard.common.content.ItemKey;
import com.tle.blackboard.common.content.RegistrationUtil;

/**
 * @author Aaron
 */
public class SynchroniseContentThread extends Thread
{
	private final String institutionUrl;
	private final boolean available;

	SynchroniseContentThread(String institutionUrl, boolean available)
	{
		this.institutionUrl = institutionUrl;
		this.available = available;
	}

	private void recurseChildren(ContentDbLoader contentDbLoader, Content content, List<Content> equellaContents)
		throws Exception
	{
		if( content.getIsFolder() )
		{
			BbUtil.debug("Content \"" + content.getTitle() + "\" is a folder.  Recursively getting contents.");
			for( Content courseContent : contentDbLoader.loadChildren(content.getId()) )
			{
				recurseChildren(contentDbLoader, courseContent, equellaContents);
			}
		}
		else
		{
			BbUtil.debug("Content \"" + content.getTitle() + "\" is content.  Checking EQUELLA-ness.");

			// check for EQUELLA-ness
			String handler = content.getContentHandler();
			if( handler.equals(BbUtil.CONTENT_HANDLER) || handler.equals("resource/tle-myitem")
				|| handler.equals("resource/tle-plan") )
			{
				BbUtil.debug("Content \"" + content.getTitle() + "\" has a matching handler.");
				equellaContents.add(content);

				// we need to see if it belongs to this institution (NO CAN
				// DO??)...
				/*
				 * final Matcher equellaMatcher =
				 * getEquellaUrlMatcher(institutionUrl, content
				 * .getBody().getText()); if( equellaMatcher.find() ) {
				 * BbUtil.debug("URL matched!  Adding to list.");
				 * equellaContents.add(content); } else {
				 * BbUtil.debug("No URL match."); }
				 */
			}
		}
	}

	@Override
	public void run()
	{
		// scan every course, look at every content and see if it's in the
		// database, if it's not then add it
		final BbPersistenceManager pm = BbContext.instance().getPersistenceManager();
		try
		{
			final ContentDbLoader contentDbLoader = (ContentDbLoader) pm.getLoader(ContentDbLoader.TYPE);

			for( blackboard.data.course.Course course : WebServiceUtil.getBbCourses(null) )
			{
				final List<Content> equellaContents = new ArrayList<Content>();
				final boolean courseAvailable = course.getIsAvailable();
				// if( available && !course.getIsAvailable() )
				// {
				// debug("Course " + course.getTitle() +
				// " is not Available, skipping");
				// continue;
				// }

				final Id courseId = course.getId();
				BbUtil.debug("Looking at course " + courseId.toExternalString());

				for( Content folder : BbUtil.getBbFoldersForCourse(courseId) )
				{
					BbUtil.debug("Found folder directly under course: " + folder.getTitle());
					recurseChildren(contentDbLoader, folder, equellaContents);
				}

				final Set<ItemKey> itemContent = new HashSet<ItemKey>();
				BbUtil.debug("Found " + equellaContents.size() + " EQUELLA contents");
				for( Content equellaContent : equellaContents )
				{
					final Id folderId = equellaContent.getParentId();

					// BbUtil.debug("Getting properties for "
					// + equellaContent.getId().toExternalString() + " "
					// + equellaContent.getTitle());
					ItemInfo itemInfo = null;
					try
					{
						itemInfo = ContentUtil.instance().ensureProperties(equellaContent, course, folderId,
							institutionUrl);

					}
					catch( Exception t )
					{
						// ignore
					}

					// In freak cases it can return null
					if( itemInfo != null )
					{
						final ItemKey itemKey = itemInfo.getItemKey();

						// check the DB for it, if not then insert
						ContentRegisteredResponse reg = RegistrationUtil.contentIsRegistered(itemKey);
						if( !reg.isRegistered() )
						{
							BbUtil.debug("Content \"" + itemKey + "\" not in DB so recording it");
							// EquellaContentUtil.recordItem(itemInfo.getItemKey(),
							// true,
							// equellaContent.getIsAvailable(), courseAvailable,
							// equellaContent
							// .getTitle(),
							// equellaContent.getCreatedDate().getTime(),
							// equellaContent.getModifiedDate().getTime(),
							// course.getTitle());
						}
						else
						// if( reg.isAvailable() != course.getIsAvailable() )
						{
							// BbUtil.debug("Content is marked as available="
							// + reg.isAvailable() + " but course is available="
							// + courseAvailable
							// + ".  Updating entry.");
							// EquellaContentUtil.recordItem(itemInfo.getItemKey(),
							// false,
							// equellaContent.getIsAvailable(), courseAvailable,
							// equellaContent
							// .getTitle(),
							// equellaContent.getCreatedDate().getTime(),
							// equellaContent.getModifiedDate().getTime(),
							// course.getTitle());
							BbUtil.debug("Content \"" + itemKey
								+ "\" already in DB, updating record in case of changes");
						}
						// else
						// {
						// BbUtil.debug("Content \"" + itemKey
						// + "\" is already registered in DB");
						// }

						// Need to extract the current description (the XML is
						// kept up to date)
						final String description = itemInfo.getDescription();

						RegistrationUtil.recordItem(itemInfo.getItemKey(), !reg.isRegistered(), equellaContent
							.getIsAvailable(), courseAvailable, equellaContent.getTitle(), description, itemInfo
							.getAttachmentName(), equellaContent.getCreatedDate().getTime(), equellaContent
							.getModifiedDate().getTime(), course.getTitle());

						itemContent.add(itemKey);
					}
				}

				// check DB rows for this institutionUrl and course, whatever
				// not found in contents should be chucked
				final List<ItemKey> dbItems = RegistrationUtil.findEquellaContentByCourse(institutionUrl,
					courseId.toExternalString(), available);
				BbUtil.debug("Found " + dbItems.size()
					+ " registered content.  Cross referencing it with discovered content.");
				for( ItemKey dbItem : dbItems )
				{
					if( !itemContent.contains(dbItem) )
					{
						BbUtil.debug("Content \"" + dbItem
							+ "\" in DB but not found in course contents.  Removing from DB.");
						RegistrationUtil.unrecordItem(dbItem.getDatabaseId(), dbItem.getContentId());
					}
				}
			}

			// Cleanup NULL contentid columns. These should no longer exist.
			BbUtil.debug("Removing registered content with bad data.");
			int removed = RegistrationUtil.cleanupBadContent(institutionUrl);
			if( removed > 0 )
			{
				BbUtil.debug("Removed " + removed + " entries with bad data.");
			}
			else
			{
				BbUtil.debug("No bad data exists.");
			}
		}
		catch( Exception e )
		{
			BbUtil.error("Exception happened", e);
			throw new RuntimeException(e);
		}
	}
}
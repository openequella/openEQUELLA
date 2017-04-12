package com.tle.blackboard.common.content;

import static com.tle.blackboard.common.SqlUtil.delete;
import static com.tle.blackboard.common.SqlUtil.insert;
import static com.tle.blackboard.common.SqlUtil.select;
import static com.tle.blackboard.common.SqlUtil.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import blackboard.db.BbDatabase;
import blackboard.persist.DatabaseContainer;
import blackboard.platform.persistence.PersistenceServiceFactory;

import com.google.common.base.Strings;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.SqlUtil;
import com.tle.blackboard.common.SqlUtil.OptionalParam;
import com.tle.blackboard.common.SqlUtil.ResultProcessor;
import com.tle.blackboard.common.content.ContentUtil.ContentRegisteredResponse;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
// @NonNullByDefault
public abstract class RegistrationUtil
{
	// until proven otherwise. The databaseVersion is currently 5 (as of after 6.3 GA)
	private static int databaseVersion = 0;

	private static final String COL_DATABASE_ID = "ID";
	private static final String COL_INSTITUTION_URL = "INSTURL";
	private static final String COL_DATE_CREATED = "DTCREATED";
	private static final String COL_DATE_MODIFIED = "DTMODIFIED";
	private static final String COL_UUID = "UUID";
	private static final String COL_VERSION = "VERSION";
	private static final String COL_PAGE = "PAGE";
	private static final String COL_CONTENT_ID = "CONTENTID";
	private static final String COL_BLACKBOARD_TITLE = "BBTITLE";
	private static final String COL_BLACKBOARD_DESC = "BBDESC";
	private static final String COL_COURSE_NAME = "COURSENAME";
	private static final String COL_COURSE_ID = "COURSEID";
	private static final String COL_FOLDER_ID = "FOLDERID";
	private static final String COL_AVAILABLE = "AVAILABLE";
	private static final String COL_COURSE_AVAILABLE = "COURSEAVAILABLE";
	private static final String COL_DATE_ACCESSED = "DTACCESSED";
	private static final String COL_ATTACHMENT_NAME = "ATTNAME";

	public RegistrationUtil()
	{
		throw new Error();
	}

	public static void recordItem(ItemKey itemKey, boolean added, boolean available, boolean courseAvailable,
		String bbTitle, String bbDesc, String attachmentName, Date dateAdded, Date dateModifed, String courseName)
	{
		BbUtil.trace("recordItem(" + itemKey + ", " + added + ", " + available + ", " + courseAvailable + ", "
			+ bbTitle + ", " + bbDesc + ", " + attachmentName + ", " + dateAdded + ", " + dateModifed + ",  "
			+ courseName + ")");
		ensureDatabase();

		StringBuilder sql = null;

		final boolean oracle = getDatabase().isOracle();
		final String schema = SqlUtil.getSchema(oracle);

		final long now = System.currentTimeMillis();
		final java.sql.Timestamp addedTimestamp = new java.sql.Timestamp(dateAdded == null ? now : dateAdded.getTime());
		final java.sql.Timestamp modifiedTimestamp = new java.sql.Timestamp(dateModifed == null ? now
			: dateModifed.getTime());

		if( added )
		{
			if( oracle )
			{
				sql = insert(COL_DATABASE_ID, COL_DATE_CREATED, COL_DATE_MODIFIED, COL_INSTITUTION_URL, COL_UUID,
					COL_VERSION, COL_COURSE_ID, COL_FOLDER_ID, COL_AVAILABLE, COL_COURSE_AVAILABLE,
					COL_BLACKBOARD_TITLE, COL_BLACKBOARD_DESC, COL_PAGE, COL_CONTENT_ID, COL_COURSE_NAME,
					COL_ATTACHMENT_NAME).append(" VALUES (").append(schema)
					.append("equellaseq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?)");
			}
			else
			{
				sql = insert(COL_DATE_CREATED, COL_DATE_MODIFIED, COL_INSTITUTION_URL, COL_UUID, COL_VERSION,
					COL_COURSE_ID, COL_FOLDER_ID, COL_AVAILABLE, COL_COURSE_AVAILABLE, COL_BLACKBOARD_TITLE,
					COL_BLACKBOARD_DESC, COL_PAGE, COL_CONTENT_ID, COL_COURSE_NAME, COL_ATTACHMENT_NAME).append(
					" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}
			SqlUtil.runSql(sql.toString(), null, addedTimestamp, modifiedTimestamp, itemKey.getInstitutionUrl(),
				itemKey.getUuid(), itemKey.getVersion(), itemKey.getCourseId(), itemKey.getFolderId(), available,
				courseAvailable, bbTitle, trim(bbDesc, 511), itemKey.getPage(), itemKey.getContentId(), courseName,
				attachmentName);
			itemKey.setDatabaseId(0); // would be nice to know what the new one
										// is
		}
		else
		{
			String contentId = itemKey.getContentId();
			if( !Strings.isNullOrEmpty(contentId) )
			{
				BbUtil.debug("updating recorded usage using contentId");

				sql = update(COL_DATE_MODIFIED, COL_AVAILABLE, COL_COURSE_AVAILABLE, COL_BLACKBOARD_TITLE,
					COL_BLACKBOARD_DESC, COL_PAGE, COL_COURSE_NAME, COL_ATTACHMENT_NAME).append("WHERE CONTENTID=?");
				List<Integer> updateCount = SqlUtil.runSql(sql.toString(), null, modifiedTimestamp, available,
					courseAvailable, bbTitle, bbDesc, itemKey.getPage(), courseName, attachmentName, contentId);

				int updates = updateCount.get(0);
				if( updates > 1 )
				{
					// There have instances of dupes. Remove them all. This
					// should be a rare occurrence
					BbUtil.error("** duplicate recordings of " + contentId + " **", null);

					// remove dupes and add a brand new entry
					unrecordItem(0, contentId);
					recordItem(itemKey, true, available, courseAvailable, bbTitle, bbDesc, attachmentName, dateAdded,
						dateModifed, courseName);
				}
				else if( updates == 0 )
				{
					BbUtil.debug("content ID not logged... updating the existing one and record the content ID");

					sql = update(COL_DATE_MODIFIED, COL_AVAILABLE, COL_COURSE_AVAILABLE, COL_BLACKBOARD_TITLE,
						COL_BLACKBOARD_DESC, COL_PAGE, COL_COURSE_NAME, COL_CONTENT_ID, COL_ATTACHMENT_NAME).append(
						"WHERE INSTURL=? AND UUID=? AND VERSION=? AND COURSEID=? AND FOLDERID=?");
					updateCount = SqlUtil.runSql(sql.toString(), null, modifiedTimestamp, available, courseAvailable,
						bbTitle, trim(bbDesc, 511), itemKey.getPage(), courseName, itemKey.getContentId(),
						attachmentName, itemKey.getInstitutionUrl(), itemKey.getUuid(), itemKey.getVersion(),
						itemKey.getCourseId(), itemKey.getFolderId());
					checkSingleUpdate(updateCount);
				}
			}
			else
			{
				BbUtil.debug("updating recorded usage using old field matching");

				sql = update(COL_DATE_MODIFIED, COL_AVAILABLE, COL_COURSE_AVAILABLE, COL_BLACKBOARD_TITLE,
					COL_BLACKBOARD_DESC, COL_PAGE, COL_COURSE_NAME, COL_ATTACHMENT_NAME).append(
					"WHERE INSTURL=? AND UUID=? AND VERSION=? AND COURSEID=? AND FOLDERID=?");
				final List<Integer> updateCount = SqlUtil.runSql(sql.toString(), null, modifiedTimestamp, available,
					courseAvailable, bbTitle, trim(bbDesc, 511), itemKey.getPage(), courseName, attachmentName,
					itemKey.getInstitutionUrl(), itemKey.getUuid(), itemKey.getVersion(), itemKey.getCourseId(),
					itemKey.getFolderId());
				checkSingleUpdate(updateCount);
			}
		}
	}

	private static boolean checkSingleUpdate(List<Integer> updateCount)
	{
		final int updates = updateCount.get(0);
		if( updates != 1 )
		{
			// houston, we have a problem
			BbUtil.error("** Updated " + updates + "  rows when only one should have been updated **", null);
			return false;
		}
		return true;
	}

	private static boolean unrecordItem(int usageId)
	{
		BbUtil.debug("unrecordItem(" + usageId + ")");
		ensureDatabase();

		final List<Integer> updateCount = SqlUtil.runSql(delete() + "WHERE ID=?", null, usageId);
		return updateCount.get(0) > 0;
	}

	public static boolean unrecordItem(int usageId, String contentId)
	{
		BbUtil.debug("unrecordItem(" + usageId + ", " + contentId + ")");
		ensureDatabase();

		if( usageId != 0 )
		{
			boolean unrecorded = unrecordItem(usageId);
			if( unrecorded )
			{
				return true;
			}
		}

		final List<Integer> updateCount = SqlUtil.runSql(delete() + "WHERE CONTENTID=?", null, contentId);
		return updateCount.get(0) > 0;
	}

	public static boolean updateRecordedTitle(String contentId, String title, String description)
	{
		BbUtil.debug("updateRecordedTitle(" + contentId + ", " + title + ", " + description + ")");
		ensureDatabase();

		final List<Integer> updateCount = SqlUtil.runSql(
			update(COL_BLACKBOARD_TITLE, COL_BLACKBOARD_DESC).append("WHERE CONTENTID=?").toString(), null, title,
			description, contentId);
		return updateCount.get(0) > 0;
	}

	public static boolean updateRecordedLocation(String contentId, String courseId, String folderId)
	{
		BbUtil.debug("updateRecordedLocation(" + contentId + ", " + courseId + ", " + folderId + ")");
		ensureDatabase();

		final List<Integer> updateCount = SqlUtil.runSql(
			update(COL_COURSE_ID, COL_FOLDER_ID).append("WHERE CONTENTID=?").toString(), null, courseId, folderId,
			contentId);
		return updateCount.get(0) > 0;
	}

	public static boolean updateDateAccessed(String contentId, Date dateAccessed)
	{
		BbUtil.debug("updateDateAccessed(" + contentId + ", " + dateAccessed + ")");
		ensureDatabase();

		final Timestamp accessedTimestamp = new Timestamp(dateAccessed.getTime());
		final List<Integer> updateCount = SqlUtil.runSql(update(COL_DATE_ACCESSED).append("WHERE CONTENTID=?")
			.toString(), null, accessedTimestamp, contentId);
		return updateCount.get(0) > 0;
	}

	public static List<ItemInfo> findUsages(final String institutionUrl, final String itemUuid, final int itemVersion,
		boolean versionIsLatest, boolean allVersions, boolean available)
	{
		BbUtil.debug("findUsages(" + institutionUrl + ", " + itemUuid + ", " + itemVersion + ", " + allVersions + ", "
			+ available + ")");
		ensureDatabase();

		final StringBuilder sql = select().append("WHERE INSTURL=? AND UUID=?");
		if( !allVersions )
		{
			sql.append(" AND (VERSION=?");
			if( versionIsLatest )
			{
				sql.append(" OR VERSION=0");
			}
			sql.append(")");
		}
		if( available )
		{
			sql.append(" AND AVAILABLE=? AND COURSEAVAILABLE=?");
		}
		sql.append(" ORDER BY DTCREATED DESC");

		final List<ItemInfo> usages = SqlUtil.runSql(sql.toString(), new ResultProcessor<ItemInfo>()
		{
			@Override
			public List<ItemInfo> getResults(ResultSet results) throws SQLException
			{
				final List<ItemInfo> u = new ArrayList<ItemInfo>();
				while( results.next() )
				{
					final String contentId = results.getString(COL_CONTENT_ID);
					final ItemInfo r = new ItemInfo(institutionUrl, itemUuid, results.getInt(COL_VERSION), contentId,
						results.getString(COL_COURSE_ID), results.getString(COL_FOLDER_ID), results.getString(COL_PAGE));
					r.setCreatedDate(results.getTimestamp(COL_DATE_CREATED));
					r.setModifiedDate(results.getTimestamp(COL_DATE_MODIFIED));
					r.setAvailable(results.getBoolean(COL_AVAILABLE));
					r.setName(results.getString(COL_BLACKBOARD_TITLE));
					r.setDescription(results.getString(COL_BLACKBOARD_DESC));
					r.getItemKey().setDatabaseId(results.getInt(COL_DATABASE_ID));
					r.setAttachmentName(results.getString(COL_ATTACHMENT_NAME));
					u.add(r);
				}
				return u;
			}
		}, institutionUrl, itemUuid, new OptionalParam<Integer>(itemVersion, !allVersions), new OptionalParam<Boolean>(
			available, available), new OptionalParam<Boolean>(available, available));
		return usages;
	}

	public static List<ItemInfo> findAllUsages(final String institutionUrl, final String query, final String courseId,
		String folderId, boolean available, String sortColumn, boolean sortReverse)
	{
		BbUtil.debug("findAllUsages(" + institutionUrl + ", " + query + ", " + courseId + ", " + folderId + ", "
			+ available + ", " + sortColumn + ", " + sortReverse + ")");
		ensureDatabase();

		final StringBuilder sql = select().append(" WHERE INSTURL=?");

		final boolean hasQuery = !Strings.isNullOrEmpty(query);
		String likeQuery = null;
		if( hasQuery )
		{
			likeQuery = '%' + query.toLowerCase() + '%';
			sql.append(" AND LOWER(BBTITLE) LIKE ?");
		}
		final boolean hasCourseId = !Strings.isNullOrEmpty(courseId);
		if( hasCourseId )
		{
			sql.append(" AND COURSEID = ?");
		}
		final boolean hasFolderId = !Strings.isNullOrEmpty(folderId);
		if( hasFolderId )
		{
			sql.append(" AND FOLDERID = ?");
		}
		if( available )
		{
			sql.append(" AND AVAILABLE=? AND COURSEAVAILABLE=?");
		}

		String sortCol = "DTCREATED";
		String sortOrd = sortReverse ? "DESC" : "ASC";
		if( !Strings.isNullOrEmpty(sortColumn) )
		{
			if( sortColumn.equals("name") )
			{
				sortCol = "LOWER(BBTITLE)";
			}
			else if( sortColumn.equals("course") )
			{
				sortCol = "LOWER(COURSENAME)";
			}
		}
		sql.append(" ORDER BY " + sortCol + " " + sortOrd);

		//@formatter:off
			final List<ItemInfo> usages = SqlUtil.runSql(
				sql.toString(),
				new ResultProcessor<ItemInfo>()
				{
					@Override
					public List<ItemInfo> getResults(ResultSet results) throws SQLException
					{
						final List<ItemInfo> u = new ArrayList<ItemInfo>();
						while( results.next() )
						{
							final ItemInfo r = new ItemInfo(
								institutionUrl, 
								results.getString(COL_UUID),
								results.getInt(COL_VERSION), 
								results.getString(COL_CONTENT_ID),
								results.getString(COL_COURSE_ID), 
								results.getString(COL_FOLDER_ID), 
								results.getString(COL_PAGE)
								);
							r.setCreatedDate(results.getTimestamp(COL_DATE_CREATED));
							r.setModifiedDate(results.getTimestamp(COL_DATE_MODIFIED));
							r.setAvailable(results.getBoolean(COL_AVAILABLE));
							r.setName(results.getString(COL_BLACKBOARD_TITLE));
							r.setDescription(results.getString(COL_BLACKBOARD_DESC));
							r.getItemKey().setDatabaseId(results.getInt(COL_DATABASE_ID));
							r.setDateAccessed(results.getTimestamp(COL_DATE_ACCESSED));
							r.setAttachmentName(results.getString(COL_ATTACHMENT_NAME));
							u.add(r);
						}
						return u;
					}
				}, 
				institutionUrl, 
				new OptionalParam<String>(likeQuery, hasQuery),
				new OptionalParam<String>(courseId, hasCourseId), 
				new OptionalParam<String>(folderId,	hasFolderId),
				new OptionalParam<Boolean>(available, available), 
				new OptionalParam<Boolean>(available, available)
				);
			//@formatter:on
		return usages;
	}

	// Used by SynchroniseContentThread
	public static List<ItemKey> findEquellaContentByCourse(final String institutionUrl, final String courseId,
		boolean available)
	{
		BbUtil.debug("findEquellaContentByCourse(" + institutionUrl + ", " + courseId + ", " + available + ")");
		ensureDatabase();

		// ID, UUID, VERSION, FOLDERID, ATTACHMENT_NAME, CONTENTID
		final StringBuilder sql = select(COL_DATABASE_ID, COL_UUID, COL_VERSION, COL_FOLDER_ID, COL_PAGE,
			COL_CONTENT_ID).append("WHERE INSTURL=? AND COURSEID=?");
		if( available )
		{
			sql.append(" AND AVAILABLE=? AND COURSEAVAILABLE=?");
		}

		final List<ItemKey> usages = SqlUtil.runSql(sql.toString(), new ResultProcessor<ItemKey>()
		{
			@Override
			public List<ItemKey> getResults(ResultSet results) throws SQLException
			{
				List<ItemKey> u = new ArrayList<ItemKey>();
				while( results.next() )
				{
					final int dbId = results.getInt(COL_DATABASE_ID);
					final String uuid = results.getString(COL_UUID);
					final int version = results.getInt(COL_VERSION);
					final String folderId = results.getString(COL_FOLDER_ID);
					final String page = results.getString(COL_PAGE);
					final String contentId = results.getString(COL_CONTENT_ID);

					final ItemKey k = new ItemKey(institutionUrl, uuid, version, contentId, courseId, folderId, page);
					k.setDatabaseId(dbId);
					u.add(k);
				}
				return u;
			}
		}, institutionUrl, courseId, new OptionalParam<Boolean>(available, available), new OptionalParam<Boolean>(
			available, available));
		return usages;
	}

	public static int cleanupBadContent(final String institutionUrl)
	{
		BbUtil.debug("cleanupBadContent(" + institutionUrl + ")");
		ensureDatabase();

		String sql = delete() + "WHERE INSTURL=? AND CONTENTID IS NULL";
		List<Integer> updates = SqlUtil.runSql(sql, null, institutionUrl);
		return updates.get(0);
	}

	public static ContentRegisteredResponse contentIsRegistered(ItemKey itemKey)
	{
		BbUtil.debug("contentIsRegistered(" + itemKey + ")");
		ensureDatabase();

		final ResultProcessor<Object[]> resultProcessor = new ResultProcessor<Object[]>()
		{
			@Override
			public List<Object[]> getResults(ResultSet results) throws SQLException
			{
				List<Object[]> r = new ArrayList<Object[]>();
				while( results.next() )
				{
					r.add(new Object[]{results.getInt(COL_DATABASE_ID), results.getBoolean(COL_AVAILABLE),
							results.getString(COL_CONTENT_ID)});
				}
				return r;
			}
		};

		final List<Object[]> res;
		final StringBuilder sql = select(COL_DATABASE_ID, COL_AVAILABLE, COL_CONTENT_ID);
		if( itemKey.getContentId() != null )
		{
			sql.append("WHERE CONTENTID=?");
			res = SqlUtil.runSql(sql.toString(), resultProcessor, itemKey.getContentId());
		}
		else
		{
			// OLD STYLE
			final String page = itemKey.getPage();
			sql.append("WHERE INSTURL=? AND UUID=? AND VERSION=? AND COURSEID=? AND FOLDERID=? AND PAGE"
				+ (Strings.isNullOrEmpty(page) ? " IS NULL" : "=?"));

			res = SqlUtil.runSql(sql.toString(), resultProcessor, itemKey.getInstitutionUrl(), itemKey.getUuid(),
				itemKey.getVersion(), itemKey.getCourseId(), itemKey.getFolderId(), new OptionalParam<String>(page,
					!Strings.isNullOrEmpty(page)));
		}

		final ContentRegisteredResponse response = new ContentRegisteredResponse();
		if( !res.isEmpty() )
		{
			response.setRegistered(true);
			final Object[] r = res.get(0);
			response.setId((Integer) r[0]);
			response.setAvailable((Boolean) r[1]);
			response.setContentId((String) r[2]);
		}

		return response;
	}

	private static void ensureDatabase()
	{
		if( databaseVersion < 1 )
		{
			try
			{
				SqlUtil.runSql(SqlUtil.select("1").toString(), null);
				databaseVersion = 1;
			}
			catch( Exception e )
			{
				makeDatabase();
				databaseVersion = 1;
			}
		}

		if( databaseVersion < 2 )
		{
			if( !SqlUtil.columnExists(COL_CONTENT_ID) )
			{
				modifyDatabaseAddContentAndCourseName();
			}
			databaseVersion = 2;
		}

		if( databaseVersion < 3 )
		{
			if( !SqlUtil.columnExists(COL_BLACKBOARD_DESC) )
			{
				modifyDatabaseAddDescription();
			}
			databaseVersion = 3;
		}

		if( databaseVersion < 4 )
		{
			if( !SqlUtil.columnExists(COL_DATE_ACCESSED) )
			{
				modifyDatabaseAddDateAccessed();
			}
			databaseVersion = 4;
		}

		if( databaseVersion < 5 )
		{
			if( !SqlUtil.columnExists(COL_ATTACHMENT_NAME) )
			{
				modifyDatabaseAddAttachmentName();
			}
			databaseVersion = 5;
		}
	}

	/**
	 * Todo: this clearly isn't normalised. But the question is, is it worth it?
	 */
	private static void makeDatabase()
	{
		final String sql;
		if( getDatabase().isOracle() )
		{
			BbUtil.debug("Oracle DB detected");

			SqlUtil.runSql("CREATE SEQUENCE equellaseq START WITH 1 INCREMENT BY 1 NOMAXVALUE", null);

			//@formatter:off
				sql = "CREATE TABLE equellacontent (" 
					+ "ID NUMBER NOT NULL, "
					+ "INSTURL NVARCHAR2(255) NOT NULL, "
					+ "DTCREATED DATE DEFAULT SYSDATE, "
					+ "DTMODIFIED DATE DEFAULT SYSDATE, "
					+ "BBTITLE NVARCHAR2(1024) NOT NULL,"
					+ "PAGE NVARCHAR2(1024)," //nullable for item summaries
					+ "UUID NVARCHAR2(40) NOT NULL, "
					+ "VERSION NUMBER NOT NULL,"
					+ "COURSEID NVARCHAR2(32) NOT NULL, "
					+ "FOLDERID NVARCHAR2(32) NOT NULL, " 
					+ "AVAILABLE NUMBER NOT NULL,"
					+ "COURSEAVAILABLE NUMBER NOT NULL,"
					+ "CONSTRAINT equellacontent_pk PRIMARY KEY (ID))";
				//@formatter:on
			SqlUtil.runSql(sql, null);

			SqlUtil.runSql("CREATE INDEX eqcontent_uuidversion ON equellacontent(INSTURL, UUID, VERSION)", null);
			// cannot be UNIQUE since same item could be added to same folder
			// (in theory)
			SqlUtil.runSql("CREATE INDEX eqcontent_all ON equellacontent(INSTURL, UUID, VERSION, COURSEID, FOLDERID)",
				null);
			SqlUtil.runSql("CREATE INDEX eqcontent_course ON equellacontent(INSTURL, COURSEID)", null);
			SqlUtil.runSql("CREATE INDEX eqcontent_available ON equellacontent(AVAILABLE, COURSEAVAILABLE)", null);
		}
		else
		{
			BbUtil.debug("SQL Server DB detected");
			//@formatter:off
				sql = "CREATE TABLE dbo.equellacontent (" 
					+ "ID int NOT NULL PRIMARY KEY IDENTITY, " 
					+ "INSTURL nvarchar(255) NOT NULL, "
					+ "BBTITLE nvarchar(1024) NOT NULL, "
					+ "PAGE nvarchar(1024) NULL, " //nullable for item summaries
					+ "DTCREATED datetime NOT NULL, "
					+ "DTMODIFIED datetime NOT NULL, " 
					+ "UUID nvarchar(40) NOT NULL, "
					+ "VERSION int NOT NULL,"
					+ "COURSEID nvarchar(32) NOT NULL, "
					+ "FOLDERID nvarchar(32) NOT NULL, " 
					+ "AVAILABLE bit NOT NULL,"
					+ "COURSEAVAILABLE bit NOT NULL"
					+  ")";
				//@formatter:on
			SqlUtil.runSql(sql, null);

			SqlUtil.runSql("CREATE INDEX eqcontent_course ON dbo.equellacontent(INSTURL, COURSEID)", null);
			// cannot be UNIQUE since same item could be added to same folder
			// (in theory)
			SqlUtil.runSql(
				"CREATE INDEX eqcontent_all ON dbo.equellacontent(INSTURL, UUID, VERSION, COURSEID, FOLDERID)", null);
			SqlUtil.runSql("CREATE INDEX eqcontent_uuidversion ON dbo.equellacontent(INSTURL, UUID, VERSION)", null);
			SqlUtil.runSql("CREATE INDEX eqcontent_available ON dbo.equellacontent(AVAILABLE, COURSEAVAILABLE)", null);
		}
	}

	private static void modifyDatabaseAddContentAndCourseName()
	{
		if( getDatabase().isOracle() )
		{
			// debug("Oracle DB detected");
			//@formatter:off
				final String sql = "ALTER TABLE equellacontent ADD (" 
					+ "CONTENTID NVARCHAR2(32) NULL, "
					+ "COURSENAME NVARCHAR2(512) NULL"
					+ ")";
				//@formatter:on
			SqlUtil.runSql(sql, null);
			SqlUtil.runSql("CREATE INDEX eqcontent_content ON equellacontent(INSTURL, CONTENTID)", null);
		}
		else
		{
			// debug("SQL Server DB detected");
			//@formatter:off
				final String sql = "ALTER TABLE dbo.equellacontent ADD " 
					+ "CONTENTID nvarchar(32) NULL, "
					+ "COURSENAME nvarchar(512) NULL";
				//@formatter:on
			SqlUtil.runSql(sql, null);
			SqlUtil.runSql("CREATE INDEX eqcontent_content ON dbo.equellacontent(INSTURL, CONTENTID)", null);
		}
	}

	private static void modifyDatabaseAddDescription()
	{
		final String sql;
		if( getDatabase().isOracle() )
		{
			sql = "ALTER TABLE equellacontent ADD (BBDESC NVARCHAR2(512) NULL)";
		}
		else
		{
			sql = "ALTER TABLE dbo.equellacontent ADD BBDESC nvarchar(512) NULL";
		}
		SqlUtil.runSql(sql, null);
	}

	private static void modifyDatabaseAddDateAccessed()
	{
		final String sql;
		if( getDatabase().isOracle() )
		{
			sql = "ALTER TABLE equellacontent ADD (" + COL_DATE_ACCESSED + " DATE NULL)";
		}
		else
		{
			sql = "ALTER TABLE dbo.equellacontent ADD " + COL_DATE_ACCESSED + " datetime NULL";
		}
		SqlUtil.runSql(sql, null);
	}

	private static void modifyDatabaseAddAttachmentName()
	{
		final String sql;
		if( getDatabase().isOracle() )
		{
			sql = "ALTER TABLE equellacontent ADD (" + COL_ATTACHMENT_NAME + " NVARCHAR2(512) NULL)";
		}
		else
		{
			sql = "ALTER TABLE dbo.equellacontent ADD " + COL_ATTACHMENT_NAME + " nvarchar(512) NULL";
		}
		SqlUtil.runSql(sql, null);
	}

	private static BbDatabase getDatabase()
	{
		final DatabaseContainer dbContainer = (DatabaseContainer) PersistenceServiceFactory.getInstance()
			.getDbPersistenceManager().getContainer();
		return dbContainer.getBbDatabase();
	}

	private static String trim(String text, int maxlen)
	{
		if( text != null && text.length() > maxlen )
		{
			return text.substring(0, maxlen);
		}
		return text;
	}
}

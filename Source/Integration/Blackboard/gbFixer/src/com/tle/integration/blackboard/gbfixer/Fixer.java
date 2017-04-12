package com.tle.integration.blackboard.gbfixer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import blackboard.data.content.Content;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.platform.BbServiceManager;
import blackboard.platform.context.ContextManager;
import blackboard.platform.context.ContextManagerFactory;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.vxi.data.VirtualInstallation;

import com.tle.integration.blackboard.gbfixer.SqlUtil.ResultProcessor;

public class Fixer
{
	private volatile static Fixer instance;

	private final ContentDbLoader contentDbLoader;
	private final static String FIELD_MIME_TYPE = "mimeType";
	private static final List<String> GRADABLE_MIMES = Arrays.asList("equella/attachment-lti", "equella/qtitest",
		"equella/scorm-package");

	private boolean started = false;
	private boolean finished = false;
	private int removals = 0;
	private int count = 0;
	private int numResults = 0;
	private String error;

	public Fixer() throws PersistenceException
	{
		BbPersistenceManager bbPm = BbServiceManager.getPersistenceService().getDbPersistenceManager();
		contentDbLoader = (ContentDbLoader) bbPm.getLoader(ContentDbLoader.TYPE);
	}

	public static Fixer instance() throws Exception
	{
		if( instance != null )
		{
			return instance;
		}

		instance = new Fixer();
		return instance;
	}

	public synchronized void submit(HttpServletRequest request) throws Exception
	{
		if( !started )
		{
			this.started = true;
			Runnable runner = new Runnable()
			{
				@Override
				public void run()
				{
					EqGradeableItemDAO dao = new EqGradeableItemDAO();
					try
					{
						List<String> contents = SqlUtil.runSql(SqlUtil.select().toString(),
							new ResultProcessor<String>()
							{
								@Override
								public List<String> getResults(ResultSet results) throws SQLException
								{
									List<String> contentIds = new ArrayList<>();
									while( results.next() )
									{
										contentIds.add(results.getString("CONTENTID"));
									}
									return contentIds;
								}
							});
						numResults = contents.size();

						ContextManager context = ContextManagerFactory.getInstance();
						final VirtualInstallation vi = context.getContext().getVirtualInstallation();
						BbPersistenceManager bbPm = BbPersistenceManager.getInstance(vi);

						for( String contentId : contents )
						{
							count++;

							Id cid = bbPm.generateId(Content.DATA_TYPE, contentId);
							try
							{
								Content content = contentDbLoader.loadById(cid);
								//Be super super sure it's ours
								if( "resource/tle-resource".equals(content.getContentHandler()) )
								{
									String mimeType = content.getExtendedData().getValue(FIELD_MIME_TYPE);
									if( mimeType == null )
									{
										// someone dun goofed
										mimeType = content.getExtendedData().getValue(FIELD_MIME_TYPE.toLowerCase());
									}
									if( !GRADABLE_MIMES.contains(mimeType) )
									{
										List<GradableItem> items = dao.getForContentId(cid);
										if( items != null )
										{
											for( GradableItem gradableItem : items )
											{
												dao.deleteById(gradableItem.getId());
												removals++;
											}
										}
									}
								}
							}
							catch( KeyNotFoundException knfe )
							{
								//System.out.println("");
							}
						}
						finished = true;
					}
					catch( Exception e )
					{
						finished = false;
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						error = sw.toString();
					}
				}
			};
			new Thread(runner, "Gradebook fixer thread").start();
		}
	}

	public synchronized boolean hasStarted()
	{
		return started;
	}

	public synchronized boolean hasFinished()
	{
		return finished;
	}

	public synchronized int getCount()
	{
		return count;
	}

	public synchronized String getError()
	{
		return error;
	}

	public synchronized int getNumResults()
	{
		return numResults;
	}

	public synchronized int getRemovals()
	{
		return removals;
	}
}

package com.tle.integration.blackboard.linkfixer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import blackboard.base.BbList;
import blackboard.base.FormattedText;
import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.data.navigation.CourseToc;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.persist.content.ContentDbPersister;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.navigation.CourseTocDbLoader;
import blackboard.platform.BbServiceManager;
import blackboard.platform.config.BbConfig;
import blackboard.platform.context.ContextManager;
import blackboard.platform.log.LogService;
import blackboard.platform.plugin.PlugInConfig;
import blackboard.platform.plugin.PlugInException;
import blackboard.platform.plugin.PlugInUtil;
import blackboard.platform.vxi.data.VirtualHost;
import blackboard.platform.vxi.data.VirtualInstallation;
import blackboard.platform.vxi.service.VirtualInstallationManager;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class Fixer extends AbstractFixer
{
	public static final String EXECUTE = "execute";

	private volatile static Fixer instance;

	private final ContextManager context;
	protected final ContentDbLoader contentDbLoader;
	private final ContentDbPersister contentDbPersister;
	protected final CourseDbLoader courseDbLoader;
	protected final CourseTocDbLoader courseTocDbLoader;
	protected final LogService bbLogs;
	private final String bbUriStem;

	private int blackboardVersion;
	protected boolean completed;
	protected boolean started;
	protected boolean errored;
	protected int percent;
	protected int lookedAt;
	protected int equellaLookedAt;
	protected int fixedIds;
	protected int fixedUrls;

	public static final String EQUELLA_URL = "equellaurl";
	public static final String CONFIG_FILE = "config.properties";

	protected Fixer() throws Exception
	{
		super();

		BbServiceManager.initFromSystemProps();

		final VirtualInstallationManager vim = (VirtualInstallationManager) BbServiceManager
			.lookupService(VirtualInstallationManager.class);
		final VirtualHost vh = vim.getVirtualHost(""); //$NON-NLS-1$
		context = (ContextManager) BbServiceManager.lookupService(ContextManager.class);
		context.setContext(vh);

		bbUriStem = getBbUriStem(vim.getVirtualInstallationById(vh.getVirtualInstallationId()));

		bbLogs = BbServiceManager.getLogService();
		BbPersistenceManager bbPm = BbServiceManager.getPersistenceService().getDbPersistenceManager();
		contentDbLoader = (ContentDbLoader) bbPm.getLoader(ContentDbLoader.TYPE);
		contentDbPersister = (ContentDbPersister) bbPm.getPersister(ContentDbPersister.TYPE);
		courseDbLoader = (CourseDbLoader) bbPm.getLoader(CourseDbLoader.TYPE);
		courseTocDbLoader = (CourseTocDbLoader) bbPm.getLoader(CourseTocDbLoader.TYPE);
	}

	private final String getBbUriStem(VirtualInstallation vi)
	{
		String path = ""; //$NON-NLS-1$
		try
		{
			path = PlugInUtil.getUriStem(EQUELLA_BLOCK_VENDOR, EQUELLA_BLOCK_HANDLE, vi);
		}
		catch( final Exception t )
		{
			logMessage(0, "Error getting relative path " + t.getMessage()); //$NON-NLS-1$
		}

		// see Jira Defect TLE-996 :
		// http://apps.dytech.com.au/jira/browse/TLE-996
		// This is only a temporary fix. We can't assume the VI is bb_bb60.
		if( path.length() == 0 )
		{
			path = "/webapps/" + EQUELLA_BLOCK_VENDOR + "-" + EQUELLA_BLOCK_HANDLE + "-bb_bb60/";
		}
		return path;
	}

	@Override
	protected void finalize() throws Throwable
	{
		context.releaseContext();
		super.finalize();
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

	public void load()
	{
		// force re-evaluation of the Equella URL on page load
		// (it may be have been changed in the other Building Block since)
		setEquellaUrl("");
	}

	@Override
	public synchronized void submit(HttpServletRequest request) throws Exception
	{
		if( request.getParameter(EXECUTE) != null )
		{
			if( !started && !completed )
			{
				started = true;
				Runnable runner = new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							BbList courseList = courseDbLoader.loadAllCourses();
							int courseCount = courseList.size();
							for( int i = 0; i < courseCount; i++ )
							{
								percent = (int) (100.0 * i / courseCount);
								Course course = (Course) courseList.get(i);

								logMessage(0, "Looking at Course '" + course.getTitle() + "' ("
									+ course.getId().toExternalString() + ")");

								BbList courseTocs = courseTocDbLoader.loadByCourseId(course.getId());
								for( int j = 0; j < courseTocs.size(); j++ )
								{
									CourseToc courseToc = (CourseToc) courseTocs.get(j);

									logMessage(1, "Looking at CourseToc '" + courseToc.getLabel() + "' ("
										+ courseToc.getId().toExternalString() + ")");

									recurseContent(contentDbLoader, courseToc,
										getChildren(contentDbLoader, courseToc.getContentId()), 2);
								}
							}
							started = false;
							completed = true;
							errored = false;
						}
						catch( Exception e )
						{
							started = false;
							completed = false;
							errored = true;
							logMessage(0, "---------------------------------------------------");
							logMessage(0, "An error occurred: " + e.getMessage());
							logMessage(0, "See Blackboard service logs for details");
							bbLogs.logError("An error occurred trying to fix EQUELLA content links", e);
						}
					}
				};
				new Thread(runner, "Link Fixer Thread").start();
			}
		}
	}

	protected void recurseContent(ContentDbLoader contentLoader, CourseToc courseToc, BbList contentList, int level)
		throws Exception
	{
		for( int j = 0; j < contentList.size(); j++ )
		{
			Content content = (Content) contentList.get(j);
			logMessage(level, "Looking at " + contentDisplay(content));
			lookedAt++;

			String handler = content.getContentHandler();
			if( handler.equals("resource/tle-resource") || handler.equals("resource/tle-myitem")
				|| handler.equals("resource/tle-plan") )
			{
				logMessage(level, "Handleable type " + handler);
				fixContent(courseToc, content, level);
			}
			recurseContent(contentLoader, courseToc, getChildren(contentLoader, content.getId()), level + 1);
		}
	}

	protected BbList getChildren(ContentDbLoader contentLoader, Id bbContentId) throws PersistenceException
	{
		return contentLoader.loadChildren(bbContentId);
	}

	private void fixContent(CourseToc courseToc, Content content, int level) throws Exception
	{
		equellaLookedAt++;

		FormattedText body = content.getBody();

		// need to find and replace in text the values of course_id=nnn and
		// content_id=nnn
		FixTextFeedback feedback = new FixTextFeedback();
		final String newText = fixText(body.getText(), feedback);

		if( feedback.fixedHardCodedIds )
		{
			logMessage(level, "*** Hard coded content IDs on " + contentDisplay(content) + " was fixed. ***");
			fixedIds++;
		}
		if( feedback.fixedNonTokenedUrls )
		{
			logMessage(level, "*** Non ViewContent url on " + contentDisplay(content) + " was fixed. ***");
			fixedUrls++;
		}
		content.setBody(new FormattedText(newText, FormattedText.Type.HTML));

		// persist it
		contentDbPersister.persist(content);
	}

	private String contentDisplay(Content content)
	{
		return "'" + content.getTitle() + "' (" + content.getId().toExternalString() + ")";
	}

	/**
	 * This is a rather hacky way to get the version.
	 * 
	 * @return
	 */
	@Override
	protected int getBlackboardVersion()
	{
		if( blackboardVersion == 0 )
		{
			String vers = BbServiceManager.getConfigurationService().getBbProperty(BbConfig.LIBRARY_VERSION);
			// don't use vers.contains (not in Java 1.4)
			if( vers.indexOf('.') > -1 ) //$NON-NLS-1$
			{
				vers = vers.split("\\.")[0]; //$NON-NLS-1$
			}
			try
			{
				blackboardVersion = Integer.parseInt(vers);
			}
			catch( NumberFormatException nfe )
			{
				blackboardVersion = 6;
			}
		}
		return blackboardVersion;
	}

	public String getStatus()
	{
		if( errored )
		{
			return "An error occurred when trying to fix EQUELLA links.  See Blackboard log " + bbLogs.getLogFileName()
				+ " for more details.";
		}
		else if( completed )
		{
			return "Execution of fixer has finished!  Looked at " + lookedAt + " items (" + equellaLookedAt
				+ " EQUELLA items) and fixed " + fixedIds + " hard coded IDs and " + fixedUrls
				+ " non ViewContent urls.  The building block can now be safely removed.";
		}
		else if( hasStarted() )
		{
			return "Execution of fixer has started.  Approx " + percent + " complete.";
		}
		else
		{
			return "Ready to start.  It is <b>HIGHLY</b> recommended that you backup your Blackboard database before pressing the Submit button.";
		}
	}

	@Override
	protected void logMessage(int lvl, String msg)
	{
		super.logMessage(lvl, msg);
		bbLogs.logWarning("[EQUELLA Link Fixer] " + msg);
	}

	public synchronized boolean hasStarted()
	{
		return started;
	}

	public boolean hasCompleted()
	{
		return completed;
	}

	@Override
	public synchronized String getEquellaUrl()
	{
		if( equellaUrl.length() == 0 )
		{
			File configFile = new File(getConfigDirectory(), CONFIG_FILE);
			if( !configFile.exists() )
			{
				throw new RuntimeException("Cannot find EQUELLA Plugin Building Block configuration file");
			}

			Properties props = new Properties();
			try( InputStream inStream = new FileInputStream(configFile) )
			{
				props.load(inStream);
			}
			catch( Exception e )
			{
				throw new RuntimeException("Error loading configuration", e);
			}
			if( props.containsKey(EQUELLA_URL) )
			{
				equellaUrl = props.getProperty(EQUELLA_URL);
				EQUELLA_URL_REGEX = null;
			}
			else
			{
				throw new RuntimeException("EQUELLA Url in EQUELLA Plugin Building Block is not set");
			}
		}
		return equellaUrl;
	}

	private synchronized void setEquellaUrl(String equellaUrl)
	{
		this.equellaUrl = equellaUrl;
	}

	private File getConfigDirectory()
	{
		try
		{
			return new PlugInConfig(EQUELLA_BLOCK_VENDOR, EQUELLA_BLOCK_HANDLE).getConfigDirectory();
		}
		catch( PlugInException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String getRelativePath()
	{
		return bbUriStem;
	}
}

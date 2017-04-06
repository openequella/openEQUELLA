package com.tle.blackboard.common;

import blackboard.persist.BbPersistenceManager;
import blackboard.platform.context.ContextManager;
import blackboard.platform.context.ContextManagerFactory;
import blackboard.platform.vxi.data.VirtualInstallation;

import com.google.common.base.Throwables;

/**
 * @author Aaron
 */
// @NonNullByDefault
public class BbContext
{
	/* @Nullable */
	private static BbContext instance;
	private static final Object instanceLock = new Object();

	private final ContextManager context;
	private final BbPersistenceManager bbPm;

	@SuppressWarnings("null")
	public static BbContext instance()
	{
		if( instance == null )
		{
			synchronized( instanceLock )
			{
				if( instance == null )
				{
					instance = new BbContext();
				}
			}
		}
		return instance;
	}

	@SuppressWarnings("nls")
	private BbContext()
	{
		try
		{
			context = ContextManagerFactory.getInstance();
			final VirtualInstallation vi = context.getContext().getVirtualInstallation();
			bbPm = BbPersistenceManager.getInstance(vi);
		}
		catch( Exception e )
		{
			BbUtil.error("Couldn't init BbContext", e);
			throw Throwables.propagate(e);
		}
	}

	public ContextManager getContextManager()
	{
		return context;
	}

	public BbPersistenceManager getPersistenceManager()
	{
		return bbPm;
	}
}

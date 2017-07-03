package com.tle.common.usermanagement.user;

import java.util.Stack;

import org.apache.log4j.NDC;

import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;

/**
 * Copies over security context from a calling thread to the new thread.
 * 
 * @author Nicholas Read
 */
public abstract class AuthenticatedThread extends Thread
{
	private UserState callingThreadsAuthentication;
	private Institution callingThreadsInstitution;
	private Stack<?> loggingContext;

	public AuthenticatedThread()
	{
		super();
		setup();
	}

	public AuthenticatedThread(String name)
	{
		super(name);
		setup();
	}

	public AuthenticatedThread(ThreadGroup group, String name)
	{
		super(group, name);
		setup();
	}

	private void setup()
	{
		callingThreadsAuthentication = CurrentUser.getUserState();
		callingThreadsInstitution = CurrentInstitution.get();
		loggingContext = NDC.cloneStack();
	}

	@Override
	public final void run()
	{
		try
		{
			NDC.inherit(loggingContext);
			CurrentUser.setUserState(callingThreadsAuthentication);
			CurrentInstitution.set(callingThreadsInstitution);
			doRun();
		}
		finally
		{
			CurrentInstitution.remove();
			CurrentUser.setUserState(null);
			NDC.remove();
		}
	}

	public abstract void doRun();
}

package com.dytech.installer.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.dytech.installer.InstallerException;
import com.dytech.installer.Progress;
import com.dytech.installer.TaskListener;

public abstract class Command
{
	private Collection taskListeners;
	private boolean mandatory = true;
	private Progress progress;

	public Command()
	{
		taskListeners = new ArrayList();
	}

	public boolean isMandatory()
	{
		return mandatory;
	}

	public void setMandatory(boolean b)
	{
		mandatory = b;
	}

	public void addTaskListener(TaskListener l)
	{
		taskListeners.add(l);
	}

	public void removeTaskListener(TaskListener l)
	{
		taskListeners.remove(l);
	}

	public abstract void execute() throws InstallerException;

	@Override
	public abstract String toString();

	protected void propogateTaskStarted(int subtasks)
	{
		Iterator i = taskListeners.iterator();
		while( i.hasNext() )
		{
			((TaskListener) i.next()).taskStarted(subtasks);
		}
	}

	protected void propogateTaskCompleted()
	{
		Iterator i = taskListeners.iterator();
		while( i.hasNext() )
		{
			((TaskListener) i.next()).taskCompleted();
		}
	}

	protected void propogateSubtaskCompleted()
	{
		Iterator i = taskListeners.iterator();
		while( i.hasNext() )
		{
			((TaskListener) i.next()).subtaskCompleted();
		}
	}

	/**
	 * @return Returns the progress.
	 */
	public Progress getProgress()
	{
		return progress;
	}

	/**
	 * @param progress The progress to set.
	 */
	public void setProgress(Progress progress)
	{
		this.progress = progress;
	}
}
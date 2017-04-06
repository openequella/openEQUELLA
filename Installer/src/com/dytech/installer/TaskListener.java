package com.dytech.installer;

public interface TaskListener
{
	public void taskStarted(int subtasks);

	public void taskCompleted();

	public void subtaskCompleted();
}
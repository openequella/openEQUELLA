package com.tle.core.events;

import java.io.Serializable;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public abstract class ApplicationEvent<T extends ApplicationListener> implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum PostTo
	{
		POST_ONLY_TO_SELF, POST_TO_ALL_CLUSTER_NODES, POST_TO_OTHER_CLUSTER_NODES, POST_TO_SELF_SYNCHRONOUSLY
	}

	private final PostTo postTo;

	public ApplicationEvent(PostTo postTo)
	{
		this.postTo = postTo;
	}

	public PostTo getPostTo()
	{
		return postTo;
	}

	public boolean requiresInstitution()
	{
		return false;
	}

	public abstract Class<T> getListener();

	public abstract void postEvent(T listener);
}

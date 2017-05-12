package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SimpleSectionId;

@NonNullByDefault
public abstract class AbstractDirectEvent implements SectionEvent<EventListener>
{
	protected final int priority;
	protected final SectionId forSectionId;

	private boolean abortProcessing;

	public AbstractDirectEvent(int priority, SectionId forSectionId)
	{
		this.priority = priority;
		this.forSectionId = forSectionId;
	}

	public AbstractDirectEvent(int priority, String forSectionId)
	{
		this(priority, new SimpleSectionId(forSectionId));
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public SectionId getForSectionId()
	{
		return forSectionId;
	}

	@NonNullByDefault(false)
	@Override
	public int compareTo(SectionEvent<EventListener> o)
	{
		return o.getPriority() - getPriority();
	}

	@Nullable
	@Override
	public String getListenerId()
	{
		return null;
	}

	@Nullable
	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return null;
	}

	@Override
	public void beforeFiring(SectionInfo info, @Nullable SectionTree tree)
	{
		// nothing
	}

	@Override
	public void finishedFiring(SectionInfo info, @Nullable SectionTree tree)
	{
		// nothing
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, @Nullable EventListener listener) throws Exception
	{
		fireDirect(sectionId, info);
	}

	public abstract void fireDirect(SectionId sectionId, SectionInfo info) throws Exception;

	@Override
	public boolean isStopProcessing()
	{
		return false;
	}

	@Override
	public void stopProcessing()
	{
		// no others get called
	}

	@Override
	public void abortProcessing()
	{
		abortProcessing = true;
	}

	@Override
	public boolean isAbortProcessing()
	{
		return abortProcessing;
	}

	@Override
	public boolean isContinueAfterException()
	{
		return false;
	}
}

package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

@NonNullByDefault
public abstract class WrappedEvent implements SectionEvent<EventListener>
{
	private SectionEvent<EventListener> inner;

	@SuppressWarnings("unchecked")
	public WrappedEvent(SectionEvent<?> inner)
	{
		this.inner = (SectionEvent<EventListener>) inner;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, @Nullable EventListener listener) throws Exception
	{
		prefire(sectionId, info, listener);
		inner.fire(sectionId, info, listener);
	}

	protected abstract void prefire(SectionId sectionId, SectionInfo info, @Nullable EventListener listener);

	@Nullable
	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return inner.getListenerClass();
	}

	@Override
	public void abortProcessing()
	{
		inner.abortProcessing();
	}

	@Override
	public void beforeFiring(SectionInfo info, @Nullable SectionTree tree)
	{
		inner.beforeFiring(info, tree);
	}

	@NonNullByDefault(false)
	@Override
	public int compareTo(SectionEvent<EventListener> o)
	{
		return inner.compareTo(o);
	}

	@Override
	public void finishedFiring(SectionInfo info, @Nullable SectionTree tree)
	{
		inner.finishedFiring(info, tree);
	}

	@Override
	public SectionId getForSectionId()
	{
		return inner.getForSectionId();
	}

	@Nullable
	@Override
	public String getListenerId()
	{
		return inner.getListenerId();
	}

	@Override
	public int getPriority()
	{
		return inner.getPriority();
	}

	@Override
	public boolean isAbortProcessing()
	{
		return inner.isAbortProcessing();
	}

	@Override
	public boolean isStopProcessing()
	{
		return inner.isStopProcessing();
	}

	@Override
	public void stopProcessing()
	{
		inner.stopProcessing();
	}

	@Override
	public boolean isContinueAfterException()
	{
		return inner.isContinueAfterException();
	}
}

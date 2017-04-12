package com.tle.web.sections.equella.impl;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.name.Named;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.AbstractModalSessionServiceImpl;
import com.tle.web.sections.equella.ModalSession;
import com.tle.web.sections.equella.ModalSessionCallback;
import com.tle.web.sections.equella.ModalSessionService;

/**
 * @author aholland
 */
@Bind(ModalSessionService.class)
@Singleton
public class ModalSessionServiceImpl extends AbstractModalSessionServiceImpl<ModalSession>
	implements
		ModalSessionService
{
	@Inject
	@Named("modalTree")
	private SectionTree modalTree;

	@Override
	public SectionInfo createForward(SectionInfo original, String path, ModalSessionCallback finished)
	{
		final SectionInfo forward = original.createForward(path,
			Collections.singletonMap((Object) ModalSession.KEY_IGNORE_CURRENT_SESSION, (Object) true));
		original.setAttribute(SectionInfo.KEY_ORIGINALINFO, forward);

		ModalSession session = new ModalSession(finished);
		setupModalSession(forward, session);
		return forward;
	}

	@Override
	public void setupModalSession(SectionInfo info, ModalSession session)
	{
		doSetupModalSession(info, session, RootModalSessionSection.class, ModalSession.class);
	}

	@Override
	public void returnFromSession(SectionInfo info)
	{
		if( info.isRendered() )
		{
			return;
		}
		final ModalSession session = getCurrentSession(info);
		final ModalSessionCallback callback = session.getFinishedCallback();
		if( callback != null )
		{
			callback.executeModalFinished(info, session);
		}
		else
		{
			throw new RuntimeException("Modal session callback required"); //$NON-NLS-1$
		}
	}

	@Override
	protected SectionTree createFilterTree()
	{
		return modalTree;
	}

	@Override
	public ModalSession getCurrentSession(SectionInfo info)
	{
		return info.getAttribute(ModalSession.class);
	}
}

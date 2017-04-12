package com.tle.web.sections.equella;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.AbstractSectionFilter;

/**
 * @author aholland
 */
@NonNullByDefault
public abstract class AbstractModalSessionServiceImpl<S extends ModalSession> extends AbstractSectionFilter
{
	protected SectionTree tree;

	@Inject
	protected UserSessionService sessionService;

	protected final void doSetupModalSession(SectionInfo info, S session,
		Class<? extends AbstractRootModalSessionSection<?>> sectionClass, Class<S> sessionClass)
	{
		final AbstractRootModalSessionSection<?> root = info.lookupSection(sectionClass);
		if( root == null )
		{
			throw new Error("No AbstractRootModalSessionSection found in trees");
		}
		String sessionId = null;
		if( session != null )
		{
			sessionId = sessionService.createUniqueKey();
			sessionService.setAttribute(sessionId, session);
		}
		info.setAttribute(sessionClass, session);
		root.setSessionId(info, sessionId);
	}

	@Override
	protected SectionTree getFilterTree()
	{
		synchronized( this )
		{
			if( tree == null )
			{
				tree = createFilterTree();
			}
			return tree;
		}
	}

	protected SectionTree createFilterTree()
	{
		throw new Error("Needs lookup-method"); //$NON-NLS-1$
	}

	@Nullable
	public abstract S getCurrentSession(SectionInfo info);
}

package com.tle.web.errors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.registry.TreeRegistry;
import com.tle.web.template.Decorations;

@Bind
@Singleton
public class DefaultExceptionHandler extends AbstractExceptionHandler
{
	private static final String ERRORTREE_KEY = "/error.do"; //$NON-NLS-1$
	@Inject
	private TreeRegistry treeRegistry;

	protected SectionInfo createNewInfo(Throwable exception, SectionInfo info, SectionsController controller)
	{
		SectionTree errorTree = treeRegistry.getTreeForPath(ERRORTREE_KEY);
		MutableSectionInfo newInfo = controller.createInfoFromTree(errorTree, info);
		newInfo.preventGET();
		newInfo.setAttribute(SectionInfo.KEY_ORIGINAL_EXCEPTION, exception);
		newInfo.setAttribute(SectionInfo.KEY_MATCHED_EXCEPTION, getFirstCause(exception));
		newInfo.setErrored();
		Decorations.setDecorations(newInfo, Decorations.getDecorations(info));
		return newInfo;
	}

	protected boolean checkRendered(SectionInfo info)
	{
		if( !info.isRendered() )
		{
			info.setRendered();
		}
		else if( info.getResponse().isCommitted() )
		{
			return true;
		}
		return false;
	}

	@Override
	public void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event)
	{
		if( event != null )
		{
			SectionUtils.throwRuntime(exception);
		}
		if( checkRendered(info) )
		{
			return;
		}
		markHandled(info);
		SectionInfo newInfo = createNewInfo(exception, info, controller);
		controller.execute(newInfo);
	}
}

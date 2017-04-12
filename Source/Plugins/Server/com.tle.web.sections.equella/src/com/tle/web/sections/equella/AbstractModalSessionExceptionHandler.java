package com.tle.web.sections.equella;

import com.tle.web.errors.AbstractExceptionHandler;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.events.SectionEvent;

/**
 * @author aholland
 */
public abstract class AbstractModalSessionExceptionHandler<S extends ModalSession> extends AbstractExceptionHandler
{
	public static final String MODAL_ERROR_KEY = "$MODAL_ERROR$"; //$NON-NLS-1$

	@Override
	public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		return super.canHandle(info, ex, event)
			&& getModalService().getCurrentSession(SectionUtils.getOriginalInfo(info)) != null && shouldHandle(info);
	}

	protected boolean shouldHandle(SectionInfo info)
	{
		return true;
	}

	@Override
	public void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event)
	{
		markHandled(info);
		info.setAttribute(MODAL_ERROR_KEY, getFirstCause(exception));
		info.setErrored();
		info.preventGET();
		info.renderNow();
	}

	protected abstract AbstractModalSessionServiceImpl<S> getModalService();
}

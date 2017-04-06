package com.tle.web.activation.section;

import javax.inject.Inject;

import com.tle.core.activation.service.ActivationService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.SimpleSectionResult;

@Bind
public class DeactivateWebService extends AbstractPrototypeSection<DeactivateWebService.DeactivateModel>
{
	@Inject
	private ActivationService actService;

	@SuppressWarnings("nls")
	@DirectEvent
	public void deactive(SectionInfo info)
	{
		try
		{
			String activationUuid = getModel(info).getActivationUuid();
			actService.deactivateByUuid(activationUuid);
			info.getRootRenderContext().setRenderedResponse(new SimpleSectionResult("OK"));
		}
		catch( Exception e )
		{
			info.getRootRenderContext().setRenderedResponse(new SimpleSectionResult("ERROR" + ' ' + e.getMessage()));
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public Class<DeactivateModel> getModelClass()
	{
		return DeactivateModel.class;
	}

	public static class DeactivateModel
	{
		@Bookmarked
		private String activationUuid;

		public String getActivationUuid()
		{
			return activationUuid;
		}

		public void setActivationUuid(String activationUuid)
		{
			this.activationUuid = activationUuid;
		}
	}
}

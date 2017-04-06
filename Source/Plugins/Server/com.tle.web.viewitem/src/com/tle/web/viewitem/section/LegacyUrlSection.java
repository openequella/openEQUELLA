package com.tle.web.viewitem.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.viewitem.DRMFilter;

public class LegacyUrlSection extends AbstractPrototypeSection<Object> implements ParametersEventListener
{
	public boolean canView(SectionInfo info) throws Exception
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "legacyurl"; //$NON-NLS-1$
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		String convert = event.getParameter("convert", false); //$NON-NLS-1$
		if( convert != null )
		{
			ConversionSection conversion = info.lookupSection(ConversionSection.class);
			conversion.setConvert(info, convert);
		}

		if( event.getBooleanParameter("preview", false) ) //$NON-NLS-1$
		{
			DRMFilter drm = info.lookupSection(DRMFilter.class);
			drm.setSkip(info, true);
		}
	}
}

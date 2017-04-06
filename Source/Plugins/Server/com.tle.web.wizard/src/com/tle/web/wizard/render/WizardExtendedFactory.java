package com.tle.web.wizard.render;

import java.io.Writer;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.wizard.controls.WebControl;

@NonNullByDefault
public class WizardExtendedFactory extends ExtendedFreemarkerFactory
{
	@Override
	protected void addRootObjects(Map<String, Object> map, FreemarkerSectionResult result, Writer writer)
	{
		super.addRootObjects(map, result, writer);
		SectionId sectionId = result.getSectionId();
		if( sectionId != null && writer instanceof SectionWriter )
		{
			SectionId section = ((SectionWriter) writer).getSectionForId(sectionId);
			if( section instanceof WebControl )
			{
				WebControl webControl = (WebControl) section;
				map.put("wc", webControl.getWrappedControl()); //$NON-NLS-1$
				map.put("c", webControl); //$NON-NLS-1$
				map.put("fid", webControl.getFormName()); //$NON-NLS-1$
			}
		}
	}
}

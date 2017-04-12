package com.tle.web.freemarker.methods;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;

import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class SectionsTemplateModel implements TemplateModel
{

	public SectionWriter getSectionWriter()
	{
		try
		{
			AdapterTemplateModel model = (AdapterTemplateModel) Environment.getCurrentEnvironment().getGlobalVariable(
				"_info");
			if( model != null )
			{
				return (SectionWriter) model.getAdaptedObject(SectionWriter.class);
			}
			return null;
		}
		catch( TemplateModelException e )
		{
			throw new SectionsRuntimeException(e);
		}
	}
}

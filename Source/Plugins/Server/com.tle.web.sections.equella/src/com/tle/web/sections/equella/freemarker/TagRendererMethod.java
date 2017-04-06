package com.tle.web.sections.equella.freemarker;

import java.util.List;

import com.tle.web.freemarker.methods.SectionsTemplateModel;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.AbstractRenderedComponent;
import com.tle.web.sections.standard.model.HtmlComponentState;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class TagRendererMethod extends SectionsTemplateModel implements TemplateMethodModelEx
{
	public TagRendererMethod()
	{
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object exec(List list) throws TemplateModelException
	{
		RenderContext info = getSectionWriter();
		Object section = list.get(0);
		String tag = list.get(1).toString();
		if( section instanceof AdapterTemplateModel )
		{
			Object wrapped = ((AdapterTemplateModel) section).getAdaptedObject(Object.class);
			if( wrapped instanceof HtmlComponentState )
			{
				return new TagRenderer(tag, ((HtmlComponentState) wrapped));
			}
			if( wrapped instanceof SectionId )
			{
				SectionId sectionId = (SectionId) wrapped;
				AbstractRenderedComponent<HtmlComponentState> component = (AbstractRenderedComponent<HtmlComponentState>) info
					.getSectionForId(sectionId);
				component.setRendererType(info, "null"); //$NON-NLS-1$
				ResultListCollector results = new ResultListCollector(false);
				try
				{
					info.processEvent(new RenderEvent(info, sectionId.getSectionId(), results));
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
				HtmlComponentState state = component.getState(info);
				TagRenderer renderer = new TagRenderer(tag, state);
				state.fireRendererCallback(info, renderer);
				return renderer;
			}
		}
		throw new SectionsRuntimeException("Needs to be a HtmlComponentState or AbstractRenderedComponent"); //$NON-NLS-1$
	}
}

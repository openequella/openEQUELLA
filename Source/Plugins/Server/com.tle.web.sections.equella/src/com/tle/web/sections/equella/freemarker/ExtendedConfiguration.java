package com.tle.web.sections.equella.freemarker;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.SectionsConfiguration;
import com.tle.web.sections.standard.RendererFactory;

@Bind
@Singleton
public class ExtendedConfiguration extends SectionsConfiguration
{
	@Inject
	private RendererFactory factory;

	@Override
	@PostConstruct
	protected void setup()
	{
		super.setup();
		setSharedVariable("_choose", new ChooseRenderer(factory)); //$NON-NLS-1$
		setSharedVariable("_render", new RenderDirective(factory)); //$NON-NLS-1$
		setSharedVariable("_tagrenderer", new TagRendererMethod()); //$NON-NLS-1$
	}
}

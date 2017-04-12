package com.tle.web.sections.standard;

/**
 * Renderers that need the render factory to render child components can
 * implement this. A good example of this is the Shuffle Box control that needs
 * to render buttons in the middle; however, the default button renderer may be
 * over-ridden, so it asks the RenderFactory to do the work for it.
 * 
 * @author Nick
 */
public interface RendererFactoryAware
{
	void setRenderFactory(RendererFactory rendererFactory);
}

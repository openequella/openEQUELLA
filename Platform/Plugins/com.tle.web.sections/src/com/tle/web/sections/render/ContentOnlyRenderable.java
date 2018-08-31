package com.tle.web.sections.render;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

import java.io.IOException;

public class ContentOnlyRenderable implements NestedRenderable {

    private SectionRenderable nestedRenderable;

    @Override
    public NestedRenderable setNestedRenderable(SectionRenderable nested)
    {
        this.nestedRenderable = nested;
        return this;
    }

    @Override
    public SectionRenderable getNestedRenderable()
    {
        return nestedRenderable;
    }

    @Override
    public void realRender(SectionWriter writer) throws IOException
    {
        writer.render(nestedRenderable);
    }

    @Override
    public void preRender(PreRenderContext info)
    {
        info.preRender(nestedRenderable);
    }
}

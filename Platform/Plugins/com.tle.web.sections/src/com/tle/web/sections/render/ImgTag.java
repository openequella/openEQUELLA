package com.tle.web.sections.render;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;

@SuppressWarnings("nls")
public class ImgTag extends TagRenderer
{
	private final String srcUrl;

	public ImgTag(TagState tagState, String srcUrl)
	{
		super("img", tagState);
		this.srcUrl = srcUrl;
	}

	public ImgTag(String srcUrl)
	{
		super("img", new TagState());
		this.srcUrl = srcUrl;
	}

	@Override
	protected Map<String, String> prepareAttributes(SectionWriter writer) throws IOException
	{
		Map<String, String> as = super.prepareAttributes(writer);
		as.put("src", srcUrl);
		return as;
	}
}

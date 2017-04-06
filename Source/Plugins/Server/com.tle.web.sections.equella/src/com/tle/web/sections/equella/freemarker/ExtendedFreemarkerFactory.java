package com.tle.web.sections.equella.freemarker;

import java.io.Writer;
import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.freemarker.PluginFreemarkerFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.PathGenerator;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.render.TextUtils;
import com.tle.web.sections.result.util.BundleWriter;
import com.tle.web.sections.result.util.HeaderUtils;

@SuppressWarnings("nls")
@NonNullByDefault
public class ExtendedFreemarkerFactory extends PluginFreemarkerFactory
{
	@Inject
	private BundleCache bundleCache;

	@Inject
	public void setConfiguration(ExtendedConfiguration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	protected void addRootObjects(Map<String, Object> map, FreemarkerSectionResult result, Writer writer)
	{
		super.addRootObjects(map, result, writer);
		map.put("b", new BundleWriter(pluginId, bundleCache));
		map.put("t", TextUtils.INSTANCE);
		map.put("currentUser", CurrentUser.getUserState());
		if( writer instanceof SectionWriter )
		{
			SectionWriter sWriter = (SectionWriter) writer;
			map.put("head", new HeaderUtils(sWriter));
			PathGenerator pathGen = sWriter.getPathGenerator();
			map.put("baseHref", pathGen.getBaseHref(sWriter).toString());
		}
	}
}

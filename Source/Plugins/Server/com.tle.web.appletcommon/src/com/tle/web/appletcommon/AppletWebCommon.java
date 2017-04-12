package com.tle.web.appletcommon;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class AppletWebCommon
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(AppletWebCommon.class);
	public static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/writeapplet.js"));
	public static final String PARAMETER_PREFIX = "jnlp.";

	/**
	 * writeAppletTags
	 * 
	 * @param placeholder $('#somediv')
	 * @param jarurl 'http://[]/p/r/com.tle.web.recipientapplet/selector.jar'
	 * @param mainclass 'com.tle.web.myapplet.AppletLauncher'
	 * @param locale 'en-AU'
	 * @param endpoint 'http://lebowski:8080/Shiny/my/'
	 * @param height 300px
	 * @param width 50px
	 * @param options ObjectExpression
	 * @param id applet ID
	 */
	public static final ExternallyDefinedFunction WRITE_APPLET = new ExternallyDefinedFunction("writeAppletTags",
		INCLUDE);

	private AppletWebCommon()
	{
		throw new Error();
	}
}
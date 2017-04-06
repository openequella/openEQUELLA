package com.tle.web.htmleditor.tinymce;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.BasicFreemarkerFactory;

/**
 * @author Aaron
 */
@Bind(BasicFreemarkerFactory.class)
@Singleton
public class TinyMceAddOnFreemarkerFactory extends BasicFreemarkerFactory
{
	// nothing
}

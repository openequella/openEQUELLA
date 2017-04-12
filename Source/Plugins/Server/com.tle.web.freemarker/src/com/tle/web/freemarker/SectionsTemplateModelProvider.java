package com.tle.web.freemarker;

import com.tle.web.freemarker.methods.SectionsTemplateModel;

public interface SectionsTemplateModelProvider
{
	SectionsTemplateModel getTemplateModel(Object object);
}

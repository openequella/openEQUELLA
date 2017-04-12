package com.tle.web.sections.standard.model;

public interface Option<T>
{
	T getObject();

	String getName();

	String getValue();

	String getAltTitleAttr();

	String getGroupName();

	boolean isDisabled();

	void setDisabled(boolean disabled);

	boolean isNameHtml();

	boolean hasAltTitleAttr();
}

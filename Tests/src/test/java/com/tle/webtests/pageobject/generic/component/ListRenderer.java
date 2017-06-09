package com.tle.webtests.pageobject.generic.component;

import java.util.List;

public interface ListRenderer
{
	void setSelectionByText(String... names);

	void setSelectionByValue(String... values);

	void checkLoaded();

	void selectAll();

	List<String> getSelectedTexts();

	List<String> getSelectedValues();
}

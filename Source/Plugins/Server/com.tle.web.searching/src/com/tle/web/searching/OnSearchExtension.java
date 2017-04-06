package com.tle.web.searching;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.js.JSCallAndReference;

@NonNullByDefault
public interface OnSearchExtension
{
	JSCallAndReference getOnSearchCallable();
}

package com.tle.web.controls.universal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AttachmentHandlerUtils
{
	//@formatter:off
	@SuppressWarnings("nls")
	public static final List<String> RATING_CLASSES = Collections.unmodifiableList(Arrays.asList(
		"zero", "one", "two", "three", "four", "five"));
	//@formatter:on

	public static int getRating(float avgRating)
	{
		int rating = (int) avgRating;
		if( rating < 0 )
		{
			return 0;
		}
		if( rating > 5 )
		{
			return 5;
		}
		return rating;
	}

	private AttachmentHandlerUtils()
	{
		throw new Error();
	}
}

package com.tle.freetext;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public interface SupportedVideoMimeTypeExtension
{
	boolean isSupportedMimeType(@Nullable String mimeType);
}

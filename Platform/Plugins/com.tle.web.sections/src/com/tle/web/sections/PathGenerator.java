package com.tle.web.sections;

import java.net.URI;

public interface PathGenerator
{
	URI getBaseHref(SectionInfo info);

	URI getRelativeURI(SectionInfo info);

	URI getFullURI(SectionInfo info);
}

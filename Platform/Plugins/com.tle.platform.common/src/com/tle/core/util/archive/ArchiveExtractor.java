package com.tle.core.util.archive;

import java.io.IOException;
import java.io.InputStream;

public interface ArchiveExtractor
{
	ArchiveEntry getNextEntry() throws IOException;

	InputStream getStream();
}
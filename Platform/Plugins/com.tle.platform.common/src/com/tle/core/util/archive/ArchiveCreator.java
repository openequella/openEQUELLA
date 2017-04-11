package com.tle.core.util.archive;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public interface ArchiveCreator extends Closeable
{
	OutputStream newEntry(String name, long size) throws IOException;

	void closeEntry() throws IOException;
}
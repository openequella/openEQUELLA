package com.tle.conversion.exporters;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.Collection;

/**
 * @author bmillar
 */
public interface Export
{
	/**
	 * Exports the file specified by <code>in</code> to the file specified by
	 * <code>out</code>.
	 * 
	 * @param in The file path for the input file.
	 * @param out The file path to be output to.
	 * @throws IOException If There is an error reading from or writing to
	 *             either of the files.
	 * @throws ExportException If there is an error exporting the file; eg. the
	 *             input format is not supported.
	 */
	void exportFile(String in, String out) throws IOException;

	Collection<String> getInputTypes();

	Collection<String> getOutputTypes();
}

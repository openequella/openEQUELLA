package com.tle.common.scripting.types;

/**
 * @author Aaron
 */
public interface FileHandleScriptType
{
	/**
	 * @return Just the name of the file.
	 */
	String getName();

	/**
	 * @return The path (including the filename) of the file, relative to the
	 *         item.
	 */
	String getFilepath();

	/**
	 * @return Same as getFilepath()
	 */
	@Override
	String toString();

	/**
	 * @return File handles for other items will always be read-only. Attempting
	 *         use write/delete operations on these handles will throw an
	 *         exception.
	 */
	boolean isReadOnly();
}

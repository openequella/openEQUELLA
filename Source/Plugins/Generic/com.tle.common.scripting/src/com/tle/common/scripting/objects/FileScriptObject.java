/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.scripting.objects;

import java.util.List;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.BinaryDataScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.common.scripting.types.ItemScriptType;

/**
 * Referenced by the 'staging' variable in script
 * 
 * @author aholland
 */
public interface FileScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "staging"; //$NON-NLS-1$

	/**
	 * Determines if the staging file system is available. During certain
	 * workflow operations there will be no staging file system (e.g. Delete)
	 * Note that attempting to use a staging object when it is not available
	 * will result in an exception being thrown.
	 * 
	 * @return true if there is a staging object,
	 */
	boolean isAvailable();

	/**
	 * Returns a list of files (including folders) within the specified folder.
	 * The folder path is relative to the current item.
	 * 
	 * @param folder The folder to list the contents of.
	 * @param pattern An ant style file pattern. See
	 *            http://ant.apache.org/manual/dirtasks.html
	 * @return A list of filenames
	 */
	List<String> list(String folder, String pattern);

	/**
	 * Returns a list of files (NOT including folders) within the specified
	 * folder. The folder path is relative to the current item.
	 * 
	 * @param folder The folder to list the contents of
	 * @param pattern An ant style file pattern. See
	 *            http://ant.apache.org/manual/dirtasks.html
	 * @return A list of filenames
	 */
	List<String> listFiles(String folder, String pattern);

	/**
	 * Determine if the specified filename exists. The filename path is relative
	 * to the current item.
	 * 
	 * @param filename The path of the file, relative to the item
	 * @return true if the file exists
	 */
	boolean exists(String filename);

	/**
	 * Gets the size (in bytes) of the file specified by filename. Will return a
	 * size of -1 if the file cannot be found. The filename path is relative to
	 * the current item.
	 * 
	 * @param filename The path of the file, relative to the item
	 * @return The size in bytes of the file
	 */
	long fileLength(String filename);

	/**
	 * Get the last modified date of the file specified by filename. The date is
	 * in milliseconds since 1-Jan-1970. The filename path is relative to the
	 * current item.
	 * 
	 * @param filename The file to check
	 * @return The last modified date of the file
	 */
	long lastModified(String filename);

	/**
	 * Remove a file from the staging folder system. The filename path is
	 * relative to the current item.
	 * 
	 * @param filename The file to delete
	 */
	void deleteFile(String filename);

	/**
	 * Move a file from dest to src within the staging folder. The dest and src
	 * paths are relative to the current item. E.g.
	 * <code>staging.move('file.jpg', 'folder/subfolder/file.jpg');</code>
	 * 
	 * @param src The source file path
	 * @param dest The destination file path
	 * @return A file handle of the destination
	 */
	FileHandleScriptType move(String src, String dest);

	/**
	 * Copy a file from dest to src within the staging folder. The dest and src
	 * paths are relative to the current item. E.g. <code>
	 * staging.copy('file.jpg', 'file copy.jpg');
	 * staging.copy('other.jpg', 'folder/other.jpg');
	 * </code>
	 * 
	 * @param src The source file path
	 * @param dest The destination file path
	 * @return A file handle of the destination
	 */
	FileHandleScriptType copy(String src, String dest);

	/**
	 * Creates a new folder, creating any required parent folders. The path is
	 * relative to the item. E.g. <code>
	 * staging.createFolder('folder/subfolder');
	 * </code>
	 * 
	 * @param path The path of the new folder
	 * @return A file handle of the new folder
	 */
	FileHandleScriptType createFolder(String path);

	/**
	 * Write the text to a file in the staging folder specified by filename. The
	 * filename path is relative to the current item. This method will always
	 * create a new file regardless of whether there is an existing one or not.
	 * 
	 * @param filename The file to write to
	 * @param text The text to write
	 * @return A file handle of the new file
	 */
	FileHandleScriptType writeTextFile(String filename, String text);

	/**
	 * Read text from a file in the staging folder specified by filename. The
	 * filename path is relative to the current item.
	 * 
	 * @param filename The file to read
	 * @return The text content of the file. null if file is not found.
	 */
	String readTextFile(String filename);

	/**
	 * Write the data to a file in the staging folder specified by filename. The
	 * filename path is relative to the current item. This method will always
	 * create a new file regardless of whether there is an existing one or not.
	 * 
	 * @param filename The file to write to
	 * @param data The binary data to write
	 * @return A file handle of the new file
	 */
	FileHandleScriptType writeBinaryFile(String filename, BinaryDataScriptType data);

	/**
	 * Get a file handle for use in other script functions. The file specified
	 * by filename may or may not exist. The filename path is relative to the
	 * current item.
	 * 
	 * @param filename The file to use
	 * @return A file handle
	 */
	FileHandleScriptType getFileHandle(String filename);

	/**
	 * Get a <em>read-only</em> file handle from a arbitrary item for use in
	 * other script functions. The file specified by filename may or may not
	 * exist. The filename path is relative to the supplied item parameter.
	 * 
	 * @param item The item to use as a reference point. i.e. the filename is
	 *            relative to this item
	 * @param filename The file to use
	 * @return A file handle
	 */
	FileHandleScriptType getFileHandle(ItemScriptType item, String filename);
}

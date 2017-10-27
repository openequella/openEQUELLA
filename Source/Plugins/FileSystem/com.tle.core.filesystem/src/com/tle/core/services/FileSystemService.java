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

package com.tle.core.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import com.dytech.common.io.FileUtils.GrepFunctor;
import com.dytech.edge.common.FileInfo;
import com.tle.common.filesystem.FileCallback;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.filesystem.remoting.RemoteFileSystemService;
import com.tle.core.util.archive.ArchiveEntry;
import com.tle.core.util.archive.ArchiveProgress;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.stream.FileContentStream;

@SuppressWarnings("nls")
public interface FileSystemService extends RemoteFileSystemService
{
	String THUMBS_FOLDER = "_THUMBS";
	String TILES_FOLDER = "_TILES";
	String THUMBNAIL_EXTENSION = ".jpeg";
	String GALLERY_THUMBNAIL_EXTENSION = "_135_" + THUMBNAIL_EXTENSION;
	String GALLERY_PREVIEW_EXTENSION = "_PREVIEW_" + THUMBNAIL_EXTENSION;
	String SECURE_FOLDER = "_SECURE";
	String SCORM_FOLDER = "_SCORM";
	String VIDEO_PREVIEW_EXTENSION = ".mp4";
	String VIDEO_PREVIEW_FOLDER = "_VIDEOPREVIEW";
	String ZIPS_FOLDER = "_zips";

	/**
	 * Gets a Java File object if you for the associated
	 * Staging/Institution/Item file
	 * 
	 * @param handle
	 * @param path The path cannot be at a level above the handle path, ie. no
	 *            ".." allowed! Can be null or "" to indicate to use the handle
	 *            only.
	 * @return
	 */
	File getExternalFile(FileHandle handle, String path);

	/**
	 * Builds a flat file listing of the folder represented by the path. Does
	 * not recurse subdirectories
	 * 
	 * @param handle The base folder handle. E.g. a StagingFile object
	 * @param path The path to the folder to list the contents of. May be blank
	 * @param filter Custom filter to ignore certain files
	 * @return A flat file listing of the folder represented by the path
	 * @throws IOException
	 */
	FileEntry[] enumerate(FileHandle handle, String path, FileFilter filter) throws IOException;

	/**
	 * Builds a tree of FileEntry objects for the given file path. The root of
	 * the tree represents the path specified. If the path is a file, a single
	 * FileEntry object is returned.
	 * 
	 * @param handle The base folder handle. E.g. a StagingFile object
	 * @param path Path relative to handle, may be a folder or an actual file.
	 * @param filter Custom filter to ignore certain files
	 * @return A tree representing the folder structure of path, or single
	 *         FileEntry if path is a file.
	 * @throws IOException
	 */
	FileEntry enumerateTree(FileHandle handle, String path, FileFilter filter) throws IOException;

	/**
	 * Use UnicodeStreamReader when reading from this stream!!!
	 */
	InputStream read(FileHandle handle, String filename) throws IOException;

	OutputStream getOutputStream(FileHandle handle, String filename, boolean append) throws IOException;

	/**
	 * Does not close content stream
	 * 
	 * @param handle
	 * @param filename
	 * @param content
	 * @param append
	 * @return
	 * @throws IOException
	 */
	FileInfo write(FileHandle handle, String filename, InputStream content, boolean append, boolean md5)
		throws IOException;

	/**
	 * Does not close content stream
	 * 
	 * @param handle
	 * @param filename
	 * @param content
	 * @param append
	 * @return
	 * @throws IOException
	 */
	FileInfo write(FileHandle handle, String filename, InputStream content, boolean append) throws IOException;

	/**
	 * Does not close content reader
	 * 
	 * @param handle
	 * @param filename
	 * @param content
	 * @param append
	 * @return
	 * @throws IOException
	 */
	FileInfo write(FileHandle handle, String filename, Reader content, boolean append) throws IOException;

	FileInfo getFileInfo(FileHandle handle, String filename);

	void saveFiles(StagingFile staging, FileHandle destination) throws IOException;

	void commitFiles(TemporaryFileHandle staging, FileHandle destination) throws IOException;

	void commitFiles(TemporaryFileHandle staging, String filename, FileHandle destination) throws IOException;

	boolean fileExists(FileHandle handle, String filename);

	boolean fileExists(FileHandle handle);

	/**
	 * @param handle
	 * @param filename
	 * @return False if not a dir OR if the file doesn't exist
	 */
	boolean fileIsDir(FileHandle handle, String filename);

	/**
	 * Test if two paths point to the same file. Eg. in Windows path1 =
	 * "MyFile.jpg" and path2 = "myfile.JPG" don't string compare and yet refer
	 * to the same file. There will be other reasons the handles and paths don't
	 * match and yet refer to the same file.
	 * 
	 * @param handle1
	 * @param path1
	 * @param handle2
	 * @param path2
	 * @return
	 */
	boolean isSameFile(FileHandle handle1, String path1, FileHandle handle2, String path2);

	void mkdir(FileHandle handle, String filename);

	boolean rename(FileHandle handle, String filename, String newname);

	/**
	 * Similar to rename, but will build the folder structure of the newname if
	 * necessary
	 * 
	 * @param handle
	 * @param filename
	 * @param newname
	 * @return
	 */
	boolean move(FileHandle handle, String filename, String newname);

	boolean move(FileHandle handle, String filename, FileHandle newHandle, String newname);

	FileInfo copy(FileHandle handle, String filename, String newname);

	FileInfo copy(FileHandle handle, String filename, FileHandle toHandle, String newname);

	FileInfo copy(FileHandle source, FileHandle destination);

	void copyToStaging(FileHandle handle, String filename, TemporaryFileHandle staging, String newname,
		boolean ignoreInteralFiles) throws IOException;

	void copyToStaging(FileHandle handle, TemporaryFileHandle staging, boolean ignoreInteralFiles) throws IOException;

	InputStream retrieveCachedFile(FileHandle handle, String filename, long time) throws IOException;

	boolean isFileCached(FileHandle handle, String filename, long time);

	long fileLength(FileHandle handle, String filename) throws FileNotFoundException;

	long recursivefileLength(FileHandle handle, String filename) throws IOException;

	long lastModified(FileHandle handle, String filename);

	long mostRecentModification(FileHandle handle, String dirname);

	boolean isArchive(FileHandle handle, String zipfile);

	/**
	 * This method will close the input stream once complete
	 */
	void unzipFile(FileHandle handle, InputStream in, ArchiveType method, final ArchiveProgress progress)
		throws IOException;

	/**
	 * This method will close the input stream once complete
	 * 
	 * @param handle
	 * @param in
	 * @param method
	 * @throws IOException
	 */
	void unzipFile(FileHandle handle, InputStream in, ArchiveType method) throws IOException;

	FileInfo unzipFile(FileHandle handle, String zipfile, String outpath, final ArchiveProgress progress)
		throws IOException;

	FileInfo unzipFile(FileHandle handle, String zipfile, String outpath) throws IOException;

	ZipProgress unzipWithProgress(FileHandle handle, String zipFile, String targetPath) throws IOException;

	/**
	 * @param handle
	 * @param filename
	 * @param entryToFind
	 * @param matchCase
	 * @return
	 */
	ArchiveEntry findZipEntry(FileHandle handle, String filename, String entryToFind, boolean matchCase);

	/**
	 * @param handle
	 * @param packageZipName
	 * @param entryToFind
	 * @return A stream. Not closed by us
	 */
	ByteArrayOutputStream extractNamedZipEntryAsStream(FileHandle handle, String packageZipName, String entryToFind);

	/**
	 * @param handle
	 * @param packageZipName
	 * @param entryToFind
	 * @param out Not closed by us
	 */
	void extractNamedZipEntryAsStream(FileHandle handle, String packageZipName, String entryToFind, OutputStream out);

	long countFiles(FileHandle handle, String filename);

	/**
	 * Deletes a file, or recursively remove a folder. File or folder need not
	 * exist
	 * 
	 * @param handle
	 * @return
	 */
	boolean removeFile(FileHandle handle);

	/**
	 * Deletes a file, or recursively remove a folder. File or folder need not
	 * exist
	 * 
	 * @param handle
	 * @param filename
	 * @return
	 */
	boolean removeFile(FileHandle handle, String filename);

	/**
	 * Deletes a file, or recursively remove a folder. File or folder need not
	 * exist
	 * 
	 * @param handle
	 * @param filename
	 * @param callback
	 * @return
	 */
	boolean removeFile(FileHandle handle, String filename, FileCallback callback);

	boolean hasFileExtension(String filename, Collection<String> fileExtensions);

	String getMD5Checksum(FileHandle handle, String path) throws IOException;

	/**
	 * NOTE: This will close your OutputStream.
	 */
	void zipFile(FileHandle handle, OutputStream out, ArchiveType archiveType) throws IOException;

	/**
	 * NOTE: This will close your OutputStream.
	 */
	void zipFile(FileHandle handle, String path, OutputStream out, ArchiveType archiveType,
		final ArchiveProgress progress) throws IOException;

	void zipFile(FileHandle handle, String path, FileHandle out, String outFilename, ArchiveType archiveType,
		final ArchiveProgress progress) throws IOException;

	void zipFile(FileHandle handle, String path, FileHandle out, String outFilename, ArchiveType archiveType)
		throws IOException;

	/**
	 * Returns a list of paths matching the given pattern. The pattern is
	 * modelled on Ant's file patterns, and a complex example can be seen if you
	 * view the source for this interface. The example could not be included in
	 * Javadoc comments, as it contains characters that cannot be escaped.
	 */
	//
	// Example of Usage - Cannot be put in a multiline comment!
	//
	// **/someDir/20??-12-*/*/product*.txt
	//
	List<String> grep(FileHandle root, String path, String pattern);

	List<String> grepIncludingDirs(FileHandle root, String path, String pattern);

	/**
	 * Applies a functor to a list of paths matching the given pattern. See
	 * matchFiles() for basic pattern usage, and an example of returning a list
	 * of match file names.
	 */
	void apply(FileHandle root, String path, String pattern, GrepFunctor functor);

	FileContentStream getContentStream(FileHandle handle, String path, String mimeType);

	FileContentStream getInsecureContentStream(FileHandle handle, String path, String mimeType);
}

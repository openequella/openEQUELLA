/*
 * Copyright 2019 Apereo
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

package com.tle.web.filemanager.common;

import java.util.List;

public interface ServerBackend
{
	String getDownloadUrl(String wizardId, String filename);

	void delete(String wizardId, String filename);

	List<FileInfo> listFiles(String wizardId, String directory);

	/**
	 * @return true for a successful rename.
	 */
	boolean renameFile(String wizardId, String oldName, String newName);

	void copy(String wizardId, String sourceFile, String destFile);

	void markAsResource(String wizardId, boolean mark, String fullPath);

	void newFolder(String wizardId, String name);

	void write(String wizardId, String filename, boolean append, byte[] upload);

	void extractArchive(String wizardId, String filename, String destDir);
}

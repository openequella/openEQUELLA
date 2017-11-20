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

package com.tle.core.wizard;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.FileNode;
import com.dytech.edge.common.ScriptContext;
import com.google.common.collect.ImmutableCollection;
import com.tle.beans.Language;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides an interface for wizards to access specific functionality.
 */
public interface LERepository
{
	FileInfo uploadStream(String szFileName, InputStream inp, boolean calcMd5) throws IOException;

	void delete(String szFileName);

	InputStream read(String filename);

	void copy(String filename, String targetFilename);

	PropBagEx getItemBag();

	String getWizardName();

	boolean isEditable();

	boolean isExpert();

	String getWebUrl();

	String getUserUUID();

	FileNode getFileTree(String path);

	boolean isConvertibleToHtml(String filename);

	void spawnBrowser(String link);

	String getWizid();

	String getStagingid();

	boolean checkDataUniqueness(String xpath, ImmutableCollection<String> list, boolean canAccept);

	void checkLinkAttachmentUniqueness(String[] urls);

	FileInfo unzipFile(String zipfile, String targetFolder, boolean ignoreZipError);

	ModifiableAttachments getAttachments();

	ScriptContext getScriptContext(WizardPage page, HTMLControl control, Map<String, Object> attributes);

	void pushPathOverride(String path, int index);

	void popPathOverride();

	Item getItem();

	Locale getLocale();

	List<Language> getLanguages();

	boolean fileExists(String file);

	void updateMetadataMapping();

	Object getThreadLock();

	boolean registerFilename(UUID id, String filename);

	void unregisterFilename(UUID id);
}

package com.tle.core.wizard;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.FileNode;
import com.dytech.edge.common.ScriptContext;
import com.google.common.collect.ImmutableCollection;
import com.tle.beans.Language;
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

	com.tle.beans.item.Item getItem();

	Locale getLocale();

	List<Language> getLanguages();

	boolean fileExists(String file);

	void updateMetadataMapping();

	Object getThreadLock();
}

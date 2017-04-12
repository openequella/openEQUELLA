package com.tle.mypages.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.FileNode;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableCollection;
import com.tle.beans.Language;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.wizard.WizardStateInterface;

/**
 * @author aholland
 */
// TODO: could use WebRepository and change the WizardState/MyPagesState to an
// iface
@Bind
public class MyPagesRepository implements LERepository
{
	@Inject
	private FileSystemService fsys;

	private WizardStateInterface state;
	private StagingFile stagingHandle;

	public void setState(WizardStateInterface state)
	{
		this.state = state;
		stagingHandle = (StagingFile) state.getFileHandle();
	}

	@Override
	public boolean checkDataUniqueness(String xpath, ImmutableCollection<String> list, boolean canAccept)
	{
		return false;
	}

	@Override
	public void checkLinkAttachmentUniqueness(String[] urls)
	{
	}

	@Override
	public void delete(String szFileName)
	{
	}

	@Override
	public boolean fileExists(String file)
	{
		return fsys.fileExists(new SubTemporaryFile(stagingHandle, file));
	}

	@Override
	public ModifiableAttachments getAttachments()
	{
		return state.getAttachments();
	}

	@Override
	public FileNode getFileTree(String path)
	{
		return null;
	}

	@Override
	public Item getItem()
	{
		return state.getItem();
	}

	@Override
	public PropBagEx getItemBag()
	{
		return state.getItemxml();
	}

	@Override
	public List<Language> getLanguages()
	{
		return null;
	}

	@Override
	public Locale getLocale()
	{
		return CurrentLocale.getLocale();
	}

	@Override
	public ScriptContext getScriptContext(WizardPage page, HTMLControl control, Map<String, Object> attributes)
	{
		return null;
	}

	@Override
	public String getStagingid()
	{
		return stagingHandle.getUuid();
	}

	@Override
	public String getUserUUID()
	{
		return null;
	}

	@Override
	public String getWebUrl()
	{
		return null;
	}

	@Override
	public String getWizardName()
	{
		return CurrentLocale.get("com.tle.mypages.wizardname"); //$NON-NLS-1$
	}

	@Override
	public String getWizid()
	{
		return state.getWizid();
	}

	@Override
	public boolean isEditable()
	{
		return true;
	}

	@Override
	public boolean isExpert()
	{
		return false;
	}

	@Override
	public boolean isConvertibleToHtml(String filename)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream read(String filename)
	{
		try
		{
			return fsys.read(stagingHandle, filename);
		}
		catch( IOException ex )
		{
			throw new RuntimeApplicationException("Error reading file", ex);
		}
	}

	@Override
	public void copy(String filename, String targetFilename)
	{
		fsys.copy(stagingHandle, filename, targetFilename);
	}

	@Override
	public void spawnBrowser(String link)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public FileInfo unzipFile(String zipfile, String packagefile, boolean ignoreZipError)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public FileInfo uploadStream(String filename, InputStream inp, boolean calcMd5)
	{
		try
		{
			return fsys.write(stagingHandle, filename, inp, false);
		}
		catch( IOException ex )
		{
			throw Throwables.propagate(ex);
		}
	}

	@Override
	public void updateMetadataMapping()
	{
		// nothing
	}

	@Override
	public void pushPathOverride(String path, int index)
	{
		// nothing
	}

	@Override
	public void popPathOverride()
	{
		// nothing
	}

	@Override
	public Object getThreadLock()
	{
		return new Object();
	}
}

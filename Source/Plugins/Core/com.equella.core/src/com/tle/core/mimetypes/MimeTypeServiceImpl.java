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

package com.tle.core.mimetypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.plugins.AbstractPluginService;
import org.hibernate.Hibernate;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.CacheLoader;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.TextExtracterExtension;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.dao.MimeEntryDao;
import com.tle.core.mimetypes.institution.MimeMigrator;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.events.services.EventService;
import com.tle.exceptions.AccessDeniedException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Bind(MimeTypeService.class)
@Singleton
public class MimeTypeServiceImpl implements MimeTypeService, MimeTypesUpdatedListener
{
	private static final String DEFAULT_MIMETYPE = "application/octet-stream"; //$NON-NLS-1$

	private static final String KEY_PFX = AbstractPluginService.getMyPluginId(MimeTypeServiceImpl.class)+".";
	@Inject
	private MimeEntryDao mimeEntryDao;
	@Inject
	private EventService eventService;

	private PluginTracker<TextExtracterExtension> textExtracterTracker;

	private PluginTracker<RegisterMimeTypeExtension<Attachment>> attachmentResources;
	private Map<String, List<Extension>> extensionMap;

	private InstitutionCache<EntryCache> mimeCache;

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		mimeCache = service.newInstitutionAwareCache(new CacheLoader<Institution, EntryCache>()
		{
			@Override
			public EntryCache load(Institution key) throws Exception
			{
				return fillCache();
			}
		});
	}

	@Override
	public Collection<MimeEntry> searchByFilename(String filename)
	{
		String extension = getExtension(filename);
		Map<String, MimeEntry> tempExtensionMap = mimeCache.getCache().getMimeEntries();
		List<MimeEntry> mimes = new ArrayList<MimeEntry>();
		for( Entry<String, MimeEntry> entry : tempExtensionMap.entrySet() )
		{
			if( entry.getKey().startsWith(extension) )
			{
				mimes.add(entry.getValue());
			}
		}
		return mimes;
	}

	@Override
	public void clearMimeCache()
	{
		mimeCache.clear();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void resetEntries()
	{
		List<MimeEntry> defaultEntries = MimeMigrator.getDefaultMimeEntries();
		List<MimeEntry> all = mimeEntryDao.enumerateAll();
		for( MimeEntry mimeEntry : all )
		{
			mimeEntryDao.delete(mimeEntry);
		}
		mimeEntryDao.flush();
		mimeEntryDao.clear();
		for( MimeEntry mimeEntry : defaultEntries )
		{
			mimeEntry.setInstitution(CurrentInstitution.get());
			mimeEntryDao.save(mimeEntry);
			mimeEntryDao.flush();
			mimeEntryDao.clear();
		}
		mimeTypesChanged();
	}

	@Override
	public MimeTypesSearchResults searchByMimeType(String mimeType, int offset, int length)
	{
		String query = (Check.isEmpty(mimeType) ? "" : mimeType.toLowerCase()); //$NON-NLS-1$
		/*
		 * Map<String, MimeEntry> typeMap =
		 * mimeCache.getCache().getMimeEntries(); List<MimeEntry> mimes = new
		 * ArrayList<MimeEntry>(); for( Entry<String, MimeEntry> entry :
		 * typeMap.entrySet() ) { if( entry.getKey().startsWith(query) ) {
		 * mimes.add(entry.getValue()); } } return mimes;
		 */
		return mimeEntryDao.searchAll(query, offset, length);
	}

	private String getExtension(final String filename)
	{
		if( Check.isEmpty(filename) )
		{
			return ""; //$NON-NLS-1$
		}

		String result = filename;
		int index = filename.lastIndexOf('.');
		if( index >= 0 )
		{
			result = filename.substring(index + 1);
		}
		return result.toLowerCase();
	}

	@Override
	public MimeEntry getEntryForFilename(String filename)
	{
		EntryCache cache = mimeCache.getCache();
		return cache.getExtensionEntries().get(getExtension(filename));
	}

	@Override
	public String getMimeTypeForFilename(String filename)
	{
		MimeEntry entry = getEntryForFilename(filename);
		if( entry != null )
		{
			return entry.getType();
		}
		return DEFAULT_MIMETYPE;
	}

	@Override
	public MimeEntry getEntryForMimeType(String mimeType)
	{
		return mimeCache.getCache().getMimeEntries().get(mimeType);
	}

	@Override
	public MimeEntry getEntryForId(long id)
	{
		return mimeEntryDao.findById(id);
	}

	public static class EntryCache
	{
		private final Map<String, MimeEntry> extensionEntries;
		private final Map<String, MimeEntry> mimeEntries;

		public EntryCache(Map<String, MimeEntry> extensionEntries, Map<String, MimeEntry> mimeEntries)
		{
			this.extensionEntries = extensionEntries;
			this.mimeEntries = mimeEntries;
		}

		public Map<String, MimeEntry> getExtensionEntries()
		{
			return extensionEntries;
		}

		public Map<String, MimeEntry> getMimeEntries()
		{
			return mimeEntries;
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public EntryCache fillCacheFromDB()
	{
		return createMappings(mimeEntryDao.enumerateAll());
	}

	public EntryCache fillCache()
	{
		if( CurrentInstitution.get() == null )
		{
			return createMappings(getNonInstitutionEntries());
		}
		else
		{
			return fillCacheFromDB();
		}
	}

	private EntryCache createMappings(List<MimeEntry> entries)
	{
		Map<String, MimeEntry> mappedEntries = new HashMap<String, MimeEntry>();
		Map<String, MimeEntry> mimeEntries = new HashMap<String, MimeEntry>();
		for( MimeEntry mimeEntry : entries )
		{
			Collection<String> extensions = mimeEntry.getExtensions();
			// Load it all up
			Hibernate.initialize(mimeEntry.getAttributes());
			for( String ext : extensions )
			{
				mappedEntries.put(ext, mimeEntry);
			}
			mimeEntries.put(mimeEntry.getType(), mimeEntry);
		}
		return new EntryCache(mappedEntries, mimeEntries);
	}

	private List<MimeEntry> getNonInstitutionEntries()
	{
		return MimeFileUtils.readLegacyFile(getClass().getResourceAsStream("mimenoninst.types"), null); //$NON-NLS-1$
	}

	@Override
	@Transactional
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public void saveOrUpdate(long mimeEntryId, MimeEntryChanges changes)
	{
		MimeEntry mimeEntry;
		if( mimeEntryId != 0 )
		{
			mimeEntry = mimeEntryDao.findById(mimeEntryId);
			/*
			 * This is to get around Hibernate #HHH-358 in conjunction with
			 * Oracle's blank strings
			 */
			mimeEntry.setAttributes(new HashMap<String, String>(mimeEntry.getAttributes()));
		}
		else
		{
			mimeEntry = new MimeEntry();
		}
		mimeEntry.setInstitution(CurrentInstitution.get());
		changes.editMimeEntry(mimeEntry);
		validate(mimeEntry);
		mimeEntryDao.saveOrUpdate(mimeEntry);
		mimeTypesChanged();
	}

	private void validate(MimeEntry mimeEntry)
	{
		// if this is a new mime type, or an existing one is being edited, it's
		// type field must be unique
		MimeEntry existing = getEntryForMimeType(mimeEntry.getType());
		if( existing != null && existing.getId() != mimeEntry.getId() )
		{
			throw new InvalidDataException(
				new ValidationError("type", null, KEY_PFX + "error.mimetype.exists"));
		}

		if( Check.isEmpty(mimeEntry.getType()) )
		{
			throw new InvalidDataException(
				new ValidationError("type", null,KEY_PFX + "error.mimetype.empty"));
		}

		Collection<String> extensions = mimeEntry.getExtensions();
		if( !extensions.isEmpty() )
		{
			List<MimeEntry> entries = mimeEntryDao.getEntriesForExtensions(extensions);
			int sz = entries.size();
			if( sz > 1 || (sz == 1 && entries.get(0).getId() != mimeEntry.getId()) )
			{
				throw new InvalidDataException(new ValidationError("extensions",null,KEY_PFX + "error.extensions.alreadyinuse"));
			}
		}
	}

	@Override
	@Transactional
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public void delete(MimeEntry mimeEntry)
	{
		MimeEntry mime = mimeEntryDao.findById(mimeEntry.getId());
		if( MimeMigrator.EQUELLA_TYPES.contains(mime.getType()) )
		{
			throw new AccessDeniedException("You cannot remove the equella/* mime types");
		}
		mimeEntryDao.delete(mime);
		mimeTypesChanged();
	}

	@Override
	@Transactional
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public void delete(long id)
	{
		MimeEntry mime = mimeEntryDao.findById(id);
		if( mime == null )
		{
			throw new NotFoundException("MimeEntry with id " + id + " not found.");
		}
		if( MimeMigrator.EQUELLA_TYPES.contains(mime.getType()) )
		{
			throw new AccessDeniedException("You cannot remove the equella/* mime types");
		}
		mimeEntryDao.delete(mime);
		mimeTypesChanged();
	}

	private void mimeTypesChanged()
	{
		mimeCache.clear();
		eventService.publishApplicationEvent(new MimeTypesUpdatedMessage());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getListFromAttribute(MimeEntry entry, String key, Class<T> entryType)
	{
		Map<String, String> attr = entry.getAttributes();
		String jsonText = attr.get(key);
		if( !Check.isEmpty(jsonText) )
		{
			return new ArrayList<T>(JSONArray.toCollection(JSONArray.fromObject(jsonText), entryType));
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBeanFromAttribute(MimeEntry entry, String key, Class<T> entryType)
	{
		Map<String, String> attr = entry.getAttributes();
		String enabledJson = attr.get(key);
		if( !Check.isEmpty(enabledJson) )
		{
			return (T) JSONObject.toBean(JSONObject.fromObject(enabledJson), entryType);
		}
		return null;
	}

	@Override
	public void clearAllForPrefix(MimeEntry entry, String keyPrefix)
	{
		Map<String, String> attr = entry.getAttributes();
		Iterator<String> iter = attr.keySet().iterator();
		while( iter.hasNext() )
		{
			String key = iter.next();
			if( key.startsWith(keyPrefix) )
			{
				iter.remove();
			}
		}
	}

	@Override
	public <T> void setBeanAttribute(MimeEntry entry, String key, @Nullable T bean)
	{
		Map<String, String> attr = entry.getAttributes();
		if( bean == null )
		{
			attr.remove(key);
			return;
		}
		attr.put(key, JSONObject.fromObject(bean).toString());
	}

	@Override
	public void setListAttribute(MimeEntry entry, String key, @Nullable Collection<?> list)
	{
		Map<String, String> attr = entry.getAttributes();
		if( list == null || list.isEmpty() )
		{
			attr.remove(key);
			return;
		}
		attr.put(key, JSONArray.fromObject(list).toString());
	}

	@Override
	public List<TextExtracterExtension> getAllTextExtracters()
	{
		return textExtracterTracker.getBeanList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TextExtracterExtension> getTextExtractersForMimeEntry(final MimeEntry mimeEntry)
	{
		// Collection<Extension> extensions = textExtracterTracker
		// .getExtensions(new Filter<Extension>()
		// {
		// @Override
		// public boolean include(Extension t)
		// {
		//					Collection<Parameter> mimes = t.getParameters("mimeType"); //$NON-NLS-1$
		// for( Parameter mime : mimes )
		// {
		// String m = mime.valueAsString();
		// int wildIndex = m.indexOf('*');
		// if( wildIndex >= 0 )
		// {
		// String nonWild = m.substring(0, wildIndex);
		// if( mimeType.startsWith(nonWild) )
		// {
		// return true;
		// }
		// }
		// else
		// {
		// if( mimeType.equals(m) )
		// {
		// return true;
		// }
		// }
		// }
		// return false;
		// }
		// });
		//
		// List<TextExtracterExtension> extracters = new
		// ArrayList<TextExtracterExtension>();
		// for( Extension e : extensions )
		// {
		// extracters.add(textExtracterTracker.getBeanByExtension(e));
		// }
		// return extracters;

		List<TextExtracterExtension> extracters = getAllTextExtracters();
		List<TextExtracterExtension> filtered = new ArrayList<TextExtracterExtension>();
		for( TextExtracterExtension extracter : extracters )
		{
			if( extracter.isEnabledForMimeEntry(mimeEntry) )
			{
				filtered.add(extracter);
			}
		}
		return filtered;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		textExtracterTracker = new PluginTracker<TextExtracterExtension>(pluginService, "com.tle.core.mimetypes", //$NON-NLS-1$
			"textExtracter", "id", new ExtensionParamComparator()); //$NON-NLS-1$ //$NON-NLS-2$
		textExtracterTracker.setBeanKey("class"); //$NON-NLS-1$

		attachmentResources = new PluginTracker<RegisterMimeTypeExtension<Attachment>>(pluginService,
			"com.tle.core.mimetypes", "attachmentResourceMimeType", null, //$NON-NLS-1$
			new PluginTracker.ExtensionParamComparator("order")); //$NON-NLS-1$
		attachmentResources.setBeanKey("class"); //$NON-NLS-1$
	}

	@Override
	public String getMimeEntryForAttachment(Attachment attachment)
	{
		AttachmentType attachType = attachment.getAttachmentType();
		String type = attachType.name().toLowerCase();
		if( attachType == AttachmentType.CUSTOM )
		{
			type += '/' + ((CustomAttachment) attachment).getType().toLowerCase();
		}
		Map<String, List<Extension>> map = getExtensionMap();
		List<Extension> extensions = map.get(type);
		if( extensions != null )
		{
			for( Extension extension : extensions )
			{
				return attachmentResources.getBeanByExtension(extension).getMimeType(attachment);
			}
		}

		return null;
	}

	private synchronized Map<String, List<Extension>> getExtensionMap()
	{
		if( extensionMap == null )
		{
			extensionMap = new HashMap<String, List<Extension>>();
			List<Extension> extensions = attachmentResources.getExtensions();
			for( Extension extension : extensions )
			{
				Collection<Parameter> typeParams = extension.getParameters("type"); //$NON-NLS-1$
				for( Parameter parameter : typeParams )
				{
					String typeString = parameter.valueAsString();
					List<Extension> perTypeList = extensionMap.get(typeString);
					if( perTypeList == null )
					{
						perTypeList = new ArrayList<Extension>();
						extensionMap.put(typeString, perTypeList);
					}
					perTypeList.add(extension);
				}
			}
		}
		return extensionMap;
	}
}

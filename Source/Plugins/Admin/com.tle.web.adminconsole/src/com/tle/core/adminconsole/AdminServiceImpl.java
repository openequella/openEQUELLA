package com.tle.core.adminconsole;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.io.ByteStreams;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.StagingService;
import com.tle.core.services.entity.BaseEntityService;
import com.tle.core.services.item.FreeTextService;

/**
 * @author Nicholas Read
 */
@Bind(RemoteAdminService.class)
@Singleton
@SuppressWarnings("nls")
public class AdminServiceImpl implements RemoteAdminService
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private StagingService stagingService;
	@Inject
	private TLEAclManager tleAclManager;
	@Inject
	private BaseEntityService baseEntityService;

	private PluginTracker<?> toolTracker;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		toolTracker = new PluginTracker<Object>(pluginService, "com.tle.admin.tools", "tool", null);
	}

	@Override
	public Map<String, Set<String>> getAllowedTools()
	{
		Map<String, Set<String>> extToPrivsMap = new HashMap<String, Set<String>>();
		Set<String> privileges = new HashSet<String>();

		Collection<Extension> extensions = toolTracker.getExtensions();
		for( Extension extension : extensions )
		{
			Collection<Parameter> privParams = extension.getParameters("privilege");
			if( privParams.size() > 0 )
			{
				Set<String> extPrivs = new HashSet<String>();
				for( Parameter parameter : privParams )
				{
					extPrivs.add(parameter.valueAsString());
				}

				extToPrivsMap.put(extension.getUniqueId(), extPrivs);
				privileges.addAll(extPrivs);
			}
		}

		// To start with, filter out any privileges we definitely don't have,
		// but include possible privileges for ACLs using "owner".
		if( !privileges.isEmpty() )
		{
			privileges = tleAclManager.filterNonGrantedPrivileges(privileges, true);
		}

		// Remove any edit privileges for base entity objects if no objects
		// exist for an entity, or revokes have been put directly on objects.
		privileges.removeAll(baseEntityService.getEditPrivilegeForEntitiesIHaveNoneToEdit());

		// Unregister any tools we don't have the privileges to use
		for( Iterator<Entry<String, Set<String>>> iter = extToPrivsMap.entrySet().iterator(); iter.hasNext(); )
		{
			Entry<String, Set<String>> extPrivs = iter.next();
			Set<String> privs = extPrivs.getValue();

			// Remove tool if privileges have been specified, but none of them
			// are granted.
			if( privs != null )
			{
				if( Collections.disjoint(privileges, privs) )
				{
					iter.remove();
				}
				else
				{
					privs.retainAll(privileges);
				}
			}
		}

		return extToPrivsMap;
	}

	@Override
	public void reindexSearchEngine()
	{
		freeTextService.indexAll();
	}

	@Override
	public void uploadFile(String staging, String filename, byte[] bytes, boolean append) throws IOException
	{
		fileSystemService.write(new StagingFile(staging), filename, new ByteArrayInputStream(bytes), append);
	}

	@Override
	public byte[] downloadFile(String staging, String filename) throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		InputStream stream2 = fileSystemService.read(new StagingFile(staging), filename);
		try
		{
			ByteStreams.copy(stream2, stream);
		}
		finally
		{
			stream2.close();
		}
		return stream.toByteArray();
	}

	@Override
	public void removeFile(String staging, String filename)
	{
		fileSystemService.removeFile(new StagingFile(staging), filename);
	}

	@Override
	public void clearStaging(String staging)
	{
		StagingFile file = new StagingFile(staging);
		fileSystemService.removeFile(file, ""); //$NON-NLS-1$
	}

	@Override
	public String createStaging()
	{
		return stagingService.createStagingArea().getUuid();
	}

	@Override
	public SearchResults<Item> searchReducedItems(Search search, int start, int count)
	{
		SearchResults<Item> results = freeTextService.search(search, start, count);

		List<Item> items = results.getResults();
		for( ListIterator<Item> iter = items.listIterator(); iter.hasNext(); )
		{
			Item item = iter.next();
			// Can be null if not found
			if( item != null )
			{
				Item newItem = new Item();
				newItem.setId(item.getId());
				newItem.setUuid(item.getUuid());
				newItem.setVersion(item.getVersion());
				newItem.setName(item.getName());
				newItem.setDescription(item.getDescription());
				newItem.setStatus(item.getStatus());
				newItem.setMetadataSecurityTargets(item.getMetadataSecurityTargets());

				ItemDefinition itemDef = new ItemDefinition();
				itemDef.setId(item.getItemDefinition().getId());
				newItem.setItemDefinition(itemDef);

				iter.set(newItem);
			}
		}

		return new SimpleSearchResults<Item>(results.getResults(), results.getCount(), results.getOffset(),
			results.getAvailable());
	}
}

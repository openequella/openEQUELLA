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

package com.tle.freetext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.freetext.event.ItemReindexEvent;
import com.tle.core.freetext.reindex.InstitutionFilter;
import com.tle.core.freetext.reindex.ReindexFilter;
import com.tle.core.freetext.reindex.ReindexHandler;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.SecurityPostProcessor;
import com.tle.core.events.services.EventService;

@Bind
@Singleton
public class SecurityExtension implements SecurityPostProcessor
{
	private PluginTracker<ReindexHandler> reindexHandlers;
	private Set<String> privsRequiringReindex;

	@Inject
	private EventService eventService;

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		reindexHandlers = new PluginTracker<ReindexHandler>(pluginService, "com.tle.core.freetext", "securityReindexHandler", null)
			.setBeanKey("bean");
	}

	private boolean doesTargetListAffectItemIndexing(TargetList oldTL, TargetList newTL)
	{
		Set<String> reindexPrivs = getPrivsRequiringReindex();

		// Map equals will check Map.Entry equals, which checks List equals,
		// which checks TargetListEntry equals
		return !getTargetListPrivReindexingMap(reindexPrivs, oldTL)
			.equals(getTargetListPrivReindexingMap(reindexPrivs, newTL));
	}

	@SuppressWarnings("nls")
	private synchronized Set<String> getPrivsRequiringReindex()
	{
		if( privsRequiringReindex == null || reindexHandlers.needsUpdate() )
		{
			ImmutableSet.Builder<String> builder = ImmutableSet.builder();
			List<Extension> extensions = reindexHandlers.getExtensions();
			for( Extension extension : extensions )
			{
				Collection<Parameter> priv = extension.getParameters("privilege");
				for( Parameter parameter : priv )
				{
					builder.add(parameter.valueAsString());
				}
			}
			privsRequiringReindex = builder.build();
		}
		return privsRequiringReindex;
	}

	private Multimap<String, TargetListEntry> getTargetListPrivReindexingMap(Set<String> reindexPrivs, TargetList tl)
	{
		Multimap<String, TargetListEntry> rv = ArrayListMultimap.create();
		if( tl.getEntries() != null )
		{
			for( TargetListEntry entry : tl.getEntries() )
			{
				final String priv = entry.getPrivilege();
				if( reindexPrivs.contains(priv) )
				{
					rv.get(priv).add(entry);
				}
			}
		}
		return rv;
	}

	@SuppressWarnings("nls")
	@Override
	public void postProcess(Node node, String target, Object domainObj, TargetList oldTL, TargetList newTL)
	{
		if( doesTargetListAffectItemIndexing(oldTL, newTL) )
		{
			ReindexFilter filter = null;
			if( node == Node.INSTITUTION )
			{
				filter = new InstitutionFilter();
			}
			else if( node == Node.DYNAMIC_ITEM_METADATA )
			{
				// Nothing for dynamic
				return;
			}
			else
			{
				List<ReindexHandler> handlers = reindexHandlers.getBeanList();
				for( ReindexHandler reindexHandler : handlers )
				{
					filter = reindexHandler.getReindexFilter(node, domainObj);
					if( filter != null )
					{
						break;
					}
				}
				if( filter == null )
				{
					throw new RuntimeApplicationException("Unhandled reindexing type: " + node);
				}

			}

			eventService.publishApplicationEvent(new ItemReindexEvent(filter));
		}
	}
}

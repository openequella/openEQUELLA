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

package com.tle.web.sections.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.tle.common.Check;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.RegistrationHandler;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.events.SectionEvent;

@SuppressWarnings("nls")
public class DefaultSectionTree implements SectionTree
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SectionTree.class);
	private static final Map<Class<?>, Set<Class<?>>> INTERFACE_CACHE = new ConcurrentHashMap<Class<?>, Set<Class<?>>>();

	private final Map<String, InternalNode> nodeIdMap;
	private final ListMultimap<Class<?>, InternalNode> classMap;

	private InternalNode root;

	private final Map<String, Object> dataMap = new HashMap<String, Object>();
	private final Map<String, Object> layoutMap = new HashMap<String, Object>();
	private final Map<String, String> placeHolderMap = new HashMap<String, String>();
	private final Map<Object, Object> attributes = new HashMap<Object, Object>();
	private final Map<Object, Object> runtimeAttributes = new HashMap<Object, Object>();
	private final List<DelayedRegistration> delayedRegisterers = new ArrayList<DelayedRegistration>();
	private final List<SectionEvent<? extends EventListener>> applicationEvents = new ArrayList<SectionEvent<? extends EventListener>>();
	private final ListenerRegistry listeners = new ListenerRegistry();
	private RegistrationController registrationController;
	private final List<RegistrationHandler> extraHandlers = new ArrayList<RegistrationHandler>();
	private boolean disableRegistration;
	private int unnamed = 0;
	private boolean finished;

	public DefaultSectionTree()
	{
		nodeIdMap = new HashMap<String, InternalNode>();
		classMap = ArrayListMultimap.create(100, 1);
	}

	public DefaultSectionTree(RegistrationController controller, SectionNode rootNode)
	{
		this();
		this.registrationController = controller;
		setRootNode(rootNode);
	}

	@PostConstruct
	public void treeFinished()
	{
		registrationController.treeFinished(this);
	}

	public void setRootNode(SectionNode rootNode)
	{
		String rootId = rootNode.getId();
		if( rootId == null )
		{
			Section rootSection = rootNode.getSection();
			rootId = rootSection.getDefaultPropertyName();
		}
		rootNode.setId("");
		registerSectionsInternal("", rootNode, null, null, true, false, rootId);
	}

	private Section getSection(String id)
	{
		InternalNode node = getNode(id, false);
		return (node == null ? null : node.section);
	}

	private InternalNode getNode(String sectionId, boolean mustExist)
	{
		InternalNode node = nodeIdMap.get(sectionId);
		if( node == null && mustExist )
		{
			throw new Error("No node for " + sectionId);
		}
		return node;
	}

	private InternalNode getParentNode(String id)
	{
		InternalNode node = nodeIdMap.get(id);
		// Note: this HAS to blow up if node == null
		return node.parent;
	}

	private void addSection(String parent, String id, Section section, String insertRef, boolean after, boolean inner)
	{
		if( parent == null )
		{
			if( root != null )
			{
				throw new Error("Root is already set!");
			}
			root = new InternalNode(null, id, section, getClassToRegister(section));
		}
		else
		{
			InternalNode parentNode = nodeIdMap.get(parent);
			if( parentNode == null )
			{
				throw new Error("Parent section '" + parent + "' not found");
			}
			parentNode.addChild(new InternalNode(parentNode, id, section, getClassToRegister(section)), insertRef,
				after, inner);
		}
	}

	private Class<?> getClassToRegister(Section section)
	{
		Class<?> clazz = (section.isTreeIndexed() ? section.getClass() : null);
		// Sigh, friggen proxies. Dodgy:
		if( clazz != null && clazz.getName().contains("$$EnhancerByCGLIB$$") )
		{
			clazz = section.getClass().getSuperclass();
		}
		return clazz;
	}

	@Override
	public String getPlaceHolder(String id)
	{
		if( id == null )
		{
			return null;
		}
		String placeId = placeHolderMap.get(id);
		return placeId != null ? placeId : id;
	}

	@Override
	public String getSubId(String parentId, String childId)
	{
		if( parentId.startsWith(root.id) )
		{
			parentId = parentId.substring(root.id.length());
		}
		return parentId + "_" + childId;
	}

	@Override
	public String getRootId()
	{
		return (root == null ? "" : root.id);
	}

	@Override
	public String getParentId(String sectionId)
	{
		return getParentNode(sectionId).id;
	}

	@Override
	public Section getSectionForId(String sectionId)
	{
		return getSection(sectionId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getData(String id)
	{
		return (T) dataMap.get(id);
	}

	@Override
	public <T> void setData(String id, T data)
	{
		dataMap.put(id, data);
	}

	@Override
	public boolean containsId(String id)
	{
		return nodeIdMap.containsKey(id);
	}

	private String registerSectionsInternal(String preferred, Object node, String parentId, String insertId,
		boolean after, boolean inner, String idForRoot)
	{
		if( disableRegistration )
		{
			throw new SectionsRuntimeException("Trying to register sections after registration finished");
		}
		if( parentId == null && root != null )
		{
			parentId = root.id;
		}

		if( parentId != null )
		{
			InternalNode parent = getNode(parentId, false);
			if( parent == null )
			{
				throw new SectionsRuntimeException("Trying to register into an unknown parent: " + parentId);
			}
		}
		if( node instanceof List<?> )
		{
			List<?> list = (List<?>) node;
			for( Object object : list )
			{
				registerSectionsInternal(null, object, parentId, null, true, inner, null);
			}
			return null;
		}
		else if( node instanceof SectionNode )
		{
			SectionNode snode = (SectionNode) node;
			String preferredId = snode.getId();
			Section section = snode.getSection();
			String id = registerSectionsInternal(preferredId, section, parentId, insertId, after, inner, idForRoot);
			String placeHolderId = snode.getPlaceHolderId();
			if( !Check.isEmpty(placeHolderId) )
			{
				placeHolderMap.put(placeHolderId, id);
			}
			List<Object> children = snode.getChildren();
			if( children != null )
			{
				registerSectionsInternal(null, children, id, null, true, false, null);
			}
			children = snode.getInnerChildren();
			if( children != null )
			{
				registerSectionsInternal(null, children, id, null, true, true, null);
			}
			return id;
		}
		else if( node instanceof Section )
		{
			Section section = (Section) node;
			final String id = (idForRoot == null ? findUniqueName(preferred, section) : idForRoot);
			placeHolderMap.put(section.getClass().getName(), id);
			addSection(parentId, id, section, insertId, after, inner);
			registrationController.registered(id, this, section);
			section.registered(id, this);
			return id;
		}

		// Dynamic trees can have null root sections. E.g. see
		// WebWizardPage.wrapControls
		if( node == null && idForRoot != null )
		{
			root = new InternalNode(null, idForRoot, null, null);
			return root.id;
		}
		else if( node == null )
		{
			throw new SectionsRuntimeException("Trying to register a null section");
		}

		throw new SectionsRuntimeException("Unknown section registration type:" + node.getClass());
	}

	@Override
	public void addRegistrationHandler(RegistrationHandler handler)
	{
		extraHandlers.add(handler);
	}

	@Override
	public List<RegistrationHandler> getExtraRegistrationHandlers()
	{
		return extraHandlers;
	}

	private String findUniqueName(String preferred, Section section)
	{
		String prefId = (preferred == null ? section.getDefaultPropertyName() : preferred);

		final String rootId = getRootId();
		if( Check.isEmpty(prefId) )
		{
			if( !containsId(rootId) )
			{
				return rootId;
			}
			prefId = Integer.toString(++unnamed);
		}
		prefId = rootId + prefId;
		String id = prefId;
		int count = 2;
		while( containsId(id) )
		{
			id = prefId + count;
			count++;
		}
		return id;
	}

	@Override
	public String registerSections(Object section, String parent)
	{
		return registerSectionsInternal(null, section, parent, null, true, false, null);
	}

	@Override
	public String registerSections(Object section, String parent, String insertId, boolean after)
	{
		return registerSectionsInternal(null, section, parent, insertId, after, false, null);
	}

	// public void unregisterSection(String sectionIdStr)
	// {
	// TreeSectionId sectionId = new TreeSectionId(sectionIdStr, null, this);
	// final String parent = parentMap.get(sectionIdStr);
	//
	// List<SectionId> children = childMap.get(parent);
	// children.remove(sectionId);
	// children = allChildMap.get(parent);
	// children.remove(sectionId);
	//
	// remove(sectionIdStr);
	// }
	//
	// private void remove(String sectionId)
	// {
	// SectionId id = sectionMap.get(sectionId);
	// List<SectionId> children = allChildMap.get(sectionId);
	// if( children != null )
	// {
	// for( SectionId child : children )
	// {
	// remove(child.getSectionId());
	// }
	// }
	//
	// childMap.remove(id);
	// allChildMap.remove(id);
	// parentMap.remove(id);
	// parentStringIdMap.remove(sectionId);
	// sectionMap.remove(sectionId);
	//
	// removeListeners(sectionId);
	// }

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(Object key)
	{
		return (T) attributes.get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SectionId, S extends T> S lookupSection(Class<T> key, SectionId reference)
	{
		List<InternalNode> matches = classMap.get(key);
		int size = matches.size();
		if( size == 1 )
		{
			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace("Exact match for " + key + " found");
			}
			return (S) matches.get(0).section;
		}
		if( size == 0 )
		{
			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace("No match for " + key);
			}
			return null;
		}
		throw new Error(
			"Multiple sections of class "
				+ key
				+ " exist in this tree"
				+ (reference == null ? "" : " when doing a lookup on " + reference + " "
					+ (reference.getClass().getName())));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SectionId, S extends T> List<S> lookupSections(Class<T> key)
	{
		final List<InternalNode> matches = classMap.get(key);
		final List<S> result = new ArrayList<>();
		for( InternalNode node : matches )
		{
			result.add((S) node.section);
		}
		return result;
	}

	@Override
	public boolean containsKey(Object key)
	{
		return attributes.containsKey(key);
	}

	@Override
	public void setAttribute(Object key, Object value)
	{
		attributes.put(key, value);
	}

	@Override
	public void addApplicationEvent(SectionEvent<? extends EventListener> event)
	{
		applicationEvents.add(event);
	}

	@Override
	public List<SectionEvent<? extends EventListener>> getApplicationEvents()
	{
		return applicationEvents;
	}

	@Inject
	public void setRegistrationController(RegistrationController registrationHandler)
	{
		this.registrationController = registrationHandler;
	}

	@Override
	public String registerInnerSection(Object section, String parentId)
	{
		return registerSectionsInternal(null, section, parentId, null, true, true, null);
	}

	@Override
	public void finished()
	{
		finished = true;

		// Breadth first!
		final List<InternalNode> s = new LinkedList<InternalNode>();
		s.add(root);
		while( !s.isEmpty() )
		{
			final InternalNode node = s.remove(0);
			if( node.section != null ) // can be null for dynamic rootless trees
			{
				node.section.treeFinished(node.id, this);
			}
			s.addAll(node.getChildNodes());
		}
	}

	@Override
	public boolean isFinished()
	{
		return finished;
	}

	@Override
	public <T extends EventListener> void addListener(String target, Class<T> clazz, Object eventListener)
	{
		listeners.addListener(target, clazz, eventListener);
	}

	@Override
	public <T extends EventListener> List<Object> getListeners(String target, Class<? extends T> clazz)
	{
		return listeners.getListeners(target, clazz);
	}

	@Override
	public void removeListeners(String target)
	{
		listeners.removeListeners(target);
	}

	@Override
	public Map<Object, Object> getRuntimeAttributes()
	{
		return runtimeAttributes;
	}

	@Override
	public void setRuntimeAttribute(Object key, Object value)
	{
		runtimeAttributes.put(key, value);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		addToString(sb, getRootId(), 0);
		return sb.toString();
	}

	private void addToString(StringBuilder sb, String id, int indent)
	{
		for( int i = 0; i < indent; i++ )
		{
			sb.append(' ');
		}
		if( id.isEmpty() )
		{
			sb.append("''");
		}
		sb.append(id);
		InternalNode node = getNode(id, false);
		if( node != null )
		{
			sb.append(" - ");
			Section nodeSection = node.section;
			sb.append(nodeSection == null ? "No section" : nodeSection.getClass().getName());

			sb.append('\n');
			List<SectionId> list = node.getAllChildIds();
			for( SectionId childId : list )
			{
				addToString(sb, childId.getSectionId(), indent + 1);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> addToListAttribute(Object key, T value)
	{
		List<T> list = (List<T>) attributes.get(key);
		if( list == null )
		{
			list = new ArrayList<T>();
			attributes.put(key, list);
		}
		list.add(value);
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getLayout(String id)
	{
		return (T) layoutMap.get(id);
	}

	@Override
	public void setLayout(String id, Object data)
	{
		layoutMap.put(id, data);
	}

	@Override
	public void addDelayedRegistration(DelayedRegistration delayed)
	{
		delayedRegisterers.add(delayed);
	}

	@Override
	public void runDelayedRegistration()
	{
		for( DelayedRegistration registerer : delayedRegisterers )
		{
			registerer.register(this);
		}
		disableRegistration = true;
	}

	@Override
	public List<SectionId> getChildIds(String sectionId)
	{
		return getNode(sectionId, true).getChildSections();
	}

	@Override
	public List<SectionId> getAllChildIds(String sectionId)
	{
		return getNode(sectionId, true).getAllChildIds();
	}

	@Override
	public String registerSubInnerSection(Section section, String parentId)
	{
		String id = getSubId(parentId, section.getDefaultPropertyName());
		return registerInnerSection(new SectionNode(id, section), parentId);
	}

	@Override
	public String registerSubInnerSection(Section section, String parentId, String preferredId)
	{
		String id = getSubId(parentId, preferredId);
		return registerInnerSection(new SectionNode(id, section), parentId);
	}

	class InternalNode
	{
		final String id;
		final Section section;
		final InternalNode parent;

		private List<InternalNode> children; // lazy
		private final Class<?> classRegisteredAs;

		// For speed!
		private List<SectionId> nonInnerChildSections; // lazy
		private List<SectionId> allChildIds; // lazy

		InternalNode(InternalNode parent, String id, Section section, Class<?> classRegisteredAs)
		{
			this.parent = parent;
			this.section = section;
			this.id = id;
			this.classRegisteredAs = classRegisteredAs;
			nodeIdMap.put(id, this);

			if( section != null )
			{
				for( Class<?> iface : getAllIndexed(section.getClass()) )
				{
					if( iface != classRegisteredAs )
					{
						classMap.put(iface, this);
					}
				}
				if( classRegisteredAs != null )
				{
					classMap.put(classRegisteredAs, this);
				}
			}
		}

		private Set<Class<?>> getAllIndexed(Class<?> clazz)
		{
			Set<Class<?>> interfaces = INTERFACE_CACHE.get(clazz);
			if( interfaces != null )
			{
				return interfaces;
			}

			interfaces = new HashSet<Class<?>>();
			TreeIndexed ti = clazz.getAnnotation(TreeIndexed.class);
			if( ti != null )
			{
				interfaces.add(clazz);
			}

			Class<?>[] declaredInterfaces = clazz.getInterfaces();
			for( Class<?> decInt : declaredInterfaces )
			{
				ti = decInt.getAnnotation(TreeIndexed.class);
				if( ti != null )
				{
					interfaces.add(decInt);
				}
			}
			Class<?> superclass = clazz.getSuperclass();
			if( superclass != null )
			{
				interfaces.addAll(getAllIndexed(superclass));
			}
			INTERFACE_CACHE.put(clazz, interfaces);
			return interfaces;
		}

		public List<InternalNode> getChildNodes()
		{
			if( children == null )
			{
				return Collections.emptyList();
			}
			// A little inefficient, we'll just trust them...
			// return Collections.unmodifiableList(children);
			return children;
		}

		public Section getParentSection()
		{
			return (parent == null ? null : parent.section);
		}

		public List<SectionId> getChildSections()
		{
			if( nonInnerChildSections == null )
			{
				return Collections.emptyList();
			}
			// A little inefficient, we'll just trust them...
			// return Collections.unmodifiableList(nonInnerChildSections);
			return nonInnerChildSections;
		}

		public List<SectionId> getAllChildIds()
		{
			if( allChildIds == null )
			{
				return Collections.emptyList();
			}
			// A little inefficient, we'll just trust them...
			// return Collections.unmodifiableList(allChildIds);
			return allChildIds;
		}

		public int getNumberOfChildren()
		{
			if( children == null )
			{
				return 0;
			}
			return children.size();
		}

		public void addChild(InternalNode child, String insertRef, boolean after, boolean inner)
		{
			ensureChildren();

			int index = findInsertIndex(allChildIds, insertRef, after);
			children.add(index, child);

			// For speed!
			Section childSection = child.section;
			allChildIds.add(index, childSection);

			if( !inner )
			{
				int nonInnerIndex = findInsertIndex(nonInnerChildSections, insertRef, after);
				nonInnerChildSections.add(nonInnerIndex, childSection);
			}
		}

		private int findInsertIndex(List<SectionId> currentSections, String lookForId, boolean after)
		{
			int index = after ? currentSections.size() : 0;
			if( lookForId != null )
			{
				int i = 0;
				boolean found = false;
				for( SectionId sectionId : currentSections )
				{
					if( sectionId.getSectionId().equals(lookForId) )
					{
						index = i + 1;
						found = true;
						break;
					}
					i++;
				}
				if( !found )
				{
					throw new RuntimeException("Cannot find insertion point " + lookForId + " for section id " + id);
				}

				if( !after && index > 0 )
				{
					index--;
				}
			}
			return index;
		}

		private void ensureChildren()
		{
			if( children == null )
			{
				children = new ArrayList<InternalNode>();
				nonInnerChildSections = new ArrayList<SectionId>();
				allChildIds = new ArrayList<SectionId>();
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if( obj instanceof InternalNode )
			{
				InternalNode other = (InternalNode) obj;
				return id.equals(other.id);
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return id.hashCode();
		}

		@Override
		public String toString()
		{
			return id + " (" + (classRegisteredAs == null ? "No direct registered class" : classRegisteredAs) + ")";
		}
	}
}

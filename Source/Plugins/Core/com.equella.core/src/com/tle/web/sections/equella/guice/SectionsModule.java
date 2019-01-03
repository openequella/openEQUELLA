/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.equella.guice;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.DefaultSectionTree;

public abstract class SectionsModule extends AbstractModule
{
	protected void bindNamed(String name, NodeProvider rootNode)
	{
		bind(Object.class).annotatedWith(Names.named(name)).toProvider(rootNode);
	}

	protected NodeProvider node(Class<? extends Section> section)
	{
		return new NodeProvider(section, null);
	}

	protected NodeProvider node(Class<? extends Section> section, String id)
	{
		return new NodeProvider(section, id);
	}

	protected TreeProvider tree(NodeProvider node)
	{
		return new TreeProvider(node);
	}

	public static class TreeProvider implements Provider<SectionTree>
	{
		@Inject
		private Injector injector;
		@Inject
		private RegistrationController registrationController;
		private final Key<SectionNode> rootNodeKey;

		public TreeProvider(NodeProvider node)
		{
			rootNodeKey = node.bindProvider();
		}

		@Override
		public SectionTree get()
		{
			DefaultSectionTree tree = new DefaultSectionTree(registrationController, injector.getProvider(rootNodeKey)
				.get());
			tree.treeFinished();
			return tree;
		}

	}

	public class NodeProvider implements NodeAnnotation, Provider<SectionNode>
	{
		@Inject
		private Injector injector;

		private Key<SectionNode> key;
		private final Key<? extends Section> sectionKey;
		private final List<Key<?>> children = new ArrayList<Key<?>>();
		private final List<Key<?>> innerChildren = new ArrayList<Key<?>>();
		private final String id;
		private String placeHolderId;
		private final Class<? extends Section> section;

		public NodeProvider(Class<? extends Section> clazz)
		{
			this(clazz, null);
		}

		public NodeProvider(Class<? extends Section> clazz, String id)
		{
			this.section = clazz;
			if( clazz != null )
			{
				this.sectionKey = Key.get(clazz);
				bind(clazz);
			}
			else
			{
				sectionKey = null;
			}
			this.id = id;
		}

		public NodeProvider placeHolder(String placeHolderId)
		{
			this.placeHolderId = placeHolderId;
			return this;
		}

		public NodeProvider innerChild(Class<? extends Section> section)
		{
			addSectionToList(innerChildren, section);
			return this;
		}

		public NodeProvider innerChild(NodeProvider provider)
		{
			innerChildren.add(provider.bindProvider());
			return this;
		}

		public NodeProvider innerChildren(Object... nodes)
		{
			addToList(innerChildren, nodes);
			return this;
		}

		public NodeProvider child(Class<? extends Section> section)
		{
			addSectionToList(children, section);
			return this;
		}

		public NodeProvider child(NodeProvider provider)
		{
			children.add(provider.bindProvider());
			return this;
		}

		public NodeProvider children(Object... children)
		{
			addToList(this.children, children);
			return this;
		}

		private void addToList(List<Key<?>> list, Object... children)
		{
			for( Object child : children )
			{
				if( child instanceof Class )
				{
					addSectionToList(list, ((Class<?>) child).asSubclass(Section.class));
				}
				else if( child instanceof NodeProvider )
				{
					list.add(((NodeProvider) child).bindProvider());
				}
			}
		}

		private void addSectionToList(List<Key<?>> list, Class<? extends Section> child)
		{
			list.add(Key.get(child));
			bind(child);
		}

		private Key<SectionNode> bindProvider()
		{
			if( key == null )
			{
				key = Key.get(SectionNode.class, this);
				bind(key).toProvider(this);
			}
			return key;
		}

		@Override
		public SectionNode get()
		{
			SectionNode node = new SectionNode();
			Section tempSection = null;
			if( sectionKey != null )
			{
				tempSection = injector.getInstance(sectionKey);
			}
			customize(tempSection);
			node.setSection(tempSection);
			node.setPlaceHolderId(placeHolderId);
			node.setId(id);
			Builder<Object> builder = new ImmutableList.Builder<Object>();
			for( Key<?> innerChild : innerChildren )
			{
				builder.add(injector.getInstance(innerChild));
			}
			node.setInnerChildren(builder.build());
			builder = new ImmutableList.Builder<Object>();
			for( Key<?> child : children )
			{
				builder.add(injector.getInstance(child));
			}
			node.setChildren(builder.build());
			return node;
		}

		@Override
		public Class<? extends Annotation> annotationType()
		{
			return NodeAnnotation.class;
		}

		@Override
		public Class<?> section()
		{
			return section;
		}

		@SuppressWarnings("unchecked")
		public <T extends Section> NodeProvider child(Key<T> key)
		{
			TypeLiteral<T> typeLiteral = key.getTypeLiteral();
			bind(typeLiteral).to((Class<? extends T>) typeLiteral.getRawType());
			children.add(key);
			return this;
		}

		protected void customize(Section section)
		{
			// nothing by default
		}
	}

	@Retention(RUNTIME)
	@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
	@BindingAnnotation
	public @interface NodeAnnotation
	{
		Class<?> section();
	}

}

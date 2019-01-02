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

package com.tle.web.sections.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

/**
 * An abstract {@code Section} class which must be declared as "prototype" scope
 * in Spring, in order for there to be one instance per usage in a
 * {@code SectionTree}. <br>
 * <p>
 * There is no requirement that {@code Section}s have one instance per use, and
 * it is perfectly legitimate to create a Singleton, however there are benefits
 * to making them "prototype" scoped:
 * <ul>
 * <li>You can store per {@link SectionTree} data in your instance rather than
 * storing it against the {@code SectionTree} itself (with
 * {@link SectionTree#setData(String, Object)}).
 * <li>Each instance knows which id it is registered under. Thus it can
 * implement {@link SectionId}.</li>
 * </ul>
 * 
 * @author jmaginnis
 * @param <M> The type of the {@code Section}'s model.
 */
@NonNullByDefault
public abstract class AbstractPrototypeSection<M> extends AbstractSection
{
	@Nullable
	private String id;
	@Nullable
	private SectionTree treeRegisteredIn;

	@Override
	public String getSectionId()
	{
		if( id == null )
		{
			throw new Error(getClass() + " is not registered yet"); //$NON-NLS-1$
		}
		return id;
	}

	@Override
	public String toString()
	{
		return (id == null ? getClass().getName() + " (unregistered)" : id); //$NON-NLS-1$
	}

	/**
	 * Create a {@link SectionContext} for this {@code Section}.
	 * 
	 * @param info The current info
	 * @return The newly created context.
	 */
	@Deprecated
	public SectionContext getContext(SectionInfo info)
	{
		return info.getContextForId(getSectionId());
	}

	/**
	 * Return this {@code Section}'s model.
	 * 
	 * @see SectionInfo#getModelForId(String)
	 * @param info The current info
	 * @return The model
	 */
	public M getModel(SectionInfo context)
	{
		return context.<M> getModelForId(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<M> getModelClass()
	{
		return (Class<M>) Object.class;
	}

	/**
	 * Records the id assigned to this {@code Section}, which can be retrieved
	 * via the {@code SectionId} interface.
	 */
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		if( treeRegisteredIn != null )
		{
			throw new Error(getClass().getName() + " Already registered in tree:" + treeRegisteredIn); //$NON-NLS-1$
		}
		treeRegisteredIn = tree;
		this.id = id;
	}

	@Override
	public SectionTree getTree()
	{
		return treeRegisteredIn;
	}

	@Override
	public Section getSectionObject()
	{
		return this;
	}
}

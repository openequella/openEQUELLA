/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.tle.jpfclasspath.model;


public class ModelProviderEvent implements IModelProviderEvent
{
	private int types;
	private Object source;
	private IModel[] added;
	private IModel[] removed;
	private IModel[] changed;

	public ModelProviderEvent(Object source, int types, IModel[] added, IModel[] removed, IModel[] changed)
	{
		this.source = source;
		this.types = types;
		this.added = added;
		this.removed = removed;
		this.changed = changed;
	}

	@Override
	public IModel[] getAddedModels()
	{
		return (added == null) ? new IModel[0] : added;
	}

	@Override
	public IModel[] getRemovedModels()
	{
		return (removed == null) ? new IModel[0] : removed;
	}

	@Override
	public IModel[] getChangedModels()
	{
		return (changed == null) ? new IModel[0] : changed;
	}

	@Override
	public int getEventTypes()
	{
		return types;
	}

	@Override
	public Object getEventSource()
	{
		return source;
	}
}

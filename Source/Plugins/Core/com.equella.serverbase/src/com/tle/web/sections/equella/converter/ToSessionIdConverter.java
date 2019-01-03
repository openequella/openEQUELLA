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

package com.tle.web.sections.equella.converter;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractConverter;
import net.entropysoft.transmorph.type.TypeReference;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.convert.Conversion.ConversionType;
import com.tle.web.sections.convert.SectionsConverter;
import com.tle.web.sections.events.RespondingListener;
import com.tle.web.sections.registry.handler.util.PropertyAccessor;

@Bind
@Singleton
public class ToSessionIdConverter extends AbstractConverter implements SectionsConverter
{
	@Inject
	private UserSessionService userSessionService;

	@Override
	public boolean canHandleDestinationType(TypeReference<?> destinationType)
	{
		return destinationType.isType(String.class);
	}

	@Override
	protected boolean canHandleSourceObject(Object sourceObject)
	{
		return sourceObject == null || SessionState.class.isAssignableFrom(sourceObject.getClass());
	}

	@Override
	public Object doConvert(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
		throws ConverterException
	{
		if( sourceObject != null )
		{
			SessionState state = (SessionState) sourceObject;
			if( ensureSessionObj(state) )
			{
				return state.getBookmarkString();
			}
		}
		return null;
	}

	private boolean ensureSessionObj(SessionState state)
	{
		if( state.isRemoved() )
		{
			userSessionService.removeAttribute(state.getSessionId());
			return false;
		}
		if( state.isNew() )
		{
			String unique = userSessionService.createUniqueKey();
			state.setBookmarkString(unique);
			userSessionService.setAttribute(unique, state);
			state.synced();
		}
		return true;
	}

	@Override
	public void registerBookmark(SectionTree tree, final SectionId sectionId, final PropertyAccessor readAccessor,
		PropertyAccessor writeAccessor, TypeReference<?> typeRef)
	{
		if( typeRef.isRawTypeSubOf(SessionState.class) )
		{
			tree.addListener(null, RespondingListener.class, new RespondingListener()
			{
				@Override
				public void responding(SectionInfo info)
				{
					Object model = info.getModelForId(sectionId.getSectionId());
					try
					{
						SessionState state = (SessionState) readAccessor.read(model);
						if( state != null && ensureSessionObj(state) && state.isModified() )
						{
							userSessionService.setAttribute(state.getSessionId(), state);
							state.synced();
						}
					}
					catch( Exception e )
					{
						throw new SectionsRuntimeException(e);
					}
				}
			});
		}
	}

	@Override
	public boolean supports(String type)
	{
		return (type.equals(ConversionType.TOPARAMS.name()));
	}
}

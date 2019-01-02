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
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.convert.Conversion.ConversionType;
import com.tle.web.sections.convert.SectionsConverter;
import com.tle.web.sections.registry.handler.util.PropertyAccessor;

@Bind
@Singleton
public class FromSessionIdConverter extends AbstractConverter implements SectionsConverter
{
	@Inject
	private UserSessionService userSessionService;

	@Override
	protected boolean canHandleDestinationType(TypeReference<?> destinationType)
	{
		return destinationType.isRawTypeSubOf(SessionState.class);
	}

	@Override
	protected boolean canHandleSourceObject(Object sourceObject)
	{
		return sourceObject == null || sourceObject.getClass() == String.class;
	}

	@Override
	public Object doConvert(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
		throws ConverterException
	{
		try
		{
			SessionState sessionObj = (SessionState) destinationType.getRawType().newInstance();
			sessionObj.setBookmarkString((String) sourceObject);
			return userSessionService.getAttribute(sessionObj.getSessionId());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void registerBookmark(SectionTree tree, SectionId sectionId, PropertyAccessor readAccessor,
		PropertyAccessor writeAccessor, TypeReference<?> typeRef)
	{
		// nothing
	}

	@Override
	public boolean supports(String convertId)
	{
		return convertId.equals(ConversionType.FROMPARAMS.name());
	}

}

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

package com.tle.web.viewitem.section;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.core.office2html.service.Office2HtmlConversionService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewurl.ItemUrlExtender;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.WrappedViewItemResource;

public class ConversionSection extends AbstractPrototypeSection<ConversionSection.ConversionModel>
	implements
		ViewItemFilter
{
	@Inject
	private Office2HtmlConversionService conversionService;
	@TreeLookup
	private RootItemFileSection rootSection;

	public static class ConversionModel
	{
		@Bookmarked
		private String convert;

		public String getConvert()
		{
			return convert;
		}

		public void setConvert(String convert)
		{
			this.convert = convert;
		}
	}

	public static class ConversionUrl implements ItemUrlExtender
	{
		private static final long serialVersionUID = 1L;
		private final String type;

		public ConversionUrl(String type)
		{
			this.type = type;
		}

		@Override
		public void execute(SectionInfo info)
		{
			ConversionSection section = info.lookupSection(ConversionSection.class);
			section.setConvert(info, type);
		}
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addFilterMapping(Type.ALWAYS, this);
	}

	@Override
	public Class<ConversionModel> getModelClass()
	{
		return ConversionModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "convert"; //$NON-NLS-1$
	}

	@Override
	public int getOrder()
	{
		return -300;
	}

	public class ConversionResource extends WrappedViewItemResource
	{
		private final String type;

		public ConversionResource(ViewItemResource resource, String convert)
		{
			super(resource);
			this.type = convert;
		}

		@Override
		public Bookmark createCanonicalURL()
		{
			try
			{
				String convertedFile = conversionService.convert(getViewableItem().getFileHandle(), getFilepath(),
					type);
				return getViewableItem().createStableResourceUrl(convertedFile);
			}
			catch( Exception e )
			{
				throw new SectionsRuntimeException(e);
			}
		}

	}

	public void setConvert(SectionInfo info, String convert)
	{
		getModel(info).setConvert(convert);
	}

	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		String conversionType = getModel(info).getConvert();
		if( Check.isEmpty(conversionType) )
		{
			return resource;
		}
		return new ConversionResource(resource, conversionType);
	}
}

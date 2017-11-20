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

package com.tle.core.qti.converter.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.qti.dao.QtiAssessmentTestDao;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class QtiAssessmentTestXmlConverter implements Converter
{
	private final QtiAssessmentTestDao testDao;

	public QtiAssessmentTestXmlConverter(QtiAssessmentTestDao testDao)
	{
		this.testDao = testDao;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz)
	{
		return clazz == QtiAssessmentTest.class;
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		final QtiAssessmentTest test = (QtiAssessmentTest) obj;
		if( test != null )
		{
			writer.addAttribute("uuid", test.getUuid());
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		final String uuidFromStream = reader.getAttribute("uuid");
		return testDao.getByUuid(uuidFromStream);
	}
}

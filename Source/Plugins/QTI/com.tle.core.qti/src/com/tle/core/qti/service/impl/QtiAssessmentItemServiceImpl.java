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

package com.tle.core.qti.service.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.XMLConstants;

import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.core.guice.Bind;
import com.tle.core.qti.dao.QtiAssessmentItemDao;
import com.tle.core.qti.service.QtiAssessmentItemService;

import uk.ac.ed.ph.jqtiplus.attribute.value.StringAttribute;
import uk.ac.ed.ph.jqtiplus.internal.util.DumpMode;
import uk.ac.ed.ph.jqtiplus.internal.util.ObjectDumper;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind(QtiAssessmentItemService.class)
@Singleton
public class QtiAssessmentItemServiceImpl implements QtiAssessmentItemService
{
	@Inject
	private QtiAssessmentItemDao dao;

	@Nullable
	@Override
	public QtiAssessmentItem convertItemToEntity(ResolvedAssessmentTest test, ResolvedAssessmentItem question)
	{
		final AssessmentItem questionItem = question.getItemLookup().extractIfSuccessful();
		// protect against shit packages
		if( questionItem != null )
		{
			final QtiAssessmentItem questionEntity = new QtiAssessmentItem();
			questionEntity.setUuid(UUID.randomUUID().toString());
			questionEntity.setInstitution(CurrentInstitution.get());

			questionEntity.setIdentifier(questionItem.getIdentifier());
			questionEntity.setTitle(questionItem.getTitle());
			questionEntity.setAdaptive(questionItem.getAdaptive());
			questionEntity.setLabel(questionItem.getLabel());
			// getLang() has a bug where it doesn't use correct namespace
			StringAttribute lang = (StringAttribute) questionItem.getAttributes().get(AssessmentItem.ATTR_LANG_NAME,
				XMLConstants.XML_NS_URI);
			questionEntity.setLang(lang.getComputedValue());
			questionEntity.setTimeDependent(questionItem.getTimeDependent());

			// FIXME: this is not very useful
			ItemBody itemBody = questionItem.getItemBody();
			if( itemBody != null )
			{
				questionEntity.setItemBody(ObjectDumper.dumpObject(itemBody, DumpMode.DEEP));
			}

			return questionEntity;
		}
		return null;
	}

	@Transactional
	@Override
	public void save(QtiAssessmentItem question) throws InvalidDataException
	{
		final Institution institution = question.getInstitution();
		if( institution == null )
		{
			question.setInstitution(CurrentInstitution.get());
		}
		dao.save(question);
	}
}

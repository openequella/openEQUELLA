package com.tle.core.qti.service.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.XMLConstants;

import org.springframework.transaction.annotation.Transactional;

import uk.ac.ed.ph.jqtiplus.attribute.value.StringAttribute;
import uk.ac.ed.ph.jqtiplus.internal.util.DumpMode;
import uk.ac.ed.ph.jqtiplus.internal.util.ObjectDumper;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.core.guice.Bind;
import com.tle.core.qti.dao.QtiAssessmentItemDao;
import com.tle.core.qti.service.QtiAssessmentItemService;
import com.tle.core.user.CurrentInstitution;

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

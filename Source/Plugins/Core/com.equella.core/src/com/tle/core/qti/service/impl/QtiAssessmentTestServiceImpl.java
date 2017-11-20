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

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.oauth.convert.OAuthTokenConverter;
import com.tle.core.plugins.AbstractPluginService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.PathUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.guice.Bind;
import com.tle.core.item.event.ItemDeletedEvent;
import com.tle.core.item.event.listener.ItemDeletedListener;
import com.tle.core.qti.dao.QtiAssessmentItemDao;
import com.tle.core.qti.dao.QtiAssessmentItemRefDao;
import com.tle.core.qti.dao.QtiAssessmentResultDao;
import com.tle.core.qti.dao.QtiAssessmentTestDao;
import com.tle.core.qti.service.QtiAssessmentItemService;
import com.tle.core.qti.service.QtiAssessmentTestService;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.RootNodeLookup;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(QtiAssessmentTestService.class)
@Singleton
public class QtiAssessmentTestServiceImpl implements QtiAssessmentTestService, ItemDeletedListener
{
	private static final String PREFIX = AbstractPluginService.getMyPluginId(OAuthTokenConverter.class)+".";
	@Inject
	private QtiAssessmentTestDao qtiTestDao;
	@Inject
	private QtiAssessmentItemRefDao qtiItemRefDao;
	@Inject
	private QtiAssessmentItemDao qtiItemDao;
	@Inject
	private QtiAssessmentResultDao qtiResultDao;

	@Inject
	private QtiAssessmentItemService questionService;

	@Override
	public QtiAssessmentTest getByUuid(String uuid)
	{
		return qtiTestDao.getByUuid(uuid);
	}

	@Nullable
	@Override
	public QtiAssessmentTest findByUuid(String uuid)
	{
		return qtiTestDao.findByUuid(uuid);
	}

	@Nullable
	@Override
	public QtiAssessmentTest findByItem(Item item)
	{
		return qtiTestDao.findByItem(item);
	}

	@Override
	public QtiAssessmentTest convertTestToEntity(ResolvedAssessmentTest resQuiz, Item item, String xmlPath,
		String testUuid)
	{
		final RootNodeLookup<AssessmentTest> testLookup = resQuiz.getTestLookup();
		final AssessmentTest quiz = testLookup.extractIfSuccessful();
		final QtiAssessmentTest testEntity = new QtiAssessmentTest();
		testEntity.setUuid(testUuid);
		testEntity.setIdentifier(quiz.getIdentifier());
		testEntity.setTestPartIdentifier(getFirstTestPart(quiz).getIdentifier().toString());
		testEntity.setInstitution(CurrentInstitution.get());
		testEntity.setItem(item);
		testEntity.setTitle(quiz.getTitle());
		testEntity.setXmlPath(xmlPath);

		final String testRootPath = PathUtils.getParentFolderFromFilepath(xmlPath);
		final List<QtiAssessmentItemRef> questionRefEntities = Lists.newArrayList();
		final List<AssessmentItemRef> assessmentItemRefs = resQuiz.getAssessmentItemRefs();
		for( AssessmentItemRef itemRef : assessmentItemRefs )
		{
			final URI uri = itemRef.getHref();
			// this is a path relative to the test XML
			String relPath = uri.toString();
			String fullItemPath = PathUtils.filePath(testRootPath, relPath);
			final ResolvedAssessmentItem resolvedAssessmentItem = resQuiz.getResolvedAssessmentItemBySystemIdMap()
				.get(URI.create(fullItemPath));
			final QtiAssessmentItem assessmentItem = questionService.convertItemToEntity(resQuiz,
				resolvedAssessmentItem);

			// protect against shit packages
			if( assessmentItem != null )
			{
				final QtiAssessmentItemRef questionRefEntity = new QtiAssessmentItemRef();
				questionRefEntity.setTest(testEntity);
				questionRefEntity.setUuid(UUID.randomUUID().toString());
				questionRefEntity.setXmlPath(fullItemPath);
				questionRefEntity.setIdentifier(itemRef.getIdentifier().toString());
				// itemRef.getWeights();
				questionRefEntity.setQuestion(assessmentItem);

				questionRefEntities.add(questionRefEntity);
			}
		}

		testEntity.getQuestionRefs().clear();
		testEntity.getQuestionRefs().addAll(questionRefEntities);

		return testEntity;
	}

	private TestPart getFirstTestPart(AssessmentTest quiz)
	{
		final List<TestPart> testParts = quiz.getTestParts();
		if( testParts == null || testParts.size() == 0 )
		{
			throw new Error("No test parts in the this test");
		}
		return testParts.get(0);
	}

	@Transactional
	@Override
	public void save(QtiAssessmentTest test) throws InvalidDataException
	{
		final Institution institution = test.getInstitution();
		if( institution == null )
		{
			test.setInstitution(CurrentInstitution.get());
		}
		validate(test);

		List<QtiAssessmentItemRef> questions = test.getQuestionRefs();
		for( QtiAssessmentItemRef itemRef : questions )
		{
			QtiAssessmentItem question = itemRef.getQuestion();
			questionService.save(question);
		}
		qtiTestDao.save(test);
	}

	@Override
	public void validate(QtiAssessmentTest test) throws InvalidDataException
	{
		final List<ValidationError> errors = Lists.newArrayList();
		if( test.getItem() == null )
		{
			errors.add(new ValidationError("item", CurrentLocale.get(PREFIX + "test.validation.noitem")));
		}
		if( errors.size() > 0 )
		{
			throw new InvalidDataException(errors);
		}
	}

	@Override
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		deleteForItemId(event.getKey());
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteForItemId(long itemId)
	{
		final QtiAssessmentTest test = qtiTestDao.findByItemId(itemId);
		if( test != null )
		{
			delete(test);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void delete(QtiAssessmentTest test)
	{
		final List<QtiAssessmentItem> items = Lists.newArrayList();

		final List<QtiAssessmentItemRef> questionRefs = test.getQuestionRefs();
		final List<QtiAssessmentResult> results = qtiResultDao.findByAssessmentTest(test);
		for( QtiAssessmentResult result : results )
		{
			result.setTest(test);
			qtiResultDao.delete(result);
		}

		// Note! Questions will not be deleted when they can be
		// disassociated from the test item
		for( QtiAssessmentItemRef itemRef : questionRefs )
		{
			items.add(itemRef.getQuestion());
		}
		qtiTestDao.delete(test);

		for( QtiAssessmentItem item : items )
		{
			qtiItemDao.delete(item);
		}

		qtiTestDao.flush();
		qtiItemDao.flush();
		qtiResultDao.flush();
		qtiItemRefDao.flush();
	}
}

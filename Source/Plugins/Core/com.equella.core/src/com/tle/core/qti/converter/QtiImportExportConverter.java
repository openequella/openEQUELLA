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

package com.tle.core.qti.converter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.NameValue;
import com.tle.common.beans.xml.IdOnlyConverter;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.common.qti.entity.QtiItemResult;
import com.tle.common.qti.entity.QtiItemVariable;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.qti.converter.initialiser.QtiObjectInitialiserCallback;
import com.tle.core.qti.converter.xstream.QtiAssessmentItemRefXmlConverter;
import com.tle.core.qti.converter.xstream.QtiAssessmentItemXmlConverter;
import com.tle.core.qti.converter.xstream.QtiAssessmentTestXmlConverter;
import com.tle.core.qti.dao.QtiAssessmentItemDao;
import com.tle.core.qti.dao.QtiAssessmentItemRefDao;
import com.tle.core.qti.dao.QtiAssessmentResultDao;
import com.tle.core.qti.dao.QtiAssessmentTestDao;
import com.tle.core.qti.dao.QtiItemResultDao;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class QtiImportExportConverter extends AbstractConverter<Object>
{
	private static final String PREFIX = AbstractPluginService.getMyPluginId(QtiImportExportConverter.class)+".";
	private static final String TEST_IMPORT_EXPORT_FOLDER = "qtitest";
	private static final String QUESTION_IMPORT_EXPORT_FOLDER = "qtiquestion";
	private static final String ASSESSMENT_RESULT_IMPORT_EXPORT_FOLDER = "qtiassessmentresult";
	private static final String ITEM_RESULT_IMPORT_EXPORT_FOLDER = "qtiitemresult";

	@Inject
	private QtiAssessmentResultDao assessmentResultDao;
	@Inject
	private QtiItemResultDao itemResultDao;
	@Inject
	private QtiAssessmentTestDao assessmentTestDao;
	@Inject
	private QtiAssessmentItemDao assessmentItemDao;
	@Inject
	private QtiAssessmentItemRefDao assessmentItemRefDao;

	private static SubTemporaryFile getTestFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, TEST_IMPORT_EXPORT_FOLDER);
	}

	private static SubTemporaryFile getQuestionFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, QUESTION_IMPORT_EXPORT_FOLDER);
	}

	private static SubTemporaryFile getAssessmentResultFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, ASSESSMENT_RESULT_IMPORT_EXPORT_FOLDER);
	}

	private static SubTemporaryFile getItemResultFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, ITEM_RESULT_IMPORT_EXPORT_FOLDER);
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		itemResultDao.deleteAll();
		assessmentResultDao.deleteAll();
		assessmentTestDao.deleteAll();
		assessmentItemDao.deleteAll();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		// Questions are independent of tests
		final XStream xstream = xmlHelper.createXStream(getClass().getClassLoader());

		// Export questions
		final Iterator<QtiAssessmentItem> questions = assessmentItemDao.getIterator();
		final SubTemporaryFile allQuestionsFolder = getQuestionFolder(staging);
		xmlHelper.writeExportFormatXmlFile(allQuestionsFolder, true);
		while( questions.hasNext() )
		{
			final QtiAssessmentItem question = questions.next();
			final BucketFile bucketFolder = new BucketFile(allQuestionsFolder, question.getId());
			question.setInstitution(null);
			xmlHelper.writeXmlFile(bucketFolder, question.getId() + ".xml", question, xstream);
		}

		xstream.registerConverter(new IdOnlyConverter(Item.class));
		xstream.registerConverter(new QtiAssessmentItemXmlConverter(assessmentItemDao));

		final SubTemporaryFile allTestsExportFolder = getTestFolder(staging);
		xmlHelper.writeExportFormatXmlFile(allTestsExportFolder, true);

		final QtiObjectInitialiserCallback qtiInitialiser = new QtiObjectInitialiserCallback();
		for( QtiAssessmentTest test : assessmentTestDao.enumerateAll() )
		{
			initialiserService.initialise(test, qtiInitialiser);

			final BucketFile bucketFolder = new BucketFile(allTestsExportFolder, test.getId());
			test.setInstitution(null);
			xmlHelper.writeXmlFile(bucketFolder, test.getId() + ".xml", test, xstream);
		}

		final XStream resultXstream = xmlHelper.createXStream(getClass().getClassLoader());
		resultXstream.registerConverter(new QtiAssessmentItemRefXmlConverter(assessmentItemRefDao));
		resultXstream.registerConverter(new QtiAssessmentTestXmlConverter(assessmentTestDao));

		final SubTemporaryFile allItemResultsFolder = getItemResultFolder(staging);
		final Iterator<QtiItemResult> itemResults = itemResultDao.getIterator();
		xmlHelper.writeExportFormatXmlFile(allItemResultsFolder, true);
		while( itemResults.hasNext() )
		{
			final QtiItemResult itemResult = itemResults.next();
			initialiserService.initialise(itemResult, qtiInitialiser);

			final BucketFile bucketFolder = new BucketFile(allItemResultsFolder, itemResult.getId());
			xmlHelper.writeXmlFile(bucketFolder, itemResult.getId() + ".xml", itemResult, resultXstream);
		}

		resultXstream.registerConverter(new IdOnlyConverter(QtiItemResult.class));
		final SubTemporaryFile allAssessmentResultsFolder = getAssessmentResultFolder(staging);
		final Iterator<QtiAssessmentResult> assessmentResults = assessmentResultDao.getIterator();
		xmlHelper.writeExportFormatXmlFile(allAssessmentResultsFolder, true);
		while( assessmentResults.hasNext() )
		{
			final QtiAssessmentResult assessmentResult = assessmentResults.next();
			initialiserService.initialise(assessmentResult, qtiInitialiser);

			final BucketFile bucketFolder = new BucketFile(allAssessmentResultsFolder, assessmentResult.getId());
			xmlHelper.writeXmlFile(bucketFolder, assessmentResult.getId() + ".xml", assessmentResult, resultXstream);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		// Questions independent of tests
		final XStream xstream = xmlHelper.createXStream(getClass().getClassLoader());

		final SubTemporaryFile allQuestionsImportFolder = getQuestionFolder(staging);
		for( String entry : xmlHelper.getXmlFileList(allQuestionsImportFolder) )
		{
			final QtiAssessmentItem question = xmlHelper.readXmlFile(allQuestionsImportFolder, entry, xstream);
			question.setId(0);
			question.setInstitution(institution);

			assessmentItemDao.save(question);
			assessmentItemDao.flush();
			assessmentItemDao.clear();
		}

		xstream.registerConverter(new IdOnlyConverter(Item.class));
		xstream.registerConverter(new QtiAssessmentItemXmlConverter(assessmentItemDao));

		final SubTemporaryFile allTestsImportFolder = getTestFolder(staging);
		for( String entry : xmlHelper.getXmlFileList(allTestsImportFolder) )
		{
			final QtiAssessmentTest test = xmlHelper.readXmlFile(allTestsImportFolder, entry, xstream);
			test.setId(0);
			test.setInstitution(institution);

			// Really shouldn't have to do this switcheroo manually
			Map<Long, Long> itemMap = params.getItems();
			Long newItemId = itemMap.get(test.getItem().getId());
			test.getItem().setId(newItemId.longValue());

			for( QtiAssessmentItemRef testItemRef : test.getQuestionRefs() )
			{
				testItemRef.setId(0);
				testItemRef.setTest(test);
			}

			assessmentTestDao.save(test);
			assessmentTestDao.flush();
			assessmentTestDao.clear();
		}

		final XStream resultXstream = xmlHelper.createXStream(getClass().getClassLoader());
		resultXstream.registerConverter(new QtiAssessmentItemRefXmlConverter(assessmentItemRefDao));
		resultXstream.registerConverter(new QtiAssessmentTestXmlConverter(assessmentTestDao));

		final Map<Long, QtiItemResult> itemResultOldIdMap = Maps.newHashMap();
		final SubTemporaryFile allItemResultsFolder = getItemResultFolder(staging);
		for( String entry : xmlHelper.getXmlFileList(allItemResultsFolder) )
		{
			final QtiItemResult itemResult = xmlHelper.readXmlFile(allItemResultsFolder, entry, resultXstream);
			itemResultOldIdMap.put(itemResult.getId(), itemResult);
			itemResult.setId(0);

			for( QtiItemVariable itemVariable : itemResult.getItemVariables() )
			{
				itemVariable.setId(0);
			}
		}

		resultXstream.registerConverter(new IdOnlyConverter(QtiItemResult.class));
		final SubTemporaryFile allAssessmentResultsFolder = getAssessmentResultFolder(staging);
		for( String entry : xmlHelper.getXmlFileList(allAssessmentResultsFolder) )
		{
			final QtiAssessmentResult assessmentResult = xmlHelper.readXmlFile(allAssessmentResultsFolder, entry,
				resultXstream);
			assessmentResult.setId(0);

			final List<QtiItemResult> itemResults = Lists.newArrayList(assessmentResult.getItemResults());
			assessmentResult.getItemResults().clear();
			for( QtiItemResult result : itemResults )
			{
				final QtiItemResult newResult = itemResultOldIdMap.get(result.getId());
				newResult.setAssessmentResult(assessmentResult);
				assessmentResult.getItemResults().add(newResult);
			}

			for( QtiItemVariable itemVariable : assessmentResult.getItemVariables() )
			{
				itemVariable.setId(0);
			}

			// Will cascade itemResults and itemVariables
			assessmentResultDao.save(assessmentResult);
			assessmentResultDao.flush();
			assessmentResultDao.clear();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	@Override
	protected NameValue getStandardTask()
	{
		return new BundleNameValue(PREFIX + "converter.qtitest.name", getStringId());
	}

	@Override
	public String getStringId()
	{
		return "QTI";
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( !params.hasFlag(ConverterParams.NO_ITEMS) )
		{
			super.addTasks(type, tasks, params);
		}
	}
}

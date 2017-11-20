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

package com.tle.core.activation.service.impl;

import static com.tle.common.security.SecurityConstants.EDIT_VIRTUAL_BASE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.BulkImport;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.Utils;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.settings.standard.CourseDefaultsSettings;
import com.tle.common.security.SecurityConstants;
import com.tle.common.util.CsvReader;
import com.tle.common.util.DateHelper;
import com.tle.common.util.Dates;
import com.tle.core.activation.CourseInfoDao;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.services.ValidationHelper;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.remoting.RemoteCourseInfoService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.settings.service.ConfigurationService;

@Bind(CourseInfoService.class)
@Singleton
@SecureEntity(RemoteCourseInfoService.ENTITY_TYPE)
public class CourseInfoServiceImpl extends AbstractEntityServiceImpl<EntityEditingBean, CourseInfo, CourseInfoService>
	implements
		CourseInfoService
{
	private static final String[] NON_BLANKS = {"name", "uuid", "code"}; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$

	private static final String CODE_COLUMN = "Code"; //$NON-NLS-1$
	private static final String NAME_COLUMN = "Name"; //$NON-NLS-1$
	private static final String DESCRIPTION_COLUMN = "Description"; //$NON-NLS-1$
	private static final String CITATION_COLUMN = "Citation"; //$NON-NLS-1$
	private static final String START_COLUMN = "Start"; //$NON-NLS-1$
	private static final String END_COLUMN = "End"; //$NON-NLS-1$
	private static final String STUDENTS_COLUMN = "Students"; //$NON-NLS-1$
	private static final String TYPE_COLUMN = "Type"; //$NON-NLS-1$
	private static final String DEPT_COLUMN = "DepartmentName"; //$NON-NLS-1$
	private static final String ARCHIVED_COLUMN = "Archived"; //$NON-NLS-1$

	private final CourseInfoDao dao;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	public CourseInfoServiceImpl(CourseInfoDao dao)
	{
		super(Node.COURSE_INFO, dao);
		this.dao = dao;
	}

	public List<CourseInfo> enumerateAll()
	{
		return dao.enumerateAll();
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, CourseInfo> session, CourseInfo entity,
		List<ValidationError> errors)
	{
		ValidationHelper.checkBlankFields(entity, NON_BLANKS, errors);

		String code = entity.getCode();
		CourseInfo existingWithCode = getByCode(code);

		// is it the SAME ONE?
		if( existingWithCode != null && !entity.getUuid().equals(existingWithCode.getUuid()) )
		{
			errors.add(new ValidationError("code", CurrentLocale //$NON-NLS-1$
				.get("com.tle.core.services.entity.course.validation.unique.code"))); //$NON-NLS-1$
		}
	}

	/**
	 * @return A list of the offending object names that are holding onto this
	 *         Entity
	 */
	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		return dao.getReferencingClasses(id);
	}

	@Override
	public List<String> getAllCitations()
	{
		return dao.getAllCitations();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void edit(CourseInfo course)
	{
		CourseInfo courseEdit = getByCode(course.getCode());
		EntityPack<CourseInfo> pack = startEdit(courseEdit);
		courseEdit = pack.getEntity();
		courseEdit.setName(course.getName());
		courseEdit.setDescription(course.getDescription());
		courseEdit.setCitation(course.getCitation());
		courseEdit.setFrom(course.getFrom());
		courseEdit.setUntil(course.getUntil());
		courseEdit.setStudents(course.getStudents());
		courseEdit.setCourseType(course.getCourseType());
		courseEdit.setDepartmentName(course.getDepartmentName());
		// TODO This is here because AbstractEntityService calls clear() on the
		// session!
		dao.flush();
		stopEdit(pack, true);
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public List<CourseInfo> bulkImport(byte[] file, boolean override)
	{
		return bulkImport(new CsvReader(new ByteArrayInputStream(file), Charset.forName("UTF-8")), override); //$NON-NLS-1$
	}

	@Override
	public CourseInfo getByCode(String code)
	{
		return dao.findByCriteria(Restrictions.eq("code", code), getInstitutionCriterion()); //$NON-NLS-1$
	}

	@Override
	public void prepareImport(TemporaryFileHandle importFolder, CourseInfo entity, ConverterParams params)
	{
		super.prepareImport(importFolder, entity, params);

		// they didn't always have a code field.
		if( entity.getCode() == null )
		{
			entity.setCode(entity.getUuid());
		}
	}

	@Override
	@SecureOnReturn(priv = EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public List<CourseInfo> bulkImport(CsvReader reader, boolean override)
	{
		CourseDefaultsSettings persistedSettings = configurationService.getProperties(new CourseDefaultsSettings());
		final Date defaultFromDate = DateHelper.parseOrNullDate(persistedSettings.getStartDate(),
			Dates.CALENDAR_CONTROL_FORM);
		final Date defaultToDate = DateHelper.parseOrNullDate(persistedSettings.getEndDate(),
			Dates.CALENDAR_CONTROL_FORM);

		return new BulkImport<CourseInfo>()
		{
			private EntityPack<CourseInfo> pack;

			@Override
			@Transactional
			protected void processOne(CsvReader reader, boolean override, java.util.List<CourseInfo> objects)
				throws IOException, Exception
			{
				super.processOne(reader, override, objects);
			}

			@Override
			public void add(CourseInfo t)
			{
				CourseInfoServiceImpl.this.add(pack, false);
			}

			@Override
			public CourseInfo createNew()
			{
				CourseInfo i = new CourseInfo();
				i.setUuid(UUID.randomUUID().toString());
				pack = new EntityPack<CourseInfo>(i, null);
				return i;
			}

			@Override
			public void edit(CourseInfo t) throws Exception
			{
				stopEdit(pack, true);
			}

			@Override
			public CourseInfo getOld(CsvReader reader) throws IOException
			{
				try
				{
					return getByCode(reader.get(CODE_COLUMN));
				}
				catch( NotFoundException e )
				{
					// Do nothing
				}
				return null;
			}

			@Override
			public void update(CsvReader reader, CourseInfo course, boolean create) throws Exception
			{
				Locale locale = CurrentLocale.getLocale();

				LanguageBundle nameBundle = new LanguageBundle();
				LanguageBundle descBundle = new LanguageBundle();

				if( create )
				{
					course.setCode(reader.get(CODE_COLUMN));
				}
				else
				{
					pack = startEdit(course);
					course = pack.getEntity();
					dao.flush();
				}

				String name = reader.get(NAME_COLUMN);
				LangUtils.setString(nameBundle, locale, Check.isEmpty(name) ? course.getCode() : name);
				LangUtils.setString(descBundle, locale, reader.get(DESCRIPTION_COLUMN));

				course.setName(nameBundle);
				course.setDescription(descBundle);
				course.setCitation(reader.get(CITATION_COLUMN));
				Date fromDate = DateHelper.parseOrNullDate(reader.get(START_COLUMN), Dates.CALENDAR_CONTROL_FORM);
				if( fromDate == null )
				{
					fromDate = defaultFromDate;
				}
				course.setFrom(fromDate);
				Date toDate = DateHelper.parseOrNullDate(reader.get(END_COLUMN), Dates.CALENDAR_CONTROL_FORM);
				if( toDate == null )
				{
					toDate = defaultToDate;
				}
				course.setUntil(toDate);
				course.setStudents(Utils.parseInt(reader.get(STUDENTS_COLUMN), 1));
				String type = reader.get(TYPE_COLUMN);
				if( !Check.isEmpty(type) )
				{
					course.setCourseType(type.toLowerCase().charAt(0));
				}
				course.setDepartmentName(reader.get(DEPT_COLUMN));
				course.setDisabled(Utils.parseLooseBool(reader.get(ARCHIVED_COLUMN), false));
			}
		}.bulkImport(reader, override);
	}

	@Override
	protected void processClone(EntityPack<CourseInfo> pack)
	{
		CourseInfo course = pack.getEntity();
		course.setCode(createUniqueCode(course));
	}

	private String createUniqueCode(CourseInfo course)
	{
		StringBuilder code = new StringBuilder(course.getCode());
		// find a unique code...
		CourseInfo existing = getByCode(code.toString());
		while( existing != null )
		{
			code.append(" Copy");
			existing = getByCode(code.toString());
		}
		return code.toString();
	}
}

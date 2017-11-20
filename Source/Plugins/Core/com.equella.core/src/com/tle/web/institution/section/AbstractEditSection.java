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

package com.tle.web.institution.section;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import com.tle.beans.DatabaseSchema;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.util.DateHelper;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.migration.MigrationService;
import com.tle.web.institution.database.DatabaseTabUtils;
import com.tle.web.institution.database.SelectDatabaseDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.EqualityExpression;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.HiddenState;
import com.tle.web.sections.standard.NumberField;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public abstract class AbstractEditSection<M extends EditInstitutionModel> extends AbstractPrototypeSection<M>
	implements
		HtmlRenderer
{
	@PlugKey("institution.options.items")
	private static Label ITEMS_LABEL;
	@PlugKey("institution.options.items.attachments")
	private static Label ITEM_ATTACHMENTS_LABEL;
	@PlugKey("institution.options.auditlogs")
	private static Label AUDITLOGS_LABEL;

	@PlugKey("institutions.edit.notmatch")
	private static Label LABEL_PASSWORDMATCH;
	@PlugKey("institutions.edit.proceed")
	private static Label LABEL_PROCEED;
	@PlugKey("institutions.edit.timezone.systemname")
	private static String KEY_TIMEZONE_SYSTEM;

	@Inject
	private InstitutionImportService instImportService;
	@Inject
	private MigrationService migrationService;
	@Inject
	private DatabaseTabUtils databaseTabUtils;

	@TreeLookup
	private ProgressSection progressSection;
	@EventFactory
	private EventGenerator events;

	@Component
	private HiddenState selectedDatabase;
	@Component
	@PlugKey("institution.details.schema.select")
	private Button selectDatabase;
	@Inject
	@Component
	private SelectDatabaseDialog selectDatabaseDialog;

	@Component(stateful = false)
	private TextField adminPassword;
	@Component(stateful = false)
	private TextField adminConfirm;
	@Component
	private TextField name;
	@Component
	private TextField url;
	@Component
	private NumberField limit;
	@Component
	private TextField filestore;
	@Component
	private SingleSelectionList<NameValue> timeZones;
	@Component
	private Button actionButton;
	@Component
	@PlugKey("institutions.edit.cancel")
	private Button cancelButton;
	@Component
	private Checkbox itemsCheck;
	@Component
	private Checkbox attachmentsCheck;
	@Component
	private Checkbox auditlogsCheck;

	public Button getSelectDatabase()
	{
		return selectDatabase;
	}

	public TextField getAdminPassword()
	{
		return adminPassword;
	}

	public TextField getAdminConfirm()
	{
		return adminConfirm;
	}

	public TextField getName()
	{
		return name;
	}

	public TextField getUrl()
	{
		return url;
	}

	public TextField getFilestore()
	{
		return filestore;
	}

	public NumberField getLimit()
	{
		return limit;
	}

	public SingleSelectionList<NameValue> getTimeZones()
	{
		return timeZones;
	}

	public Button getActionButton()
	{
		return actionButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		itemsCheck.setLabel(ITEMS_LABEL);

		itemsCheck.addEventStatements(JSHandler.EVENT_CHANGE, new FunctionCallStatement(
			attachmentsCheck.createDisableFunction(), new NotExpression(itemsCheck.createGetExpression())));

		attachmentsCheck.setLabel(ITEM_ATTACHMENTS_LABEL);
		auditlogsCheck.setLabel(AUDITLOGS_LABEL);

		adminPassword.setAutocompleteDisabled(true);
		adminConfirm.setAutocompleteDisabled(true);

		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
		SubmitValuesHandler handler = events.getNamedHandler("doAction");
		handler.addValidator(new SimpleValidator(
			new EqualityExpression(adminPassword.createGetExpression(), adminConfirm.createGetExpression()))
				.setFailureStatements(Js.alert_s(LABEL_PASSWORDMATCH)));
		handler.addValidator(new Confirm(LABEL_PROCEED));
		actionButton.setClickHandler(handler);

		timeZones.setListModel(new SimpleHtmlListModel<NameValue>(
			DateHelper.getTimeZoneNameValues(new BundleNameValue(KEY_TIMEZONE_SYSTEM, ""), false)));

		selectDatabaseDialog.setOkCallback(events.getSubmitValuesFunction("schemaSelected"));
		selectDatabase.setClickHandler(selectDatabaseDialog.getOpenFunction());

		limit.setIntegersOnly(false);
		limit.setMin(0);
		limit.setStep(0.1);
		limit.setDefaultNumber(0.0);
	}

	@EventHandlerMethod
	public abstract void doAction(SectionInfo info);

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		SectionUtils.clearModel(info, this);
	}

	@EventHandlerMethod
	public void schemaSelected(SectionInfo info, long schemaId)
	{
		getModel(info).setNavigateAway(false);
		selectedDatabase.setValue(info, schemaId);
	}

	protected Institution getInstitutionDetails(SectionInfo info)
	{
		Institution i = new Institution();
		i.setUniqueId(getModel(info).getId());
		i.setName(name.getValue(info));
		i.setFilestoreId(filestore.getValue(info));
		i.setUrl(url.getValue(info));
		i.setAdminPassword(adminPassword.getValue(info));
		i.setTimeZone(timeZones.getSelectedValueAsString(info));
		try
		{
			i.setQuota(limit.getValue(info).doubleValue());
		}
		catch( NumberFormatException e )
		{
			i.setQuota(-1);
		}
		return i;
	}

	protected void setupFieldsFromInstitution(SectionInfo info, Institution i)
	{
		name.setValue(info, i.getName());
		filestore.setValue(info, i.getFilestoreId());
		limit.setValue(info, i.getQuota());
		url.setValue(info, i.getUrl());
		timeZones.setSelectedStringValue(info, i.getTimeZone());

		getModel(info).setId(i.getUniqueId());
		getModel(info).setNavigateAway(false);
	}

	protected void prepareSelectedDatabase(SectionInfo info)
	{
		long id = getSelectedDatabase(info);
		if( id > 0 )
		{
			getModel(info).setSelectedDatabase(databaseTabUtils.getNameRenderer(migrationService.getSchema(id)));
		}
		else
		{
			// If there is only one schema online, select that and disable
			// choice

			Iterator<DatabaseSchema> iter = selectDatabaseDialog.getOnlineSchemas().iterator();
			// We should never be able to get to here unless we have at least
			// one online schema.
			DatabaseSchema ds = iter.next();
			if( !iter.hasNext() )
			{
				getModel(info).setSelectedDatabase(databaseTabUtils.getNameRenderer(ds));

				selectedDatabase.setValue(info, ds.getId());
				selectDatabase.setDisplayed(info, false);
			}
		}
	}

	protected long getSelectedDatabase(SectionInfo info)
	{
		String value = selectedDatabase.getValue(info);
		if( !Check.isEmpty(value) )
		{
			try
			{
				return Long.parseLong(value);
			}
			catch( NumberFormatException ex )
			{
				// Nothing to worry about
			}
		}
		return -1;
	}

	protected boolean validate(SectionInfo info, Institution institution)
	{
		Map<String, String> errors = instImportService.validate(institution);
		extraValidate(info, institution, errors);
		if( !errors.isEmpty() )
		{
			getModel(info).setErrors(errors);
			info.preventGET();
			return false;
		}
		return true;
	}

	protected void extraValidate(SectionInfo info, Institution institution, Map<String, String> errors)
	{
		// nothing by default
	}

	protected void ensureForCloneOrImport(Institution i)
	{
		i.setUniqueId(0);

		if( Check.isEmpty(i.getFilestoreId()) )
		{
			i.setFilestoreId(UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]+", "") //$NON-NLS-1$ //$NON-NLS-2$
				.substring(0, 20));
		}
	}

	public Checkbox getItemsCheck()
	{
		return itemsCheck;
	}

	public Checkbox getAttachmentsCheck()
	{
		return attachmentsCheck;
	}

	public Checkbox getAuditlogsCheck()
	{
		return auditlogsCheck;
	}

	protected Set<String> getFlags(SectionInfo info)
	{
		Set<String> flags = new HashSet<String>();

		if( itemsCheck.isChecked(info) )
		{
			if( !attachmentsCheck.isChecked(info) )
			{
				flags.add(ConverterParams.NO_ITEMSATTACHMENTS);
			}
		}
		else
		{
			flags.add(ConverterParams.NO_ITEMS);
			flags.add(ConverterParams.NO_ITEMSATTACHMENTS);
		}

		if( !auditlogsCheck.isChecked(info) )
		{
			flags.add(ConverterParams.NO_AUDITLOGS);
		}

		return flags;
	}
}
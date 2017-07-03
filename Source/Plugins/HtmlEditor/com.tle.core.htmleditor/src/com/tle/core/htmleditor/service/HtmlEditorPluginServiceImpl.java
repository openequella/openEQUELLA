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

package com.tle.core.htmleditor.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.common.io.UnicodeReader;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.thoughtworks.xstream.XStream;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.i18n.beans.LanguageStringBean;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.filesystem.PublicFile;
import com.tle.core.guice.Bind;
import com.tle.core.htmleditor.HtmlEditorPluginConstants;
import com.tle.core.htmleditor.dao.HtmlEditorPluginDao;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.services.ValidationHelper;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author aholland
 */
@Singleton
@SuppressWarnings("nls")
@Bind(HtmlEditorPluginService.class)
@SecureEntity(HtmlEditorPluginService.ENTITY_TYPE)
public class HtmlEditorPluginServiceImpl
	extends
		AbstractEntityServiceImpl<HtmlEditorPluginEditingBean, HtmlEditorPlugin, HtmlEditorPluginService>
	implements
		HtmlEditorPluginService
{

	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(HtmlEditorPluginServiceImpl.class);
	private static final String KEY_ERROR_VALIDATION_PLUGIN_ID_UNIQUE = "error.validation.pluginidunique";
	private static final String KEY_ERROR_INVALID_PLUGIN_NOPROPS = "error.validation.invalidplugin.noproperties";
	private static final String KEY_ERROR_INVALID_PLUGIN_NOID = "error.validation.invalidplugin.noid";
	private static final String KEY_ERROR_INVALID_PLUGIN_NOBUTTONID = "error.validation.invalidplugin.nobuttonid";

	private static final String[] NON_BLANKS = {"name", "pluginId"};
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	static
	{
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	@Inject
	private InstitutionImportService institutionImportService;

	private final HtmlEditorPluginDao pluginDao;

	@Inject
	public HtmlEditorPluginServiceImpl(HtmlEditorPluginDao dao)
	{
		super(Node.HTMLEDITOR_PLUGIN, dao);
		pluginDao = dao;
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected HtmlEditorPluginEditingBean createEditingBean()
	{
		return new HtmlEditorPluginEditingBean();
	}

	@Override
	protected void doValidation(EntityEditingSession<HtmlEditorPluginEditingBean, HtmlEditorPlugin> session,
		HtmlEditorPlugin htmleditor, List<ValidationError> errors)
	{
		// no entity validation
	}

	@Override
	protected void doValidationBean(HtmlEditorPluginEditingBean bean, List<ValidationError> errors)
	{
		super.doValidationBean(bean, errors);

		ValidationHelper.checkBlankFields(bean, NON_BLANKS, errors);

		// check uniqueness of pluginId
		final String pluginId = bean.getPluginId();
		if( !Check.isEmpty(pluginId) )
		{
			final HtmlEditorPlugin old = getByPluginId(pluginId);
			if( old != null && old.getId() != bean.getId() )
			{
				errors.add(new ValidationError("plugin",
					resources.getString(KEY_ERROR_VALIDATION_PLUGIN_ID_UNIQUE, pluginId)));
			}
		}
	}

	@Override
	public final EntityPack<HtmlEditorPlugin> startEditInternal(HtmlEditorPlugin entity)
	{
		ensureNonSystem(entity);
		EntityPack<HtmlEditorPlugin> pack = new EntityPack<HtmlEditorPlugin>();
		pack.setEntity(entity);

		// Prepare staging
		EntityFile from = new EntityFile(entity);
		StagingFile staging = stagingService.createStagingArea();
		if( fileSystemService.fileExists(from) )
		{
			fileSystemService.copy(from, staging);
		}
		pack.setStagingID(staging.getUuid());

		fillTargetLists(pack);
		return pack;
	}

	@Override
	protected void populateEditingBean(HtmlEditorPluginEditingBean bean, HtmlEditorPlugin entity)
	{
		super.populateEditingBean(bean, entity);
		bean.setPluginId(entity.getPluginId());
		bean.setAuthor(entity.getAuthor());
		bean.setType(entity.getType());
		bean.setButtons(entity.getButtons());
		bean.setConfig(entity.getConfig());
		bean.setExtra(entity.getExtra());
		bean.setServerJs(entity.getServerJs());
		bean.setClientJs(entity.getClientJs());
	}

	@Override
	protected void populateEntity(HtmlEditorPluginEditingBean bean, HtmlEditorPlugin entity)
	{
		super.populateEntity(bean, entity);
		entity.setPluginId(bean.getPluginId());
		entity.setAuthor(bean.getAuthor());
		entity.setType(bean.getType());
		entity.setButtons(bean.getButtons());
		entity.setConfig(bean.getConfig());
		entity.setExtra(bean.getExtra());
		entity.setServerJs(bean.getServerJs());
		entity.setClientJs(bean.getClientJs());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<HtmlEditorPluginEditingBean, HtmlEditorPlugin>> SESSION createSession(
		String sessionId, EntityPack<HtmlEditorPlugin> pack, HtmlEditorPluginEditingBean bean)
	{
		return (SESSION) new HtmlEditorPluginEditingSessionImpl(sessionId, pack, bean);
	}

	@Override
	protected void beforeClone(TemporaryFileHandle staging, EntityPack<HtmlEditorPlugin> pack)
	{
		// export the prefs into the staging area
		prepareExport(staging, pack.getEntity(),
			new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));
	}

	@Override
	public boolean canEdit(BaseEntityLabel plugin)
	{
		return canEdit((Object) plugin);
	}

	@Override
	public boolean canEdit(HtmlEditorPlugin plugin)
	{
		return canEdit((Object) plugin);
	}

	private boolean canEdit(Object plugin)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(HtmlEditorPluginConstants.PRIV_EDIT_HTMLEDITOR_PLUGIN);
		return !aclManager.filterNonGrantedPrivileges(plugin, privs).isEmpty();
	}

	@Override
	public boolean canDelete(BaseEntityLabel plugin)
	{
		return canDelete((Object) plugin);
	}

	@Override
	public boolean canDelete(HtmlEditorPlugin plugin)
	{
		return canDelete((Object) plugin);
	}

	private boolean canDelete(Object plugin)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(HtmlEditorPluginConstants.PRIV_DELETE_HTMLEDITOR_PLUGIN);
		return !aclManager.filterNonGrantedPrivileges(plugin, privs).isEmpty();
	}

	@Override
	public XStream getXStream()
	{
		final XStream xstream = super.getXStream();
		xstream.alias("com.tle.core.htmleditor.beans.HtmlEditorPlugin", HtmlEditorPlugin.class);
		return xstream;
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		final HtmlEditorPluginReferencesEvent event = new HtmlEditorPluginReferencesEvent(get(id));
		publishEvent(event);
		return event.getReferencingClasses();
	}

	@Transactional
	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		super.userDeletedEvent(event);
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		pluginDao.changeUserId(event.getFromUserId(), event.getToUserId());

		super.userIdChangedEvent(event);
	}

	@Transactional
	@Override
	public HtmlEditorPlugin getByPluginId(String pluginId)
	{
		return pluginDao.getByPluginId(pluginId);
	}

	@Transactional
	@Override
	public List<HtmlEditorPlugin> enumerateForType(String type)
	{
		List<HtmlEditorPlugin> plugins = pluginDao.enumerateEnabled();
		// return them all, there is currently only one type, ie "tinymce"
		return plugins;
	}

	@Override
	public void uploadPlugin(InputStream uploadStream) throws InvalidHtmlEditorPluginException
	{
		HtmlEditorPluginEditingSession session = startNewSession(new HtmlEditorPlugin());

		StagingFile stagingFile = new StagingFile(session.getStagingId());
		try
		{
			StagingFile zipArea = stagingService.createStagingArea();
			fileSystemService.write(zipArea, "plugin.zip", uploadStream, false);
			try( InputStream zipIn = fileSystemService.read(zipArea, "plugin.zip") )
			{
				fileSystemService.unzipFile(stagingFile, zipIn, ArchiveType.ZIP);
			}
			finally
			{
				stagingService.removeStagingArea(zipArea, true);
			}

			final ObjectNode plugin = loadJson(stagingFile, "plugin.json");
			if( plugin == null )
			{
				throw new InvalidHtmlEditorPluginException(
					resources.getString(KEY_ERROR_INVALID_PLUGIN_NOPROPS, "plugin.json"));
			}

			// populate the bean with plugin values
			final HtmlEditorPluginEditingBean bean = session.getBean();

			final String pluginId = getNode(plugin, "id", null);
			if( Check.isEmpty(pluginId) )
			{
				throw new InvalidHtmlEditorPluginException(resources.getString(KEY_ERROR_INVALID_PLUGIN_NOID));
			}
			bean.setPluginId(pluginId);

			final String name = getNode(plugin, "name", pluginId);
			final LanguageBundleBean lbb = new LanguageBundleBean();
			final String locale = CurrentLocale.getLocale().toString();
			lbb.setStrings(Collections.singletonMap(locale, new LanguageStringBean(locale, name)));
			bean.setName(lbb);

			final String author = getNode(plugin, "author", resources.getString("plugin.author.unknown"));
			bean.setAuthor(author);

			final ArrayNode buttonArray = (ArrayNode) plugin.get("buttons");
			if( buttonArray != null )
			{
				for( Iterator<JsonNode> buttons = buttonArray.elements(); buttons.hasNext(); )
				{
					final ObjectNode button = (ObjectNode) buttons.next();
					final String buttonId = getNode(button, "id", null);
					if( Check.isEmpty(buttonId) )
					{
						throw new InvalidHtmlEditorPluginException(
							resources.getString(KEY_ERROR_INVALID_PLUGIN_NOBUTTONID));
					}
				}
				bean.setButtons(buttonArray.toString());
			}

			final ObjectNode config = loadJson(stagingFile, "config.json");
			if( config != null )
			{
				bean.setConfig(config.toString());
			}

			// type is currently only allowed to be "tinymce"
			final String type = getNode(plugin, "type", "tinymce");
			if( !type.equals("tinymce") )
			{
				throw new InvalidHtmlEditorPluginException("type can only be tinymce");
			}
			bean.setType(type);

			// FIXME: get the type to contribute any validation and extra config
			// FIXME: SUPER-haxical:
			if( !fileSystemService.fileExists(stagingFile, "plugin/editor_plugin_src.js")
				&& fileSystemService.fileExists(stagingFile, "plugin/editor_plugin.js") )
			{
				final ObjectNode extra = JSON_MAPPER.createObjectNode();
				extra.put("minified", true);
				bean.setExtra(extra.toString());
			}

			bean.setServerJs(loadText(stagingFile, "server.js"));
			bean.setClientJs(loadText(stagingFile, "client.js"));

			commitSession(session);
		}
		catch( InvalidHtmlEditorPluginException inv )
		{
			fileSystemService.removeFile(stagingFile);
			throw inv;
		}
		catch( Exception e )
		{
			fileSystemService.removeFile(stagingFile);
			throw new InvalidHtmlEditorPluginException(e.getMessage());
		}
	}

	@Override
	protected void beforeSaveFiles(StagingFile staging, EntityFile files, boolean unlock, boolean lockAfterwards,
		HtmlEditorPlugin newEntity)
	{
		// move *only* plugin resources to public area
		if( fileSystemService.fileExists(staging, "resources") )
		{
			final PublicFile pf = new PublicFile(newEntity.getUuid());
			fileSystemService.move(staging, "resources", pf, "");
		}
		super.afterSaveFiles(staging, files, unlock, lockAfterwards, newEntity);
	}

	@Override
	protected void afterDelete(HtmlEditorPlugin entity)// NOSONAR see comment
	{
		super.afterDelete(entity);

		// TODO: failure to delete these could build up?
		// Problem with deleting these is that any content relying on them will
		// not display correctly...
		// final PublicFile pf = new PublicFile(entity.getUuid());
		// fileSystemService.removeFile(pf);
	}

	private String getNode(ObjectNode parent, String node, String defaultValue)
	{
		JsonNode n = parent.get(node);
		if( n != null )
		{
			return n.asText();
		}
		return defaultValue;
	}

	/**
	 * @param stagingFile
	 * @param file
	 * @return null if the file is not found
	 * @throws InvalidHtmlEditorPluginException
	 */
	@Nullable
	private ObjectNode loadJson(StagingFile stagingFile, String file) throws InvalidHtmlEditorPluginException
	{
		if( fileSystemService.fileExists(stagingFile, file) )
		{
			try( final UnicodeReader reader = new UnicodeReader(fileSystemService.read(stagingFile, file), "UTF-8") )
			{
				return (ObjectNode) JSON_MAPPER.readTree(reader);
			}
			catch( IOException io )
			{
				fileSystemService.removeFile(stagingFile);
				throw new InvalidHtmlEditorPluginException(resources.getString(KEY_ERROR_INVALID_PLUGIN_NOPROPS, file),
					io);
			}
		}
		return null;
	}

	@Nullable
	private String loadText(StagingFile stagingFile, String file) throws InvalidHtmlEditorPluginException
	{
		if( fileSystemService.fileExists(stagingFile, file) )
		{
			try( final UnicodeReader reader = new UnicodeReader(fileSystemService.read(stagingFile, file), "UTF-8") )
			{
				final StringWriter sw = new StringWriter();
				CharStreams.copy(reader, sw);
				return sw.toString();
			}
			catch( IOException io )
			{
				throw Throwables.propagate(io);
			}
		}
		return null;
	}
}

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

package com.tle.core.connectors.blackboard.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMMetaFactoryLocator;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

import com.blackboard.ContextWSStub;
import com.blackboard.ContextWSStub.EmulateUser;
import com.blackboard.ContextWSStub.EmulateUserResponse;
import com.blackboard.ContextWSStub.LoginTool;
import com.blackboard.ContextWSStub.RegisterTool;
import com.blackboard.ContextWSStub.RegisterToolResponse;
import com.blackboard.ContextWSStub.RegisterToolResultVO;
import com.dytech.devlib.PropBagEx;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.URLUtils;
import com.tle.common.Utils;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.connectors.blackboard.service.BlackboardConnectorService;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.AddItemResult;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.AddItemToCourse;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.Base;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.Content;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.Course;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.DeleteContent;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.EditContent;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.FindAllUsages;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.FindUsages;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.Folder;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.GetCourseCode;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.ListCoursesForUser;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.ListCoursesForUserResponse;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.ListFoldersForCourse;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.ListFoldersForFolder;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.MoveContent;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.SearchResult;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.SynchroniseEquellaContentTables;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.TestConnection;
import com.tle.core.connectors.blackboard.webservice.EQUELLAWSStub.TestConnectionResponse;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.AbstractIntegrationConnectorRespository;
import com.tle.core.connectors.service.ConnectorRepositoryImplementation;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.guice.Bind;
import com.tle.core.guice.Bindings;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.impl.ProxyDetails;
import com.tle.core.services.item.ItemResolver;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.util.ItemHelper;
import com.tle.web.integration.Integration.LmsLink;
import com.tle.web.integration.Integration.LmsLinkInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@NonNullByDefault
@SuppressWarnings({"nls", "deprecation"})
@Bindings({@Bind(BlackboardConnectorService.class), @Bind(ConnectorRepositoryImplementation.class)})
@Singleton
public class BlackboardConnectorServiceImpl extends AbstractIntegrationConnectorRespository
	implements
		BlackboardConnectorService

{
	private static final Logger LOGGER = Logger.getLogger(BlackboardConnectorService.class);

	private static final String KEY_PROXY_TOOL_PASS = "proxyToolPass";

	private static final String VENDOR_ID = "Apereo";
	private static final String PROGRAM_ID = "EQUELLA";
	private static final String[] REQUIRED_METHODS = new String[]{"Context.WS:emulateUser", "Context.WS:logout",
			"Context.WS:getMemberships", "Context.WS.getServerVersion", "Context.WS.initialize",
			"Context.WS.loginTool", "Context.WS.registerTool", "Course.WS:getCourse"};

	private static final String BB_ERROR_CODE_USER_NOT_EXIST = "[Context.WS004]";

	private static final Cache<Institution, ConfigurationContext> CONFIG_CACHE = CacheBuilder.newBuilder().build();
	/*
	 * "WSFW000"; // 'Other' exception thrown from a service method "WSFW001";
	 * // Invalid session sent from client "WSFW002"; // Invalid source IP for
	 * client program "WSFW003"; // Invalid local access to a Web Service method
	 * "WSFW004"; // Access Exception (invalid operation for current session was
	 * attempted) "WSFW005"; // Attempt to use a tool-only method as a 'user'
	 * "WSFW006"; // Request must be over https "WSFW007"; // Originator address
	 * denied "WSFW008"; // Operation is invalid "WSFW009"; // Service is not
	 * active "WSFW010"; // Unknown client programs are not allowed "WSFW011";
	 * // Client program is not available Context.WS001 Invalid user/password
	 * combination Context.WS002 Invalid password for a client program
	 * Context.WS003 Invalid client program (or vendor) specified (or Unknown
	 * vendor/program combination if unknown agents are not allowed)
	 * Context.WS004 Invalid user specified to emulate Context.WS005 Unsupported
	 * authentication method configured on the server (default:rdbms is the only
	 * supported method) Context.WS006 Proxy Tool is not currently available.
	 */
	// @formatter:off
	private static final String[][] BB_ERROR_CODES = new String[][]{
			new String[]{"[WSFW001]", "error.invalidsession"},
			new String[]{"[WSFW004]", "error.accessdenied"},
			new String[]{"[Context.WS002]", "error.invalidpassword"},
			new String[]{BB_ERROR_CODE_USER_NOT_EXIST, "error.usernotexist"},
			new String[]{"[Context.WS006]", "error.notenabled"},};
	// @formatter:on

	private static final long LOGIN_DURATION_SECONDS = 120;

	private final Object secretLock = new Object();

	@Inject
	private ConfigurationService configService;
	@Inject
	private UrlService urlService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private ItemResolver itemResolver;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	public BlackboardConnectorServiceImpl()
	{
		// Ewwww
		BlindSSLSocketFactory.register();
		// Turn off spurious Pre-emptive Authentication bollocks
		Logger.getLogger("org.apache.commons.httpclient.HttpMethodDirector").setLevel(Level.ERROR);
	}

	@Override
	public boolean isRequiresAuthentication(Connector connector)
	{
		return false;
	}

	@Nullable
	@Override
	public String getAuthorisationUrl(Connector connector, String forwardUrl, @Nullable String authData)
	{
		return null;
	}

	@Override
	public String getCourseCode(Connector connector, String username, final String courseId)
		throws LmsUserNotFoundException
	{
		return doBb(connector.getServerUrl(), username, false, new BbFunction<String>()
		{
			@Override
			public String execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException
			{
				final GetCourseCode getCourseCode = new GetCourseCode();
				getCourseCode.setCourseId(courseId);
				return equellaWs.getCourseCode(getCourseCode).get_return();
			}
		});
	}

	@Override
	public ConnectorFolder addItemToCourse(Connector connector, final String username, final String courseId,
		final String folderId, final IItem<?> item, final SelectedResource selectedResource)
		throws LmsUserNotFoundException
	{
		return doBb(connector.getServerUrl(), username, true, new BbFunction<ConnectorFolder>()
		{
			@Override
			public ConnectorFolder execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException
			{
				final LmsLinkInfo lmsLinkInfo = getLmsLink(item, selectedResource);
				final LmsLink lmsLink = lmsLinkInfo.getLmsLink();
				final IItem<?> resourcesItem = lmsLinkInfo.getResourceItem();
				final IAttachment attachment = lmsLinkInfo.getResourceAttachment();
				final String attachmentUuid = (attachment == null ? null : attachment.getUuid());
				final int version = selectedResource.isLatest() ? 0 : resourcesItem.getVersion();

				// Note: we register it as the *resource's* item UUID and
				// Version, because the
				// XML and URL are based off the resource's item

				// When removing/editing the item in BB the UUID (obtained from
				// the XML) it must match what is registered!

				// http://dev.equella.com/issues/7670
				LanguageBundle nlb = resourcesItem.getDescription();
				String description = LangUtils.isEmpty(nlb) ? "" : CurrentLocale.get(nlb);
				nlb = resourcesItem.getName();
				String name = LangUtils.isEmpty(nlb) ? resourcesItem.getUuid() : CurrentLocale.get(nlb);

				final AddItemToCourse addItemToCourse = new AddItemToCourse();
				addItemToCourse.setTitle(name);
				addItemToCourse.setDescription(description);
				addItemToCourse.setCourseId(courseId);
				addItemToCourse.setFolderId(folderId);
				addItemToCourse.setItemUuid(resourcesItem.getUuid());
				addItemToCourse.setItemVersion(version);
				addItemToCourse.setUrl(lmsLink.getUrl());
				addItemToCourse.setUsername(username); // unused
				addItemToCourse.setServerUrl(urlService.getInstitutionUrl().toString());
				addItemToCourse.setAttachment(attachmentUuid);

				// FIXME: Yikes, probably only works for real Items
				final PropBagEx xml = itemHelper.convertToXml(new ItemPack(resourcesItem, itemResolver.getXml(
					resourcesItem, selectedResource.getKey().getExtensionType()), null));

				// Note: the version in the XML must match the version recorded
				// in the usage DB!!
				final PropBagEx itemxml = xml.aquireSubtree("item");
				itemxml.setNode("@version", version);

				// If the selected object is the item
				if( selectedResource.getType() == SelectedResource.TYPE_PATH && selectedResource.getUrl().length() == 0 )
				{
					selectedResource.setUrl("./"); //$NON-NLS-1$
				}
				else if( selectedResource.getType() == SelectedResource.TYPE_ATTACHMENT )
				{
					itemxml.setNode(
						"attachments/@selectedType", lmsLinkInfo.getResourceAttachment().getAttachmentType().name() //$NON-NLS-1$
							.toLowerCase());
				}

				itemxml.setNode("attachments/@selectedTitle", lmsLink.getName());
				if( !Check.isEmpty(lmsLink.getDescription()) )
				{
					itemxml.setNode("attachments/@selectedDescription", lmsLink.getDescription());
				}

				final ViewableItem<?> viewableItem = viewableItemFactory.createNewViewableItem(item.getItemId());
				final String mimeType;
				if( attachment != null )
				{
					itemxml.setNode("attachments/@selected", lmsLink.getUrl());
					final ViewableResource viewableResource = attachmentResourceService.getViewableResource(
						createViewItemInfo(), viewableItem, attachment);
					mimeType = viewableResource.getMimeType();
				}
				else
				{
					mimeType = "equella/item";
				}
				itemxml.setNode("attachments/@selectedMimeType", mimeType);

				addItemToCourse.setXml(itemxml.toString());

				final AddItemResult result = equellaWs.addItemToCourse(addItemToCourse).get_return();
				final Folder folder = result.getFolder();
				if( folder == null )
				{
					throw new RuntimeException("Error adding item to course");
				}
				return convertFolder(folder, convertCourse(result.getCourse()));
			}
		});
	}

	@Override
	protected ViewableItemType getViewableItemType()
	{
		// No special treatment for BB anymore, use the generic integration.
		return ViewableItemType.GENERIC;
	}

	@Override
	protected String getIntegrationId()
	{
		// No special treatment for BB anymore, use the generic integration.
		return "gen";
	}

	@Override
	protected boolean isRelativeUrls()
	{
		return false;
	}

	@Override
	public List<ConnectorCourse> getCourses(Connector connector, final String username, final boolean editable,
		final boolean archived, final boolean management) throws LmsUserNotFoundException
	{
		return doBb(connector.getServerUrl(), username, editable, new BbFunction<List<ConnectorCourse>>()
		{
			@Override
			public List<ConnectorCourse> execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs)
				throws RemoteException
			{
				final List<ConnectorCourse> connCourses = new ArrayList<ConnectorCourse>();
				final Course[] courses;

				final ListCoursesForUser lcfu = new ListCoursesForUser();
				lcfu.setUsername(username);
				lcfu.setArchived(archived);
				lcfu.setModifiableOnly(editable);
				final ListCoursesForUserResponse lcfuResponse = equellaWs.listCoursesForUser(lcfu);
				courses = lcfuResponse.get_return();

				// empty arrays come through as null
				if( courses != null )
				{
					for( Course course : courses )
					{
						connCourses.add(convertCourse(course));
					}
				}
				return connCourses;
			}
		});
	}

	@Override
	public List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, final String courseId,
		boolean management) throws LmsUserNotFoundException
	{
		return doBb(connector.getServerUrl(), username, true, new BbFunction<List<ConnectorFolder>>()
		{
			@Override
			public List<ConnectorFolder> execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs)
				throws RemoteException
			{
				final ListFoldersForCourse lffc = new ListFoldersForCourse();
				lffc.setCourseId(courseId);
				final Folder[] foldersArray = equellaWs.listFoldersForCourse(lffc).get_return();
				if( foldersArray == null )
				{
					return new ArrayList<ConnectorFolder>();
				}

				final ConnectorCourse cc = new ConnectorCourse(courseId);
				final Collection<ConnectorFolder> folders = Collections2.transform(Arrays.asList(foldersArray),
					new Function<Folder, ConnectorFolder>()
					{
						@Override
						public ConnectorFolder apply(Folder f)
						{
							return convertFolder(f, cc);
						}
					});

				return new ArrayList<ConnectorFolder>(folders);
			}
		});
	}

	@Override
	public List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, final String courseId,
		final String folderId, boolean management) throws LmsUserNotFoundException
	{
		return doBb(connector.getServerUrl(), username, true, new BbFunction<List<ConnectorFolder>>()
		{
			@Override
			public List<ConnectorFolder> execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs)
				throws RemoteException
			{
				final ListFoldersForFolder lfff = new ListFoldersForFolder();
				lfff.setFolderId(folderId);
				final Folder[] foldersArray = equellaWs.listFoldersForFolder(lfff).get_return();
				if( foldersArray == null )
				{
					return new ArrayList<ConnectorFolder>();
				}

				final ConnectorCourse cc = new ConnectorCourse(courseId);
				final Collection<ConnectorFolder> folders = Collections2.transform(Arrays.asList(foldersArray),
					new Function<Folder, ConnectorFolder>()
					{
						@Override
						public ConnectorFolder apply(Folder f)
						{
							return convertFolder(f, cc);
						}
					});
				return new ArrayList<ConnectorFolder>(folders);
			}
		});
	}

	@Override
	public String testConnection(String serverUrl, final String username)
	{
		if( Check.isEmpty(serverUrl) )
		{
			return getString("test.error.nourl");
		}

		try
		{
			return doBb(serverUrl, username, true, new BbFunction<String>()
			{
				@Override
				public String execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException
				{
					final TestConnection t = new TestConnection();
					t.setParam(username);

					final TestConnectionResponse res = equellaWs.testConnection(t);
					final String r = res.get_return();
					if( !r.equals(username) )
					{
						return getString("test.error.invalidresponse");
					}
					return null;
				}
			});
		}
		catch( Exception t )
		{
			return t.getLocalizedMessage();
		}
	}

	@Override
	public String registerProxyTool(String serverUrl, String password)
	{
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

			final Stubs stubs = getStubs(serverUrl);

			boolean newSecret = false;
			String secret = getSecret(serverUrl);
			if( secret == null )
			{
				newSecret = true;
				secret = UUID.randomUUID().toString();
			}

			final RegisterTool registerTool = new RegisterTool();
			registerTool.setClientProgramId(PROGRAM_ID);
			registerTool.setClientVendorId(VENDOR_ID);
			registerTool.setDescription(getString("proxytool.description"));
			registerTool.setInitialSharedSecret(secret);
			registerTool.setRegistrationPassword(password);
			registerTool.setRequiredToolMethods(REQUIRED_METHODS);

			// register the proxy tool
			final RegisterToolResponse registerToolResponse = stubs.getContextWebservice(null).registerTool(
				registerTool);

			final RegisterToolResultVO registerToolResultVO = registerToolResponse.get_return();
			if( !Check.isEmpty(registerToolResultVO.getFailureErrors()) )
			{
				String error = registerToolResultVO.getFailureErrors()[0];

				// Dodgy... seems empty arrays on BB side return a single
				// element array of null on client side...
				if( error != null )
				{
					if( error.equals("Cannot reregister an activated client") )
					{
						return REGISTER_PROXY_TOOL_RESULT_ALREADY_REGISTERED;
					}
					return error;
				}
			}

			if( newSecret )
			{
				setSecret(serverUrl, secret);
			}

			return null;
		}
		catch( Exception t )
		{
			LOGGER.error("Failed to register proxy tool", t);
			return t.getLocalizedMessage();
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	@Override
	public List<ConnectorContent> findUsages(final Connector connector, String username, final String uuid,
		final int version, final boolean versionIsLatest, final boolean showArchived, final boolean allVersions)
		throws LmsUserNotFoundException
	{
		LOGGER.trace("findUsages enter");
		return doBb(connector.getServerUrl(), username, false, new BbFunction<List<ConnectorContent>>()
		{
			@Override
			public List<ConnectorContent> execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs)
				throws RemoteException
			{
				LOGGER.trace("doBB.execute enter");
				final FindUsages findUsages = new FindUsages();
				findUsages.setServerUrl(CurrentInstitution.get().getUrlAsUrl().toString());
				findUsages.setItemUuid(uuid);
				findUsages.setItemVersion(version);
				findUsages.setAvailable(!showArchived);
				findUsages.setAllVersions(allVersions);
				findUsages.setVersionIsLatest(versionIsLatest);

				LOGGER.trace("invoke findUsages");
				final SearchResult result = equellaWs.findUsages(findUsages).get_return();
				final Content[] content = result.getResults();
				if( content != null )
				{
					final Map<String, Course> courseMap = mapperise(result.getCourses());
					final Map<String, Folder> folderMap = mapperise(result.getFolders());

					LOGGER.trace("transforming results");
					final List<ConnectorContent> cc = Lists.transform(Arrays.asList(content),
						new Function<Content, ConnectorContent>()
						{
							@Override
							public ConnectorContent apply(Content input)
							{
								return convertContent(input, courseMap.get(input.getCourseId()),
									folderMap.get(input.getFolderId()), connector.getServerUrl(), !showArchived);
							}
						});
					LOGGER.trace("findUsages exit");
					return cc;
				}
				return Lists.newArrayList();
			}
		});
	}

	@Override
	public SearchResults<ConnectorContent> findAllUsages(final Connector connector, String username,
		final String query, final String courseId, final String folderId, final boolean archived, final int offset,
		final int count, final ExternalContentSortType sortType, final boolean reverseSort)
		throws LmsUserNotFoundException
	{
		final String sortCol;
		final boolean reverse;
		switch( sortType )
		{
			case NAME:
				sortCol = "name";
				reverse = reverseSort;
				break;
			case COURSE:
				sortCol = "course";
				reverse = reverseSort;
				break;
			case DATE_ADDED:
			default:
				sortCol = "dateAdded";
				reverse = !reverseSort;
		}

		LOGGER.trace("findAllUsages enter");
		return doBb(connector.getServerUrl(), username, false, new BbFunction<SearchResults<ConnectorContent>>()
		{
			@Override
			public SearchResults<ConnectorContent> execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs)
				throws RemoteException
			{
				LOGGER.trace("doBB.execute enter");
				final FindAllUsages findAllUsages = new FindAllUsages();
				findAllUsages.setServerUrl(CurrentInstitution.get().getUrlAsUrl().toString());
				findAllUsages.setQuery(query.replace("*", "%"));
				findAllUsages.setCourseId(courseId);
				findAllUsages.setFolderId(folderId);
				findAllUsages.setAvailable(!archived);
				findAllUsages.setOffset(offset);
				findAllUsages.setCount(count);
				findAllUsages.setSortColumn(sortCol);
				findAllUsages.setSortReverse(reverse);

				LOGGER.trace("invoke findAllUsages");
				final SearchResult result = equellaWs.findAllUsages(findAllUsages).get_return();
				final Content[] content = result.getResults();
				final int available = result.getAvailable();
				if( available > 0 && !(content.length == 1 && content[0] == null) )
				{
					final Map<String, Course> courseMap = mapperise(result.getCourses());
					final Map<String, Folder> folderMap = mapperise(result.getFolders());

					LOGGER.trace("transforming results");
					final List<ConnectorContent> cc = Lists.transform(Arrays.asList(content),
						new Function<Content, ConnectorContent>()
						{
							@Override
							public ConnectorContent apply(Content input)
							{
								return convertContent(input, courseMap.get(input.getCourseId()),
									folderMap.get(input.getFolderId()), connector.getServerUrl(), !archived);
							}
						});

					LOGGER.trace("findAllUsages exit");

					return new SimpleSearchResults<ConnectorContent>(cc, cc.size(), offset, available);
				}
				return new SimpleSearchResults<ConnectorContent>(new ArrayList<ConnectorContent>(), 0, offset,
					available);
			}
		});
	}

	@Override
	public int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived)
		throws LmsUserNotFoundException
	{
		// FIXME do proper count
		return findAllUsages(connector, username, query, "", "", true, 0, 1, ExternalContentSortType.NAME, false)
			.getAvailable();
	}

	private ConnectorContent convertContent(Content content, Course course, Folder folder, String bbServerUrl,
		boolean available)
	{
		String contentId = content.getId();
		if( contentId == null )
		{
			contentId = "u" + content.getUsageId();
		}
		final ConnectorContent c = new ConnectorContent(contentId);
		c.setCourseId(course.getId());
		c.setCourse(course.getName());
		c.setCourseCode(course.getCode());
		c.setCourseUrl(URLUtils.newURL(bbServerUrl, course.getUrl()).toString());

		c.setFolderId(folder.getId());
		c.setFolder(folder.getName());
		// URL provided by webservice is relative
		c.setFolderUrl(URLUtils.newURL(bbServerUrl, content.getFolderUrl()).toString());

		c.setDateAdded(new Date(content.getCreatedDate()));
		c.setDateModified(new Date(content.getModifiedDate()));
		c.setVersion(content.getVersion());
		c.setUuid(content.getUuid());
		c.setExternalTitle(content.getTitle());
		/*
		 * c.setExternalUrl(URLUtils.newURL( bbServerUrl,
		 * "webapps/dych-tle-BBLEARN/ViewContent?type=default&content_id=" +
		 * contentId + "&course_id=" + course.getId() + "&page=" +
		 * content.getPage()).toString());
		 */
		c.setExternalDescription(content.getDescription());

		c.setAvailable(content.getAvailable());

		final String page = content.getPage();
		if( !Check.isEmpty(page) )
		{
			if( page.equalsIgnoreCase("viewims.jsp") )
			{
				c.setAttachmentUrl(page);
			}
			else
			{
				c.setAttachmentUuid(page);
			}
		}

		if( !available )
		{
			c.setAttribute("contentAvailable", getKey("finduses.label.contentavailable"),
				getString("finduses.value.contentavailable." + (content.getAvailable() ? "yes" : "no")));

			c.setAttribute("courseAvailable", getKey("finduses.label.courseavailable"),
				getString("finduses.value.courseavailable." + (course.getAvailable() ? "yes" : "no")));
		}

		String[] instructors = course.getInstructors();
		if( instructors != null && !(instructors.length == 1 && instructors[0] == null) )
		{
			c.setAttribute(ConnectorContent.KEY_INSTRUCTOR, getKey("finduses.label.instructors"),
				Utils.join(instructors, ", "));
		}
		int enrollments = course.getEnrollments();
		if( enrollments > 0 )
		{
			c.setAttribute(ConnectorContent.KEY_ENROLLMENTS, getKey("finduses.label.enrollments"), enrollments);
		}

		long dateAccessed = content.getDateAccessed();
		if( dateAccessed != 0 )
		{
			c.setAttribute(ConnectorContent.KEY_DATE_ACCESSED, getKey("finduses.label.dateAccessed"), new Date(
				dateAccessed));
		}
		return c;
	}

	@Override
	public void synchroniseEquellaContent(Connector connector, String username) throws LmsUserNotFoundException
	{
		doBb(connector.getServerUrl(), username, true, new BbFunction<Void>()
		{
			@Override
			public Void execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException
			{
				final SynchroniseEquellaContentTables sync = new SynchroniseEquellaContentTables();
				sync.setInstitutionUrl(CurrentInstitution.get().getUrlAsUrl().toString());
				sync.setAvailable(false);
				equellaWs.synchroniseEquellaContentTables(sync);
				return null;
			}
		});
	}

	@Override
	public String getSecret(String url)
	{
		synchronized( secretLock )
		{
			return configService.getProperty(getSecretKey(url));
		}
	}

	@Override
	public void setSecret(String url, String secret)
	{
		synchronized( secretLock )
		{
			configService.setProperty(getSecretKey(url), secret);
		}
	}

	private void parseBBError(Throwable t, String username) throws LmsUserNotFoundException
	{
		LOGGER.error("Error invoking BB web service", t);

		if( t instanceof AxisFault )
		{
			String message = t.getMessage();
			for( String[] conversion : BB_ERROR_CODES )
			{
				final String bbcode = conversion[0];
				if( message != null && message.startsWith(bbcode) )
				{
					String friendlyMessage = CurrentLocale.get(getKey(conversion[1]), username);
					if( bbcode.equals(BB_ERROR_CODE_USER_NOT_EXIST) )
					{
						throw new LmsUserNotFoundException(username, friendlyMessage);
					}
					throw new RuntimeException(friendlyMessage, t);
				}
			}

			if( message != null && message.startsWith("[") )
			{
				int end = message.indexOf(']');
				if( end > 0 && end < message.length() )
				{
					message = message.substring(end + 1);
				}
			}

			throw new RuntimeException(message);
		}

		throw Throwables.propagate(t);
	}

	private String getSecretKey(String url)
	{
		return KEY_PROXY_TOOL_PASS + ":" + url;
	}

	private String getKey(String partKey)
	{
		return "com.tle.core.connectors.blackboard." + partKey;
	}

	private String getString(String partKey)
	{
		return CurrentLocale.get("com.tle.core.connectors.blackboard." + partKey);
	}

	@Override
	public ConnectorTerminology getConnectorTerminology()
	{
		ConnectorTerminology terms = new ConnectorTerminology();
		terms.setShowArchived(getKey("finduses.showarchived"));
		terms.setShowArchivedLocations(getKey("finduses.showarchived.courses"));
		terms.setCourseHeading(getKey("finduses.course"));
		terms.setLocationHeading(getKey("finduses.location"));
		return terms;
	}

	private static class BlackboardException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public BlackboardException(String message)
		{
			super(message);
		}
	}

	private Stubs getStubs(String serverUrl) throws Exception
	{
		final ConfigurationContext ctx = getConfiguration();

		final HttpTransportProperties.ProxyProperties proxyProperties = new HttpTransportProperties.ProxyProperties();
		final ProxyDetails proxy = configService.getProxyDetails();
		if( proxy.isConfigured() && !proxy.isHostExcepted(new URL(serverUrl).getHost()) )
		{
			proxyProperties.setProxyName(proxy.getHost());
			proxyProperties.setProxyPort(proxy.getPort());
			ctx.setProperty(HTTPConstants.PROXY, proxyProperties);
		}
		ctx.setProperty(HTTPConstants.SO_TIMEOUT, 120000);
		ctx.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 120000);

		return new Stubs(ctx, serverUrl);
	}

	private synchronized ConfigurationContext getConfiguration() throws AxisFault
	{
		Institution inst = CurrentInstitution.get();
		ConfigurationContext config = CONFIG_CACHE.getIfPresent(inst);
		if( config == null )
		{
			config = ConfigurationContextFactory.createConfigurationContext(new CustomConfigurator());
			CONFIG_CACHE.put(inst, config);
		}
		return config;
	}

	/**
	 * Wraps the function f in a plugin classloader and calls emulate user
	 * before any code within f.execute is called
	 * 
	 * @param <T>
	 * @param connector
	 * @param username
	 * @param f
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	private <T> T doBb(String serverUrl, String username, boolean emulateUser, BbFunction<T> f)
		throws LmsUserNotFoundException
	{
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

			final Stubs s = getStubs(serverUrl);
			final String secret = getSecret(serverUrl);
			final ContextWSStub contextWSStub = s.getContextWebservice(secret, true);
			final EQUELLAWSStub equellaWSStub = s.getEquellaWebservice(secret);

			if( !emulateUser || emulateUser(contextWSStub, username) )
			{
				return f.execute(contextWSStub, equellaWSStub);
			}
			throw new BlackboardException(CurrentLocale.get("com.tle.core.connectors.blackboard.error.failtoemulate",
				username));
		}
		catch( Exception t )
		{
			parseBBError(t, username);
			// not possible
			return null;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	private boolean emulateUser(ContextWSStub contextWSStub, String username) throws RemoteException
	{
		final EmulateUser emulateUser = new EmulateUser();
		emulateUser.setUserToEmulate(username);
		final EmulateUserResponse response = contextWSStub.emulateUser(emulateUser);
		return response.get_return();
	}

	private ConnectorFolder convertFolder(Folder folder, ConnectorCourse course)
	{
		final ConnectorFolder cf = new ConnectorFolder(folder.getId(), course);
		cf.setName(folder.getName());
		cf.setCreatedDate(new Date(folder.getCreatedDate()));
		cf.setModifiedDate(new Date(folder.getModifiedDate()));
		return cf;
	}

	private ConnectorCourse convertCourse(Course course)
	{
		final ConnectorCourse cc = new ConnectorCourse(course.getId());
		cc.setName(course.getName());
		cc.setCreatedDate(new Date(course.getCreatedDate()));
		cc.setModifiedDate(new Date(course.getModifiedDate()));
		cc.setAvailable(course.getAvailable());
		cc.setCourseCode(course.getCode());
		return cc;
	}

	private class Stubs implements CallbackHandler
	{
		private final OutflowConfiguration ofc;
		private final ConfigurationContext ctx;
		private final String bbUrl;
		private final ContextWSStub contextWebservice;
		private EQUELLAWSStub equellaWebservice;
		private String sessionId;

		private final Object webserviceLock = new Object();
		private final HttpClient httpClient;

		public Stubs(ConfigurationContext ctx, String bbUrl) throws AxisFault
		{
			this.ctx = ctx;
			this.bbUrl = bbUrl;

			/*
			 * Must use deprecated class of setting up security because the SOAP
			 * response doesn't include a security header. Using the deprecated
			 * OutflowConfiguration class we can specify that the security
			 * header is only for the outgoing SOAP message.
			 */
			ofc = new OutflowConfiguration();
			ofc.setActionItems("UsernameToken Timestamp");
			ofc.setUser("session");
			ofc.setPasswordType("PasswordText");

			final MultiThreadedHttpConnectionManager conMan = new MultiThreadedHttpConnectionManager();
			final HttpConnectionManagerParams params = new HttpConnectionManagerParams();
			params.setMaxTotalConnections(1000);
			params.setDefaultMaxConnectionsPerHost(100);
			params.setSoTimeout(60000);
			params.setConnectionTimeout(30000);
			conMan.setParams(params);

			httpClient = new HttpClient(conMan);
			final HttpClientParams clientParams = httpClient.getParams();
			clientParams.setAuthenticationPreemptive(false);
			clientParams.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			ctx.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

			contextWebservice = new ContextWSStub(ctx, PathUtils.filePath(bbUrl, "webapps/ws/services/Context.WS"));
			initStub(contextWebservice);
		}

		public ContextWSStub getContextWebservice(String secret) throws Exception
		{
			return getContextWebservice(secret, false);
		}

		public ContextWSStub getContextWebservice(String secret, boolean forceLogin) throws Exception
		{
			if( secret != null )
			{
				ensureLogin(secret, forceLogin);
			}
			return contextWebservice;
		}

		public EQUELLAWSStub getEquellaWebservice(String secret) throws Exception
		{
			return getEquellaWebservice(secret, false);
		}

		public EQUELLAWSStub getEquellaWebservice(String secret, boolean forceLogin) throws Exception
		{
			// what's this? see:
			// http://stackoverflow.com/questions/3578604/how-to-solve-the-double-checked-locking-is-broken-declaration-in-java
			EQUELLAWSStub tempService = equellaWebservice;
			if( tempService == null )
			{
				synchronized( webserviceLock )
				{
					tempService = equellaWebservice;
					if( tempService == null )
					{
						equellaWebservice = new EQUELLAWSStub(ctx, PathUtils.urlPath(bbUrl,
							"/webapps/ws/services/EQUELLA.WS"));
						tempService = equellaWebservice;
						initStub(tempService);
					}
				}
			}
			ensureLogin(secret, forceLogin);
			return tempService;
		}

		private void ensureLogin(String secret, boolean forceLogin) throws Exception
		{
			// 59 minutes
			if( forceLogin || sessionId == null )
			{
				// call initialize method of the Context web service to get the
				// sessionid
				// set the sessionid on the callback handler so it is used by
				// all subsequent webservice calls.
				sessionId = contextWebservice.initialize().get_return();

				final LoginTool loginArgs = new LoginTool();
				loginArgs.setPassword(secret);
				loginArgs.setClientVendorId(VENDOR_ID);
				loginArgs.setClientProgramId(PROGRAM_ID);
				loginArgs.setLoginExtraInfo("");
				loginArgs.setExpectedLifeSeconds(LOGIN_DURATION_SECONDS);

				final boolean loginResult = contextWebservice.loginTool(loginArgs).get_return();
				if( !loginResult )
				{
					throw new BlackboardException(getString("error.login"));
				}
			}
		}

		private void initStub(Stub stub) throws AxisFault
		{
			if( stub != null )
			{
				final ServiceClient client = stub._getServiceClient();

				final Options options = client.getOptions();
				options.setProperty(WSHandlerConstants.PW_CALLBACK_REF, this);
				options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, ofc.getProperty());
				options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, "true");
				options.setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, HTTPConstants.HEADER_PROTOCOL_11);
				URI uri = URI.create(bbUrl);
				if( uri.getScheme().toLowerCase().startsWith("https") )
				{
					Protocol myhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
					options.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, myhttps);
				}
				client.engageModule("rampart-1.5.1");
			}
		}

		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
		{
			final String pw = sessionId == null ? "nosession" : sessionId;
			for( int i = 0; i < callbacks.length; i++ )
			{
				final WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
				pwcb.setPassword(pw);
			}
		}
	}

	private static class CustomConfigurator extends DeploymentEngine implements AxisConfigurator
	{
		@Override
		public void loadServices()
		{
			// Do we really need????
			// super.loadServices();
		}

		@Override
		public AxisConfiguration getAxisConfiguration() throws AxisFault
		{
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			OMAbstractFactory.setMetaFactoryLocator(new OMMetaFactoryLocator()
			{
				@Override
				public OMMetaFactory getOMMetaFactory(String feature)
				{
					return new OMLinkedListMetaFactory();
				}
			});
			axisConfig = populateAxisConfiguration(getClass().getResourceAsStream(
				"/com/tle/core/connectors/blackboard/service/axis2.xml"));

			AxisModule module = new AxisModule("rampart");
			module.setModuleClassLoader(getClass().getClassLoader());
			module.setParent(axisConfig);
			module.setArchiveName("rampart-1.5.1");

			ModuleBuilder moduleBuilder = new ModuleBuilder(getClass().getResourceAsStream(
				"/com/tle/core/connectors/blackboard/service/module.xml"), module, axisConfig);
			moduleBuilder.populateModule();
			addNewModule(module, axisConfig);

			loadFromClassPath();

			axisConfig.setConfigurator(this);

			return axisConfig;
		}

		@Override
		public void engageGlobalModules() throws AxisFault
		{
			super.engageModules();
		}
	}

	private interface BbFunction<O>
	{
		O execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException;
	}

	@Override
	public boolean supportsExport()
	{
		return true;
	}

	@Override
	public boolean supportsEdit()
	{
		return true;
	}

	@Override
	public boolean supportsView()
	{
		return true;
	}

	@Override
	public boolean supportsDelete()
	{
		return true;
	}

	@Override
	public boolean supportsReverseSort()
	{
		return true;
	}

	@Override
	public boolean deleteContent(Connector connector, String username, final String contentId)
		throws LmsUserNotFoundException
	{
		if( contentId.startsWith("u") )
		{
			// BANG
			throw new RuntimeException(getString("error.oldid"));
		}

		return doBb(connector.getServerUrl(), username, true, new BbFunction<Boolean>()
		{
			@Override
			public Boolean execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException
			{
				final DeleteContent deleteContent = new DeleteContent();
				deleteContent.setContentId(contentId);
				return equellaWs.deleteContent(deleteContent).get_return();
			}
		});
	}

	@Override
	public boolean editContent(Connector connector, String username, final String contentId, final String title,
		final String description) throws LmsUserNotFoundException
	{
		if( contentId.startsWith("u") )
		{
			// BANG
			throw new RuntimeException(getString("error.oldid"));
		}

		return doBb(connector.getServerUrl(), username, true, new BbFunction<Boolean>()
		{
			@Override
			public Boolean execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException
			{
				final EditContent editContent = new EditContent();
				editContent.setContentId(contentId);
				// String url, String xml
				editContent.setTitle(title);
				editContent.setDescription(description);
				editContent.setInstitutionUrl(urlService.getInstitutionUrl().toString());

				return equellaWs.editContent(editContent).get_return();
			}
		});
	}

	@Override
	public boolean moveContent(Connector connector, String username, final String contentId, final String courseId,
		final String locationId) throws LmsUserNotFoundException
	{
		if( contentId.startsWith("u") )
		{
			// BANG
			throw new RuntimeException(getString("error.oldid"));
		}

		return doBb(connector.getServerUrl(), username, true, new BbFunction<Boolean>()
		{
			@Override
			public Boolean execute(ContextWSStub contextWs, EQUELLAWSStub equellaWs) throws RemoteException
			{
				final MoveContent moveContent = new MoveContent();
				moveContent.setContentId(contentId);
				moveContent.setCourseId(courseId);
				moveContent.setFolderId(locationId);
				return equellaWs.moveContent(moveContent).get_return();
			}
		});
	}

	private <T extends Base> Map<String, T> mapperise(T[] bases)
	{
		final Map<String, T> map = Maps.newHashMap();
		if( bases != null )
		{
			for( T base : bases )
			{
				map.put(base.getId(), base);
			}
		}
		return map;
	}

	@Override
	public boolean supportsCourses()
	{
		return true;
	}

	@Override
	public boolean supportsFindUses()
	{
		return true;
	}
}

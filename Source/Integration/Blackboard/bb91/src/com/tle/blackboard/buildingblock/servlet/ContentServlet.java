package com.tle.blackboard.buildingblock.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import blackboard.data.ExtendedData;
import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.gradebook.impl.OutcomeDefinition;
import blackboard.persist.Id;
import blackboard.persist.content.ContentDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.gradebook.impl.OutcomeDefinitionDbLoader;
import blackboard.platform.ContentWrapperHelper;
import blackboard.platform.blti.BasicLTILauncher.IdTypeToSend;
import blackboard.platform.plugin.PlugInUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.blackboard.buildingblock.BlockUtil;
import com.tle.blackboard.buildingblock.Configuration;
import com.tle.blackboard.buildingblock.data.WrappedContent;
import com.tle.blackboard.buildingblock.data.WrappedCourse;
import com.tle.blackboard.buildingblock.lti.FixedBasicLtiLauncher;
import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.PathUtils;
import com.tle.blackboard.common.content.ContentUtil;
import com.tle.blackboard.common.content.ItemInfo;
import com.tle.blackboard.common.content.ItemUtil;
import com.tle.blackboard.common.content.LegacyItemUtil;

@SuppressWarnings("nls")
public class ContentServlet extends HttpServlet
{
	private static final String COURSE_ID = "course_id";
	private static final String CONTENT_ID = "content_id";
	private static final String UTF_8 = "utf-8";
	private static final String PARAM_PREFIX = "bb_";

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String FIELD_DESCRIPTION = "description";
	private static final String FIELD_URL = "url";

	@Override
	protected void service(@SuppressWarnings("null") HttpServletRequest request,
		@SuppressWarnings("null") HttpServletResponse response) throws ServletException, IOException
	{
		final String servletPath = request.getServletPath();
		BbUtil.trace("ContentServlet: " + servletPath);
		try
		{
			if( servletPath.contains("ViewContentBody") )
			{
				viewContentBody(request, response);
			}
			// The outer frameset, with BB breadcrumbs
			else if( servletPath.contains("ViewContent") )
			{
				viewContentBody(request, response);
				//forwardToContentWrapper(request, response, "ViewContentBody");
			}
			// The outer frameset, with BB breadcrumbs, for clicking on links in gradebook
			else if( servletPath.contains("ViewGradebook") )
			{
				// Extract the contentId from the outcomedefinition. Blow up if there isn't one.
				final String outcomeDefinitionId = request.getParameter("outcomeDefinitionId");
				if( Strings.isNullOrEmpty(outcomeDefinitionId) )
				{
					throw new RuntimeException("No outcomeDefinitionId parameter");
				}
				final OutcomeDefinitionDbLoader outcomeDbLoader = getOutcomeDefinitionLoader();
				final OutcomeDefinition outcomeDefinition = outcomeDbLoader.loadById(BbUtil.getId(outcomeDefinitionId,
					OutcomeDefinition.DATA_TYPE));
				final Id contentId = outcomeDefinition.getContentId();
				//forwardToContentWrapper(request, response, contentId, "ViewContentBody");
			}
			else if( servletPath.contains("AddContentBody") )
			{
				addContentBody(request, response);
			}
			else if( servletPath.contains("AddContentCallback") )
			{
				addContent(request, response);
			}
		}
		catch( Exception e )
		{
			BbUtil.error("Error in ContentServlet", e);
			request.setAttribute("javax.servlet.jsp.jspException", e);
			request.getRequestDispatcher("/error.jsp").forward(request, response);
		}
	}

	private OutcomeDefinitionDbLoader outcomeDefinitionLoader;

	private OutcomeDefinitionDbLoader getOutcomeDefinitionLoader()
	{
		if( outcomeDefinitionLoader == null )
		{
			BbUtil.trace("Content loader not yet initialised");
			try
			{
				return (OutcomeDefinitionDbLoader) BbContext.instance().getPersistenceManager()
					.getLoader(OutcomeDefinitionDbLoader.TYPE);
			}
			catch( final Exception e )
			{
				BbUtil.error("Error creating outcome loader", e);
				throw Throwables.propagate(e);
			}
		}
		return outcomeDefinitionLoader;
	}

	/**
	 * The outer frameset, with BB breadcrumbs
	 * 
	 * @param request
	 * @param response
	 * @param path
	 */
	public static void forwardToContentWrapper(HttpServletRequest request, HttpServletResponse response, String path)
	{
		final Id contentId = BbUtil.getContentId(request.getParameter(CONTENT_ID));
		forwardToContentWrapper(request, response, contentId, path);
	}

	/**
	 * The outer frameset, with BB breadcrumbs
	 * 
	 * @param request
	 * @param response
	 * @param contentId
	 * @param path
	 */
	public static void forwardToContentWrapper(HttpServletRequest request, HttpServletResponse response, Id contentId,
		String path)
	{
		try
		{
			final Content bbContent = ContentUtil.instance().loadContentForViewing(contentId);

			final StringBuilder resourceHref = new StringBuilder(PathUtils.urlPath(BbUtil.getBlockRelativePath(), path));
			// Foward the outer frame params
			final String queryString = request.getQueryString();
			if( !Strings.isNullOrEmpty(queryString) )
			{
				resourceHref.append("?").append(queryString);
				// To make gradebook work. Pass on the content id to the inner iframe URL
				if( !queryString.contains(CONTENT_ID) )
				{
					resourceHref.append("&" + CONTENT_ID + "=").append(
						URLEncoder.encode(contentId.toExternalString(), UTF_8));
				}
			}

			//			final CourseMembership membership = getCourseMembershipFromRequest(request);
			//			final String displayName;
			//			if( membership != null )
			//			{
			//				// Append the user details for viewing from gradebook
			//				displayName = URLEncoder.encode(bbContent.getTitle() + " (" + membership.getUser().getUserName() + ")",
			//					UTF_8);
			//			}
			//			else
			//			{
			//				displayName = URLEncoder.encode(bbContent.getTitle(), UTF_8);
			//			}

			final String courseIdString = request.getParameter(COURSE_ID);
			String contentWrapperLink = ContentWrapperHelper.getContentWrapperLink(
				Id.generateId(Course.DATA_TYPE, courseIdString), bbContent, resourceHref.toString());
			response.sendRedirect(contentWrapperLink);

			//			final StringBuilder redirect = new StringBuilder(BlockUtil.getBbUrl(request,
			//				"webapps/blackboard/content/contentWrapper.jsp")).append("?" + CONTENT_ID + "=")
			//				.append(URLEncoder.encode(contentId.toExternalString(), UTF_8)).append("&displayName=")
			//				.append(displayName).append("&" + COURSE_ID + "=").append(URLEncoder.encode(courseIdString, UTF_8))
			//				.append("&navItem=content&href=").append(URLEncoder.encode(resourceHref.toString(), UTF_8));
			//			response.sendRedirect(redirect.toString());
		}
		catch( final Exception e )
		{
			BbUtil.error("Error in ViewContent", e);
			throw Throwables.propagate(e);
		}
	}

	private void viewContentBody(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			final Configuration configuration = Configuration.instance();
			final Id courseId = BbUtil.getCourseId(request.getParameter(COURSE_ID));
			final Id contentId = BbUtil.getContentId(request.getParameter(CONTENT_ID));

			final Content bbContent = ContentUtil.instance().loadContentForViewing(contentId);
			final WrappedContent content = new WrappedContent(new WrappedCourse(courseId), bbContent);
			// ensure the handler stuff is setup
			Configuration.instance(); // ensures it's loaded

			final String page = request.getParameter("page");
			final String launchUrl;
			final String name;
			final String description;
			if( page != null || ContentUtil.instance().isLegacy(bbContent) )
			{
				// Some legacy items / attachments have pages that need to be
				// URL encoded.
				BbUtil.error("Requested legacy item page [" + page + "]", null);
				String encodedPage = LegacyItemUtil.scrubLegacyPage(page);
				if( !encodedPage.equals(page) )
				{
					BbUtil.error("Notice:  The legacy item page needed to be url encoded: original=[" + page
						+ "] encoded=[" + encodedPage + "].  Giving Blackboard the encoded version.", null);
				}
				final ItemInfo itemInfo = LegacyItemUtil.getItemInfo(content.getContent(), content.getCourse()
					.getCourse(), content.getParentId(), configuration.getEquellaUrl());
				launchUrl = getLaunchUrl(
					PathUtils.urlPath("integ/gen", itemInfo.getItemKey().getUuid(),
						Integer.toString(itemInfo.getItemKey().getVersion())), encodedPage);
				name = itemInfo.getName();
				description = itemInfo.getDescription();
			}
			else
			{
				final ExtendedData extendedData = content.getContent().getExtendedData();
				name = content.getTitle();
				description = extendedData.getValue(FIELD_DESCRIPTION);
				launchUrl = extendedData.getValue(FIELD_URL);
			}

			final FixedBasicLtiLauncher launcher = FixedBasicLtiLauncher.newLauncher(configuration, launchUrl,
				contentId.toExternalString());
			launcher.addResourceLinkInformation(name, description);

			// Check course membership as supplied from the gradebook
			final CourseMembership membership = getCourseMembershipFromRequest(request);
			if( membership == null )
			{
				launcher.addGradingInformation(request, content);
				launcher.addCurrentUserInformation(true, true, true, IdTypeToSend.PK1);
			}
			else
			{
				launcher.addGradingInformation(bbContent, membership);
				launcher.addUserInformation(membership.getUser(), membership, true, true, true, IdTypeToSend.PK1);
			}

			launcher.addCurrentCourseInformation(IdTypeToSend.PK1);
			launcher.addReturnUrl(getReturnUrl(request, contentId, courseId));
			launcher.launch(request, response, false, null);
		}
		catch( final Exception e )
		{
			BbUtil.error("Error in ContentBody viewer", e);
			throw Throwables.propagate(e);
		}
	}

	/**
	 * @return null if there is no courseMembershipId in the query string 
	 */
	private static CourseMembership getCourseMembershipFromRequest(HttpServletRequest request) throws Exception
	{
		final String courseMembershipId = request.getParameter("courseMembershipId");
		if( courseMembershipId == null )
		{
			// TODO: Could extract it from the attempt ID as well?
			// final String attemptId = request.getParameter("attempt_id");
			return null;
		}

		final CourseMembershipDbLoader membershipLoader = (CourseMembershipDbLoader) BbContext.instance()
			.getPersistenceManager().getLoader(CourseMembershipDbLoader.TYPE);
		final CourseMembership membership = membershipLoader.loadById(
			BbUtil.getId(courseMembershipId, CourseMembership.DATA_TYPE), null, true);
		return membership;
	}

	private String getReturnUrl(HttpServletRequest request, Id contentId, Id courseId)
	{
		final String gradebookReturn = request.getParameter("cancelGradeUrl");
		if( !Strings.isNullOrEmpty(gradebookReturn) )
		{
			return PathUtils.urlPath(BlockUtil.getBBRootUrl(request), gradebookReturn);
		}
		final String returnPath = PlugInUtil.getDisplayContentReturnURL(contentId, courseId);
		return BlockUtil.getBbUrl(request, returnPath);
	}

	private void addContentBody(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		try
		{
			final WrappedContent content = new WrappedContent(request);
			final WrappedCourse course = content.getCourse();

			BbUtil.trace("Getting config");
			final Configuration configuration = Configuration.instance();

			BbUtil.trace("New launcher");
			final FixedBasicLtiLauncher launcher = FixedBasicLtiLauncher.newLauncher(configuration, "signon.do", "");
			launcher.addCurrentCourseInformation(IdTypeToSend.PK1).addCurrentUserInformation(true, true, true,
				IdTypeToSend.PK1);
			BbUtil.trace("Created launcher");
			launcher.addPostData("method", "lms");
			launcher.addPostData("forcePost", "true");
			launcher.addPostData("attachmentUuidUrls", "true");
			//Depends on BB version
			System.out.println(BbUtil.getBbVersionString());
			launcher.addPostData("cancelDisabled", "false");
			launcher.addPostData("returnprefix", PARAM_PREFIX);

			final String returnPath = PlugInUtil.getUri(BbUtil.VENDOR, BbUtil.HANDLE, "AddContentCallback");
			BbUtil.trace("Return path=" + returnPath);
			String returnUrl = BlockUtil.getBbUrl(request, returnPath) + "?" + content.getParameterString();
			BbUtil.trace("Base return url=" + returnUrl);

			BbUtil.trace("Return url=" + returnUrl);
			launcher.addReturnUrl(returnUrl);
			launcher.addPostData("returnurl", returnUrl);

			final String restriction = configuration.getRestriction();
			if( restriction != null && !restriction.equals("none") )
			{
				BbUtil.trace("restriction=" + restriction);
				launcher.addPostData(restriction, "true");
			}
			final String action = request.getParameter("action");
			BbUtil.trace("action=" + action);
			if( action != null )
			{
				launcher.addPostData("action", action);

				final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				final Date courseStartDate = course.getStartDate();
				if( courseStartDate != null )
				{
					launcher.addPostData("startDate", df.format(courseStartDate));
				}
				final Date courseEndDate = course.getEndDate();
				if( courseEndDate != null )
				{
					launcher.addPostData("endDate", df.format(courseEndDate));
				}
				launcher.addPostData("courseCode", course.getCode());
				launcher.addPostData("courseName", course.getTitle());
				launcher.addPostData("contentName", content.getTitle());
				if( action.equals("structured") )
				{
					BbUtil.trace("building structure JSON");
					String activeFolderId = null;
					if( content.isFolder() )
					{
						activeFolderId = content.getId().toExternalString();
					}
					final ObjectNode structure = mapper.createObjectNode();
					structure.put("code", course.getCourse().getCourseId());
					structure.put("id", course.getId().toExternalString());
					structure.put("name", course.getTitle());
					structure.put("targetable", false);
					final ArrayNode foldersArray = mapper.createArrayNode();
					final Iterable<Content> folders = BbUtil.getBbFoldersForCourse(course.getId());
					for( Content folder : folders )
					{
						final ObjectNode folderNode = mapper.createObjectNode();
						folderNode.put("id", folder.getId().toExternalString());
						folderNode.put("name", folder.getTitle());
						if( activeFolderId != null && folder.getId().toExternalString().equals(activeFolderId) )
						{
							folderNode.put("selected", true);
						}
						handleSubFolders(folderNode, folder, activeFolderId);
						foldersArray.add(folderNode);
					}
					if( foldersArray.size() > 0 )
					{
						structure.put("folders", foldersArray);
					}
					// Single line structure
					final String s = mapper.writeValueAsString(structure);
					BbUtil.trace("structure=" + s);
					launcher.addPostData("structure", s);
				}
				// pass on any supplied params
				final String options = request.getParameter("options");
				if( options != null )
				{
					BbUtil.trace("options=" + options);
					launcher.addPostData("options", options);
				}
			}
			BbUtil.trace("Launching...");
			launcher.launch(request, response, false, null);
		}
		catch( final Exception e )
		{
			BbUtil.error("Error posting request to signon.do", e);
			request.setAttribute("javax.servlet.jsp.jspException", e);
			request.getRequestDispatcher("/error.jsp").forward(request, response);
		}
	}

	private void addContent(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(ContentDbLoader.class.getClassLoader());
			final Configuration configuration = Configuration.instance();
			final String equellaUrl = configuration.getEquellaUrl();
			BbUtil.trace("returnFromSelectionSession");

			final WrappedContent folder = new WrappedContent(request);
			final WrappedCourse course = folder.getCourse();
			BbUtil.trace(course);

			final String links = request.getParameter(PARAM_PREFIX + "links");
			BbUtil.trace("links=" + links);

			if( links != null )
			{
				final ArrayNode an = (ArrayNode) mapper.readTree(links);

				final Iterator<JsonNode> nodeIter = an.elements();
				while( nodeIter.hasNext() )
				{
					final ObjectNode link = (ObjectNode) nodeIter.next();
					final String title = nodeValue(link, "name", "");
					final String itemName = nodeValue(link, "itemName", title);
					final String itemDescription = nodeValue(link, "itemDescription", "");

					final String description = nodeValue(link, "description", itemDescription);
					final String url = ItemUtil.convertToRelativeUrl(equellaUrl, nodeValue(link, "url", ""));
					final String uuid = nodeValue(link, "uuid", "");
					final int version = nodeValue(link, "version", 0);
					final String folderIdString = nodeValue(link, "folder", null);
					final Id folderId = folderIdString == null ? folder.getId() : BbUtil.getFolderId(folderIdString);
					final String attachmentUuid = nodeValue(link, "attachmentUuid", null);
					final String attachmentName = nodeValue(link, "attachmentName", null);
					String mimeTypeKey = "mimeType";
					if( nodeValue(link, mimeTypeKey, null) == null )
					{
						// Some versions of Equella, like 6.3-QA2, will return
						// the mime type with a lower case t.
						mimeTypeKey = "mimetype";
					}
					final String mimeType = nodeValue(link, mimeTypeKey, null);
					ContentUtil.instance().addContent(course.getCourse(), folderId, uuid, version, url, itemName,
						description, attachmentUuid, attachmentName, mimeType, equellaUrl, configuration.isNewWindow());
				}
			}

			final String contentIdString = request.getParameter(CONTENT_ID);
			final Id contentId = BbUtil.getContentId(contentIdString);
			final String contentReturnUrl = PlugInUtil.getContentReturnURL(contentId, course.getId());
			BbUtil.trace("redirecting to " + contentReturnUrl);
			// Return from content creation
			response.sendRedirect(contentReturnUrl);
		}
		catch( final Exception e )
		{
			BbUtil.error("Error in AddContent", e);
			throw Throwables.propagate(e);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	private String nodeValue(JsonNode parent, String nodeName, String defaultValue)
	{
		JsonNode jsonNode = parent.get(nodeName);
		if( jsonNode == null )
		{
			return defaultValue;
		}
		final String textValue = jsonNode.textValue();
		if( Strings.isNullOrEmpty(textValue) )
		{
			return (defaultValue == null ? "" : defaultValue);
		}
		return textValue;
	}

	private int nodeValue(JsonNode parent, String nodeName, int defaultValue)
	{
		JsonNode jsonNode = parent.get(nodeName);
		if( jsonNode == null )
		{
			return defaultValue;
		}
		return jsonNode.intValue();
	}

	// Not sure we need this crappiness anymore
	@SuppressWarnings("unused")
	private String getLaunchUrl(String itemUrl, String page)
	{
		if( page != null )
		{
			try
			{
				new URL(page);
				// is a URL, don't modify it
				return page;
			}
			catch( MalformedURLException mal )
			{
				return PathUtils.urlPath(itemUrl, page);
			}
		}
		return itemUrl;
	}

	private void handleSubFolders(ObjectNode parentFolderNode, Content parentFolder, String activeFolderId)
		throws Exception
	{
		final ArrayNode foldersArray = mapper.createArrayNode();
		for( Content folder : BbUtil.getBbContentForFolder(parentFolder.getId()) )
		{
			if( folder.getIsFolder() )
			{
				final ObjectNode folderNode = mapper.createObjectNode();
				folderNode.put("id", folder.getId().toExternalString());
				folderNode.put("name", folder.getTitle());

				// this is implied, let's cut down on post size
				// folderNode.put("targetable", true);
				if( activeFolderId != null && folder.getId().toExternalString().equals(activeFolderId) )
				{
					folderNode.put("selected", true);
				}
				handleSubFolders(folderNode, folder, activeFolderId);
				foldersArray.add(folderNode);
			}
		}
		if( foldersArray.size() > 0 )
		{
			parentFolderNode.put("folders", foldersArray);
		}
	}
}
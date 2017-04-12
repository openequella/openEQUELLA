<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@page import="java.util.Collection"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URL"%>
<%@page import="java.net.MalformedURLException"%>
<%@page import="java.util.Iterator"%>
<%@page import="blackboard.platform.plugin.PlugInUtil"%>
<%@page import="blackboard.platform.session.BbSession"%>
<%@page import="blackboard.platform.security.AccessManagerService"%>
<%@page import="blackboard.platform.session.BbSessionManagerService"%>
<%@page import="blackboard.platform.BbServiceManager"%>
<%@page import="blackboard.platform.context.ContextManager"%>
<%@page import="com.tle.blackboard.buildingblock.data.WrappedUser"%>
<%@page import="com.tle.blackboard.buildingblock.Configuration"%>
<%@page import="com.tle.blackboard.common.BbUtil"%>
<%@ taglib uri="/bbNG" prefix="bbng"%>
<%@ taglib uri="/tle" prefix="tle"%>

<%! 
WrappedUser user;
private String link(String url) throws Exception
{
	try
	{
		String token = user.getToken();
		if (url.indexOf('?') == -1)
		{
			url += "?";
		}
		else
		{
			url += "&";
		}
		url = new URL(new URL(Configuration.instance().getEquellaUrl()), url).toString();
		return url+"token="+URLEncoder.encode(token,"utf-8");
	}
	catch (MalformedURLException mal)
	{
		return "#";
	}
	catch (Exception e)
	{
		if (e.getCause() instanceof MalformedURLException)
		{
			return "#";
		}
		else
		{
			throw e;
		}
	}
}
%>
<%
ContextManager ctxMgr = (ContextManager)BbServiceManager.lookupService( ContextManager.class );
ctxMgr.setContext( request );
BbSessionManagerService sessionService = BbServiceManager.getSessionManagerService();
BbSession bbSession = sessionService.getSession( request );
AccessManagerService accessManager = (AccessManagerService) BbServiceManager.lookupService( AccessManagerService.class );
if (! bbSession.isAuthenticated()) {
    accessManager.sendLoginRedirect(request,response);
    return;
}

PlugInUtil.authorizeForSystemAdmin(request, response);
String error = "";
String message = "";
Configuration configuration = Configuration.instance();
user = WrappedUser.getUser(request);
try
{
	if(request.getMethod().equals( "POST" ))
	{
		configuration.modify(request);
		configuration.save();
		message = "Your settings have been saved";
	}
}
catch(Exception e)
{
	error = "Error saving settings: " + e.getMessage();
}

configuration.load();
String equellaurl = configuration.getEquellaUrl();
String version = configuration.getVersion();
String clientId = configuration.getOauthClientId();
String clientSecret = configuration.getOauthClientSecret();
String secretId = configuration.getSecretId();
String secret = configuration.getSecret();
String restriction = configuration.getRestriction();
boolean newWindow = configuration.isNewWindow();

String title = "EQUELLA Server Configuration";
int number = 1;
%>
<bbng:genericPage title="EQUELLA Configuration">
	<bbng:breadcrumbBar navItem="admin_plugin_manage" environment="SYS_ADMIN">
		<bbng:breadcrumb title="<%=title%>" />
	</bbng:breadcrumbBar>
	<bbng:pageHeader>
		<bbng:pageTitleBar title="<%=title%>" iconUrl="../images/tle.gif" />
	</bbng:pageHeader>

	<form action="config.jsp" method="POST">
		<bbng:dataCollection>
			<bbng:step title="EQUELLA Server Details">
				<bbng:dataElement label="EQUELLA URL" isRequired="true">
					<bbng:textElement isRequired="true" size="100" name="<%=Configuration.EQUELLA_URL%>" value="<%=equellaurl%>"/>
				</bbng:dataElement>
				
				<bbng:dataElement label="LTI Consumer ID" isRequired="true">
					<bbng:textElement isRequired="true" size="40" name="<%=Configuration.OAUTH_CLIENT_ID%>" value="<%=clientId%>"/>
				</bbng:dataElement>
				<bbng:dataElement label="LTI Consumer Secret" isRequired="true">
					<bbng:textElement isRequired="true" size="40" name="<%=Configuration.OAUTH_CLIENT_SECRET%>" value="<%=clientSecret%>"/>
				</bbng:dataElement>
				
				<bbng:dataElement label="Shared Secret ID" isRequired="true">
					<bbng:textElement isRequired="true" size="40" name="<%=Configuration.SECRETID%>" value="<%=secretId%>"/>
				</bbng:dataElement>
				<bbng:dataElement label="Shared Secret Value" isRequired="true">
					<bbng:textElement isRequired="true" size="40" name="<%=Configuration.SECRET%>" value="<%=secret%>"/>
				</bbng:dataElement>

				<% if(message.length() > 0) { %>
					<bbng:dataElement label="" >
						<div style="{color:blue}"><%=message%></div>
					</bbng:dataElement>
				<% } %>

				<% if(error.length() == 0) { %>
					<bbng:dataElement label="">
						<div><a href="<%=link("jnlp/admin.jnlp?rand="+System.currentTimeMillis())%>" target="_blank">Administration Console</a></div>
					</bbng:dataElement>
				<% } else { %>
					<bbng:dataElement label="Error" >
						<span style="color:red"><%=error%></span>
					</bbng:dataElement>
				<% } %>
			</bbng:step>
			
			<bbng:step title="Options">
				<bbng:dataElement label="Restrict selection of EQUELLA content">
					<bbng:selectElement name="<%=Configuration.RESTRICTIONS %>" >
						<bbng:selectOptionElement value="none" optionLabel="No restrictions" isSelected="<%=restriction.equals(\"none\") %>"/>
						<bbng:selectOptionElement value="itemonly" optionLabel="Items only" isSelected="<%=restriction.equals(\"itemonly\") %>"/>
						<bbng:selectOptionElement value="attachmentonly" optionLabel="Attachments only" isSelected="<%=restriction.equals(\"attachmentonly\") %>"/>
						<bbng:selectOptionElement value="packageonly" optionLabel="Packages only" isSelected="<%=restriction.equals(\"packageonly\") %>"/>	
					</bbng:selectElement>
				</bbng:dataElement>
				
				<bbng:dataElement label="Default new EQUELLA content to open in a new window">
					<bbng:checkboxElement name="<%=Configuration.NEWWINDOW%>" value="true" isSelected="<%=newWindow%>" />
				</bbng:dataElement>
			</bbng:step>
			
			<%
			//Show webservice download if on 9.1 or greater (until we know the version the BB bug is fixed in)
			int major = BbUtil.getMajorVersionNumber(); 
			if (major > 9 || major == 9 && BbUtil.getMinorVersionNumber() >= 1) {
			%>
			<bbng:step title="Web Service Download">
					<bbng:dataElement label="">
						<div>
							<p>The web service is required to be installed for the Blackboard "External System Connector" within EQUELLA to function.</p>
							<p>To install it you must download the web service jar and 
						  		upload it under <a href="/webapps/ws/wsadmin/wsadmin">Web Service administration</a> and then set it to "Available".</p>
						 </div>
					</bbng:dataElement>
					<bbng:dataElement>
						<div>Download the <a href="../webservice.jar">web service jar</a></div>
					</bbng:dataElement>
			</bbng:step>
			<% } %>
			
			<bbng:stepSubmit cancelUrl="../../blackboard/admin/manage_plugins.jsp" />
		</bbng:dataCollection>
	</form>
	
	<div style="font-size: 8pt; color: #C0C0C0">
		<%=version%>
	</div>
</bbng:genericPage>

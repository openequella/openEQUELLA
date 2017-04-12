<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@page import="java.net.URL"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.net.URLEncoder"%>
<%@page
	import="com.tle.blackboard.buildingblock.data.WrappedUser,com.tle.blackboard.buildingblock.data.WrappedUser.TaskLink" %>

<%@taglib uri="http://struts.apache.org/tags-nested" prefix="n"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="l"%>
<%@ taglib uri="/tle" prefix="tle"%>
<%@ taglib uri="/bbNG" prefix="bbng"%>

<%!
WrappedUser user;
private String link(String path) throws Exception
{
	String equellaUrl =  com.tle.blackboard.buildingblock.Configuration.instance().getEquellaUrl();
	String p = path;
	if (!path.startsWith(equellaUrl))
	{
		p = com.tle.blackboard.common.PathUtils.urlPath(com.tle.blackboard.buildingblock.Configuration.instance().getEquellaUrl(), path);
	}
	if (p.indexOf('?') == -1)
	{
		p += "?";
	}
	else
	{
		p += "&";
	}
	return p + "token=" + URLEncoder.encode(user.getToken(), "utf-8");
}
%>
<%
String rel = null;
String dir = null;
user = WrappedUser.getUser(request);
rel = com.tle.blackboard.common.BbUtil.getBlockRelativePath();
dir = rel + "portal/";
try
{
	request.setAttribute("tasks", user.getTaskLinks());
}
catch (Exception e)
{
	request.setAttribute("exception", e);
	e.printStackTrace();
}
%>
<style>
#equella_module
{
	padding: 2px;
}
</style>

<div id="equella_module">
	<div class="section" id="search">
		<div><a href="<%=link("home.do")%>" target="_blank">EQUELLA Home</a></div>
		<div><a href="<%=link("searching.do")%>" target="_blank">Search EQUELLA</a></div>
		<div><a href="<%=link("access/contribute.do")%>" target="_blank">Contribute to EQUELLA</a></div>
	</div>
	<hr>
	<div class="section" id="tasks">
		<n:notEmpty name="tasks">
			<l:iterate id="task" name="tasks">
		        <bean:define property="href" id="href" name="task" type="String"/>
		        <div><a href="<%=href%>" target="_blank"><bean:write name="task" property="text"/></a></div>
			</l:iterate>
		</n:notEmpty>		
		<n:empty name="tasks" >
			<n:notEmpty name="exception">
				<div>Error retrieving task list- <bean:write name="exception" property="message"/></div>
			</n:notEmpty>
			<n:empty name="exception">
				<div>There are no items requiring your attention at this time</div>
			</n:empty>
		</n:empty>
	</div>
</div>

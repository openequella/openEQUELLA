<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@page	import="com.tle.blackboard.buildingblock.data.WrappedContent" %>

<%
	new WrappedContent(request).remove(request);
%>
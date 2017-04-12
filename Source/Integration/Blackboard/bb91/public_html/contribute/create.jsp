<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@page	import="com.tle.blackboard.buildingblock.data.WrappedContent"%>

<%@ taglib uri="/tle" prefix="tle"%>
<%@ taglib uri="/bbNG" prefix="bbng"%>
<tle:context>
	<bbng:jspBlock>
		<%
			String error="";
			try
			{
				WrappedContent content = new WrappedContent(request);
				content.startSelectionSession(request, response);
			}
			catch (Throwable t)
			{
				java.io.StringWriter errors = new java.io.StringWriter();
				t.printStackTrace(new java.io.PrintWriter(errors));
				error = errors.toString();
			}
		%>
		<%=error%>
	</bbng:jspBlock>
</tle:context>
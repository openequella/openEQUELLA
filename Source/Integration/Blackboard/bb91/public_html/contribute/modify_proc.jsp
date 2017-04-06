<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@page	import="com.tle.blackboard.buildingblock.data.WrappedContent" %>

<%@ taglib uri="/bbNG" prefix="bbng"%>
<%@ taglib uri="/tle" prefix="tle"%>

<tle:context>
	<%
		WrappedContent content = new WrappedContent(request);
		content.modify(request);
		content.persist(request);
	%>

	<bbng:learningSystemPage title="Modify Resource Centre Object">
		<bbng:breadcrumbBar environment="COURSE" isContent="true" />

		<bbng:receipt type="SUCCESS" title="Content Updated"
			recallUrl="<%=content.getReferrer(true)%>">
			<h1><%=content.getTitle()%></h1>
			<%=content.getHtml(request, true)%>
			<br>
			<br>
		</bbng:receipt>
	</bbng:learningSystemPage>

</tle:context>
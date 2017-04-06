<%@page
	import="java.util.*,
			java.text.*,
			java.io.*,
			com.tle.integration.blackboard.gbfixer.Fixer"%>

<%@ taglib uri="/bbUI" prefix="ui"%>


<%

	Fixer fixer = Fixer.instance();

	String ex = null;
	String message = null;

	try
	{
		if( fixer.hasFinished())
		{
			message = "Fixer has finished. Looked at " + fixer.getNumResults()
				+ " items and " + fixer.getRemovals() + " gradebook items were removed.";
		}
		else if( fixer.hasStarted() )
		{
			message = "Fixer running (" + fixer.getCount() + " of " 
				+ fixer.getNumResults() + "). " + fixer.getRemovals() + " gradebook items removed.";
		}
		else if( request.getMethod().equals("POST") )
		{
			fixer.submit(request);
			message = "Fixer started...";
		}
	}
	catch( Exception e )
	{
		StringWriter s = new StringWriter();
		PrintWriter w = new PrintWriter(s);
		e.printStackTrace(w);
		ex = s.toString();
	}
	String error = (fixer.getError() == null ? ex : fixer.getError());

	String title = "EQUELLA Gradebook Fixer Plugin";
%>

<ui:docTemplate title="EQUELLA Gradebook Fixer">
	<ui:breadcrumbBar handle="admin_plugin_manage">
		<ui:breadcrumb><%=title%></ui:breadcrumb>
	</ui:breadcrumbBar>
	<h2>Hit submit to remove all non-gradeable EQUELLA content from the gradebook</h2>
	<%
		if(error != null)
		{
	%>
			<ui:dataElement label="Error" >
				<div style="{color:red}">
					<div><pre><%=error%></pre>
				</div>
	  		</ui:dataElement>
	 <%	} 
	 	if(message != null)
	 	{
	 %>
	 		<ui:dataElement label="" >
	   			<div style="{color:blue}">
	   				<%=message%>
	   			</div>
			</ui:dataElement>
	 	
	 <% } %>
	 		
	
	<form action="config.jsp" method="POST">
		<ui:stepSubmit title="Submit" />
	</form>

</ui:docTemplate>
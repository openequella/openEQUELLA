<%@page import="
			java.util.*,
		    java.text.*,
			java.io.*,
			java.util.*,
			com.tle.integration.blackboard.linkfixer.*"
%>

<%@ taglib uri="/bbUI" prefix="ui"%>


	<%
	String error = null;
	String message = null;
	
	Fixer fixer = Fixer.instance();
	
	try
	{
		fixer.load();
		if(request.getMethod().equals( "POST" ))
		{
			fixer.submit(request);
		}
		if (fixer.hasCompleted())
		{
		message = "Fixer has finished.";
	}
		else if (fixer.hasStarted())
		{
		message = "Fixer has been started.  Press Submit again to refresh the page and view the log output.";
		}
		else
		{
		message = "Fixer is ready to start.";
		}
	}
	catch(Exception e)
	{
		StringWriter s = new StringWriter();
		PrintWriter w = new PrintWriter(s);
		e.printStackTrace(w);
		error = s.toString();
	}
	
	String title = "EQUELLA Link Fixer Plugin";
	%>
	
	<ui:docTemplate title="EQUELLA Configuration">
		<ui:breadcrumbBar handle="admin_plugin_manage">
			<ui:breadcrumb><%=title%></ui:breadcrumb>
		</ui:breadcrumbBar>
	
		<ui:titleBar iconUrl="../images/tle.gif"><%=title%></ui:titleBar>
			<form action="config.jsp" method="POST">
	
				<ui:step title="EQUELLA Plugin settings">
					<ui:dataElement label="Institution URL">
  						<span><%=fixer.getEquellaUrl()%></span>
  					</ui:dataElement>
				</ui:step>
				
				<% 
				if (!fixer.hasStarted() && !fixer.hasCompleted()) 
				{
				%>
					<ui:step title="Confirmation">
						<ui:dataElement label="Check this box to begin execution when the Submit button is clicked">
	  						<input type="checkbox" name="<%=Fixer.EXECUTE%>" />
  						</ui:dataElement>
					</ui:step>
				<%
				}
				%>
				
				<ui:step title="Status">
		
						<ui:dataElement label="Execution status:">
		  					<div><%=fixer.getStatus()%></div>
		  				</ui:dataElement>
	
				    	<%
						if(error != null)
						{
						%>
							<ui:dataElement label="Error" ><div style="{color:red}">
								<div><pre><%=error%></pre></div>
				   			</ui:dataElement>
						<%
						}
		
						if(message != null)
						{
						%>
						   	<ui:dataElement label="" >
						   		<div style="{color:blue}"><%=message%></div>
			   				</ui:dataElement>
						<%
						}
						%>
				</ui:step>		
				
				<ui:stepSubmit title="Submit" />
			</form>
	
			<%
			final String log = fixer.getLog(); 
			if (log.length() > 0)
			{
			%><div style="font-family: monospace; font-size: larger;"><pre><%=log%></pre></div>
			<%
			}
			%>
	</ui:docTemplate>
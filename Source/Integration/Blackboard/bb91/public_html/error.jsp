<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html;charset=UTF-8" %>
<%@page import="java.io.PrintWriter"%>
<%@page isErrorPage="true"%>

<%@taglib uri="/bbNG" prefix="bbng"%>
<%@taglib uri="/tle" prefix="tle"%>


<<bbng:genericPage>
	<bbng:jsBlock>
		<script type="text/javascript">
			function swap(name)
			{
				var availability = document.getElementById(name).style.display;
				document.getElementById(name).style.display = availability == 'block' ? 'none' : 'block';
			}
		</script>
	</bbng:jsBlock>
	
	<tle:context>
		<bbng:receipt type="FAIL" title="Error">
			<%
				String strException = exception.getMessage();
						if( strException != null && !strException.equals("") )
						{
							strException = "An error has occurred";
						}
			%>
			<a href="javascript:void(0);" onclick="swap('error')"><%=strException%></a>
			<div id="error" style="display: none;"><pre><bbng:jspBlock>
				<%
					PrintWriter pw = new PrintWriter(out);
								exception.printStackTrace(pw);
				%>
			</bbng:jspBlock></pre></div>
		</bbng:receipt>
	</tle:context>

</bbng:genericPage>
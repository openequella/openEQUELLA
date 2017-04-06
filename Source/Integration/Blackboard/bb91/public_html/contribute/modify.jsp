<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@page	import="com.tle.blackboard.buildingblock.data.WrappedContent" %>
<%@page	import="com.tle.blackboard.common.BbUtil" %>

<%@ taglib uri="/bbNG" prefix="bbng"%>
<%@ taglib uri="/tle" prefix="tle"%>

<tle:context>
	<%
		WrappedContent content = new WrappedContent(request);
	%>
	<bbng:learningSystemPage title="Modify EQUELLA Content">
		<bbng:breadcrumbBar environment="COURSE" isContent="true" >
			<bbng:breadcrumb title="Modify Resource Content Object" />
		</bbng:breadcrumbBar> 
		<bbng:pageHeader>
			<bbng:pageTitleBar title="Modify Resource Content Object" iconUrl="../images/tle.gif" />
		</bbng:pageHeader>
	
		<form name="the_form" action="modify_proc.jsp" method="post">
			<input type="hidden" name="<%=com.tle.blackboard.common.BbUtil.CONTENT_ID%>" value="<%=content.getId().toExternalString()%>">
			<input type="hidden" name="<%=com.tle.blackboard.common.BbUtil.COURSE_ID%>" value="<%=content.getCourse().getId().toExternalString()%>">
			
			<bbng:dataCollection>
				<bbng:step title="Content Information">
					<%	String error = (String)request.getAttribute("error");
						if (error != null)
						{%>		
							<bbng:dataElement>
								<%=error%>
							</bbng:dataElement>
					<%	}	%>
							
					<bbng:dataElement label="Name" labelFor="name">
						<bbng:textElement size="70" isRequired="true" name="name" value="<%=content.getTitle()%>" />
					</bbng:dataElement>
												
					<bbng:dataElement label="Description" labelFor="description">
						<textarea cols="70" rows="5" name="description"><%=BbUtil.ent(content.getDescription())%></textarea>
					</bbng:dataElement>
					
					<bbng:dataElement label="Attachments">
						<%=content.getHtml(request, true)%>
					</bbng:dataElement>
				</bbng:step>
								
				<bbng:step title="Options">
					<bbng:dataElement label="Do you want to make the content visible">
						<bbng:radioElement name="available" value="true"  isSelected="<%= content.isAvailable()%>">Yes</bbng:radioElement>
						<bbng:radioElement name="available" value="false" isSelected="<%=!content.isAvailable()%>">No</bbng:radioElement>
					</bbng:dataElement>
					
					<bbng:dataElement label="Open in new window">
						<bbng:radioElement name="newWindow" value="true"  isSelected="<%= content.isNewWindow()%>">Yes</bbng:radioElement>
						<bbng:radioElement name="newWindow" value="false" isSelected="<%=!content.isNewWindow()%>">No</bbng:radioElement>
					</bbng:dataElement>
				
					<bbng:dataElement label="Do you want to track number of views">
						<bbng:radioElement name="views" value="true"  isSelected="<%= content.getTrackViews()%>">Yes</bbng:radioElement>
						<bbng:radioElement name="views" value="false" isSelected="<%=!content.getTrackViews()%>">No</bbng:radioElement>
					</bbng:dataElement>
				
					<bbng:dataElement label="Do you want to add Metadata">
						<bbng:radioElement name="described" value="true"  isSelected="<%= content.isDescribed()%>">Yes</bbng:radioElement>
						<bbng:radioElement name="described" value="false" isSelected="<%=!content.isDescribed()%>">No</bbng:radioElement>
					</bbng:dataElement>
	
					<bbng:dataElement label="Select Date(s) of Availability">
						<bbng:dateRangePicker baseFieldName="date"
							uncheckStartCheckbox="<%=!content.isDisplayAfter()%>" startDateTime="<%=content.getStartDate()%>"
							uncheckEndCheckbox="<%=!content.isDisplayUntil()%>"  endDateTime="<%=content.getEndDate()%>"/>
					</bbng:dataElement>
				</bbng:step>
				
				<bbng:stepSubmit />
			</bbng:dataCollection>
		</form>
	</bbng:learningSystemPage>
</tle:context>
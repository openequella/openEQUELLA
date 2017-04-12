<%@ page contentType="text/html"%>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="n"%>

<html>
<head>
<link rel="stylesheet" href="styles.css" type="text/css">
<title>Integration Tester</title>
</head>
<body>
	<n:form action="/index">
		<n:hidden property="method" />
		<div class="formrow">
			<label> Method: </label>
			<n:select property="integrationMethod" styleClass="formcontrol">
				<n:optionsCollection property="methods" />
			</n:select>
		</div>
		<div class="formrow">
			<label> Action: </label>
			<n:select property="action" styleClass="formcontrol">
				<n:optionsCollection property="actions" />
			</n:select>
		</div>
		<div class="formrow">
			<label> Options: </label>
			<n:text property="options" styleClass="formcontrol" />
		</div>
		<div class="formrow">
			<label> Template: </label>
			<n:select property="template" styleClass="formcontrol">
				<n:optionsCollection property="templates" />
			</n:select>
		</div>
		<div class="formrow">
			<label> URL: </label>
			<n:text property="url" styleClass="formcontrol" />
		</div>
		<div class="formrow">
			<label> Username: </label>
			<n:text property="username" styleClass="formcontrol" />
		</div>
		<div class="formrow">
			<label> Shared Secret: </label>
			<n:text property="sharedSecret" styleClass="formcontrol" />
		</div>
		<div class="formrow">
			<label> Shared Secret ID: </label>
			<n:text property="sharedSecretId" styleClass="formcontrol" />
		</div>
		<div class="formrow">
			<label> Course ID: </label>
			<n:text property="courseId" styleClass="formcontrol" />
		</div>
		<div class="formrow">
			<label> Select Items only: </label>
			<n:checkbox property="itemonly" />
		</div>
		<div class="formrow">
			<label> Select Packages only: </label>
			<n:checkbox property="packageonly" />
		</div>
		<div class="formrow">
			<label> Select Attachments only: </label>
			<n:checkbox property="attachmentonly" />
		</div>
		<div class="formrow">
			<label> Select multiple: </label>
			<n:checkbox property="selectMultiple" />
		</div>
		<div class="formrow">
			<label> Use download privilege: </label>
			<n:checkbox property="useDownloadPrivilege" />
		</div>
		<div class="formrow">
			<label> Force POST return: </label>
			<n:checkbox property="forcePost" />
		</div>
		<div class="formrow">
			<label> Disabling cancelling: </label>
			<n:checkbox property="cancelDisabled" />
		</div>
		<div class="formrow">
			<label> Generate ?attachment.uuid=abcd URLs: </label>
			<n:checkbox property="attachmentUuidUrls" />
		</div>
		<div class="formrow">
			<label> Generate Return URL: </label>
			<n:checkbox property="makeReturn" />
		</div>
		<div class="formrow">
			<label> Initial item XML: </label>
			<n:textarea property="itemXml" styleClass="itemXml" />
			<n:notEmpty property="itemXml">
				<n:define id="ix" property="itemXml" />
			</n:notEmpty>
		</div>
		<div class="formrow">
			<label> Initial powersearch XML: </label>
			<n:textarea property="powerXml" styleClass="itemXml" />
			<n:notEmpty property="powerXml">
				<n:define id="px" property="powerXml" />
			</n:notEmpty>
		</div>
		<div class="formrow">
			<label> Structure XML: </label>
			<n:textarea property="structure" styleClass="itemXml" />
			<n:notEmpty property="structure">
				<n:define id="sx" property="structure" />
			</n:notEmpty>
		</div>
		<div class="formrow">
			<label> &nbsp; </label>
			<n:submit value="Generate" />
		</div>
		<n:notEmpty property="clickUrl">
			<n:define id="cl" property="clickUrl" />
			<a href="<n:write property="clickUrl" filter="false"/>"><n:write
					property="clickUrl" /> </a>
		</n:notEmpty>
	</n:form>

	<n:notEmpty name="cl">
		<form method="POST" action="<n:write name="cl" filter="false"/>"
			enctype="application/x-www-form-urlencoded">
			<n:notEmpty name="ix">
				<input type="hidden" name="itemXml" value="<n:write name="ix"/>">
			</n:notEmpty>
			<n:notEmpty name="px">
				<input type="hidden" name="powerXml" value="<n:write name="px"/>">
			</n:notEmpty>
			<n:notEmpty name="sx">
				<input type="hidden" name="structure" value="<n:write name="sx"/>">
			</n:notEmpty>
			<input type="submit" value="POST to this URL">
		</form>
	</n:notEmpty>
</body>
</html>

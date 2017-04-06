<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/file.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<div id="institution-admin" class="area">
	<h2>
		${b.gkey("institutions.import.action.name")}
	</h2>	
	<#if m.stagingId??>
		<div>
			${b.key("institution.upload.unzip")}
		  <br>
		  <br>
			<div class="progressbar"></div>
		  <br>
		</div>
	<#else>
		<@settingContainer false>
			<@setting label=b.key("institution.upload.title") error=m.errors['file'] labelFor=s.fileUpload >
				<@file section=s.fileUpload size=35/>
			</@setting>
		</@settingContainer>
		
		<div class="button-strip">
			<@render s.uploadButton>${b.gkey("institutions.import.action.name")}</@render>
		</div>
	</#if>
</div>
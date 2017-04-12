<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<div id="main">
	<@settingContainer mandatory=false>
		<@setting label=b.key('addgrouping.dialog.label.groupname') labelFor=s.groupingName>
			<@textfield section=s.groupingName maxlength=64 class="focus"/>
		</@setting>
	</@settingContainer>
</div>
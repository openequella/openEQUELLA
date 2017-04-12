<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.sections.standard@/list.ftl" />
<#include "/com.tle.web.sections.standard@/radio.ftl"/>
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />

<#--<@settingContainer mandatory=false>
	<@setting label=b.key('handlers.file.label.displayname') section=s.displayName />
</@settingContainer>-->

<@detailArea>
	<@editArea />
	<#if m.showExpandButtons>
		<@setting label=''>
			<@boollist section=s.expandButtons ; option, check>
				<div class="input radio">
					<@radio check />
				</div>
			</@boollist>
		</@setting>
	</#if>
</@detailArea>
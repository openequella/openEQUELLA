<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css plugin="com.tle.web.sections.equella" path="settings.css" hasRtl=true />

<#macro settingContainer mandatory=true wide=false skinny=false>
	<div class="settingContainer<#if wide> wide</#if><#if skinny> skinny</#if>">
		<#nested>
		<#if mandatory>
			<div class="mandatoryHelp"><span class="mandatory">${b.gkey('com.tle.web.sections.equella.settings.mandatory.symbol')}</span>${b.gkey('com.tle.web.sections.equella.settings.mandatory')}</div>
		</#if>
	</div>
</#macro>

<#macro setting label section="" labelFor="" error="" mandatory=false type="" help="" rowStyle="">
	<div class="settingRow<#if error != ''> ctrlinvaliddiv</#if><#if rowStyle != ''> ${rowStyle}</#if>">
		<div class="settingLabel">
			<#if mandatory == true>
				<abbr title="${b.gkey('com.tle.web.sections.equella.settings.mandatory.field')}">
					<span class="mandatory">${b.gkey('com.tle.web.sections.equella.settings.mandatory.symbol')}</span>
					<label for="<#if section!="" && section["getElementId"]??>${section.getElementId(_info)}<#elseif labelFor?is_string>${labelFor}<#else>${labelFor.getElementId(_info)}</#if>">${label}</label>
				</abbr>
			<#else>
				<label for="<#if section!="" && section["getElementId"]??>${section.getElementId(_info)}<#elseif labelFor?is_string>${labelFor}<#else>${labelFor.getElementId(_info)}</#if>">${label}</label>
			</#if>
		</div>	
		<div class="settingField">
			<div class="control<#if error != ''> ctrlinvalid</#if>">
				<#if section != "">
					<#if type != ""><@render _choose(section, type) /><#else><@render section /></#if>
				</#if>
				<#nested>
				<#if error != ""><p class="ctrlinvalidmessage">${error}</p></#if>
			</div>
			<#if help != "">
				<div class="settingHelp">
					${help}
				</div>
			</#if>
		</div>
	</div>
</#macro> 

<#-- Renders a list of type SettingControl -->
<#macro renderSettings settings>
	<@settingContainer>
		<#list settings as set>
			<@setting label=set.label section=set.component error=set.error mandatory=set.mandatory help=set.help />
		</#list>
	</@settingContainer>
</#macro>

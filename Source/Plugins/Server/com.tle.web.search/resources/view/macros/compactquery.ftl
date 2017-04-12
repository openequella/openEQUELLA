<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/autocomplete.ftl"/>
<#include "/com.tle.web.freemarker@/macro/sections/util.ftl"/>

<@css path="compactquery.css" plugin="com.tle.web.search" hasRtl=true />

<#macro compactquery qf b auto=false >
	<div class="compactQuery">
		<#if auto>
			<@autocomplete section=qf autoSubmitButton=b />
		<#else>
			<@textfield section=qf class="compactQueryField" autoSubmitButton=b />
		</#if>
		<@button section=b class="compactQueryButton" showAs="search" iconOnly=true />
	</div>
</#macro>

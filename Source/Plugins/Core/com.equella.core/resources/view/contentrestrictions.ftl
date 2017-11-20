<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css "contentrestrictions.css" />

<div class="area">
	<h2>${b.key('bannedext.heading')}</h2>
	<p>${b.key('bannedext.description')}</p>
	<@a.div id="bannedExtensions" class="bannedextensionsdiv">
		<@render section=s.bannedExtensions />
	</@a.div>

	<h2>${b.key('userquota.title')}</h2>
	<@a.div id="userQuotas">
		<@render section=s.userQuotas />		
	</@a.div>
</div>

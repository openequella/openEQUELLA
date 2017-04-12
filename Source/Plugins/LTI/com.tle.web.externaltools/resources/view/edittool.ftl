<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>

<@setting label=b.key("editor.baseurl") section=s.baseUrl mandatory=true error=m.errors["errors.baseurl"] help=b.key("editor.help.baseurl") />

<@setting label=b.key("editor.consumerkey") section=s.consumerKey help=b.key("editor.help.consumerkey") />

<@setting label=b.key("editor.sharedsecret") section=s.sharedSecret />

<@setting label=b.key("editor.customparams") help=b.key("editor.help.customparams") >
	<@textarea section=s.customParams rows=3 />
</@setting>

<@setting label=b.key("editor.iconurl") section=s.iconUrl error=m.errors["badiconurl"] help=b.key("editor.help.iconurl") />
<#if m.thumbnailRenderer??>
<div id="current-icon">
	<@setting label=b.key("editor.icon")>
		<@render m.thumbnailRenderer /> 
	</@setting>
</div>
</#if>

<@setting label=b.key("editor.privacy") section=s.shareName help=b.key("editor.help.name") />
<@setting label='' section=s.shareEmail help=b.key("editor.help.email") />


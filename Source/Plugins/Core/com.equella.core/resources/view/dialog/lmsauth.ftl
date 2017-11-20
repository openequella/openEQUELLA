<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "auth.css" />

<div id="auth_container">
	<iframe frameBorder="0" src="${m.authUrl?html}"></iframe>
</div>
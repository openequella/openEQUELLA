<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">

<@css "institutions.css" />

<div class="area">
	<h2>${b.key("logon.error.heading")}</h2>
	<pre>${m.error}</pre>
	<@render s.retryButton/>
</div>

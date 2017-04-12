<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<@css path="contribute.css" plugin="com.tle.web.contribute"/>

<div class="area">
	<h2>${b.key("remoterepos")}</h2>
	<@render section=s.remoteReposTable class="large" />
</div>
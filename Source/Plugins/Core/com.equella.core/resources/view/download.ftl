<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "download.css"/>

<div class="area">
	<h2><@bundlekey "download.title"/></h2>
	
	<p><#if m.attachments><@bundlekey "download.pleasewait.attachments"/><#else><@bundlekey "download.pleasewait.metadata"/></#if></p>

	<@render section=s.progress class="progressbar" />
</div>
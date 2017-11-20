<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@div id="${id}rssPortlet" tag=s.rssEventsTag>
<@css "rss.css" />

<#list m.entries as entry>
	<div class="news-item">
		<h4><@render entry.title /></h4>
		<#if entry.date??>
			<@render class="news-date" section=entry.date />
		</#if>
		<#if entry.description??>
			<p>${entry.description}</p>
		</#if>
	</div>
</#list>

<div class="button-strip">
	<#if m.showMore>
		<@render s.showLessButton><@bundlekey "rss.button.showless"/></@render>
	<#else>
		<@render s.showMoreButton><@bundlekey "rss.button.showmore"/></@render>
	</#if>
</div>

</@div>
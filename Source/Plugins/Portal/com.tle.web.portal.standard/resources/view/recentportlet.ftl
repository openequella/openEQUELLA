<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="recent.css" hasRtl=true />

<@div id="${id}recentPortlet" class="recent-collections">
	<div class="recent-items">
		<#if m.hasError >
			<div class="recent-item">
				<p>${m.error}</p>
			</div>
		<#else>
			<#if m.results?? && m.results?size &gt; 0>
				<#list m.results as result>
				<div class="recent-item">
					<h4><@render result.title /></h4>
					<span class="recent-date">${result.date}</span>
					<#if result.description??><p>${result.description?html}</p></#if>
				</div>
				</#list>
			<#else>
				<div class="recent-item">
					<p><@bundlekey "recent.label.noitems"/></p>
				</div>
			</#if>
		</#if>
	</div>
	<#if m.moreAvailable>
		<div class="button-strip">
			<#if m.showMore>
				<@render s.showLessButton><@bundlekey "recent.button.showless"/></@render>
			<#else>
				<@render s.showMoreButton><@bundlekey "recent.button.showmore"/></@render>
			</#if>
		</div>
	</#if>
</@div>
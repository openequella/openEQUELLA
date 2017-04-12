<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="hierarchy.css" hasRtl=true />

<div class="browse-topics">
	<div class="indent">
		<#if m.subTopics?has_content>
			<ul class="topics">
				<#list m.subTopics as subtopic>
					<li>
						<@render subtopic.link>${subtopic.name} <#if subtopic.showResults==true> (${subtopic.resultCount})</#if></@render>
						<br>
						<span>${subtopic.description}</span>
					</li>
				</#list>
			</ul>
		<#else>
			${b.key('portlet.browse.notopics')}
		</#if>
	</div>
</div>
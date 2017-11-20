<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />

<div class="area browse-topics">
	<h2 class="folder">${m.name}</h2>
	<div class="indent">
		<#if m.topic??><p>${m.description}</p></#if>
		<#if m.subTopics?has_content>
			<#if m.topic??><h4>${m.subtopicName}</h4></#if>
			<ul>
				<#list m.subTopics as subtopic>
					<li>
						<@render subtopic.link>${subtopic.name}</@render>
						<#if subtopic.showResults==true> (${subtopic.resultCount})</#if>
						<br>
						<span>${subtopic.description}</span>
					</li>
				</#list>
			</ul>
		</#if>
		<#if m.showAdvanced>
			<p><@render section=s.advancedSearch type="link" class="action-link">${b.key('advanced.link')}</@render></p>
		</#if>
	</div>
	<@render s.resetFiltersSection/>
	<#if m.queryActions??>
		<div class="queryactions">
			<@renderList m.queryActions />
		</div>
	</#if>
</div>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@render m.divContainer>
	<#list c.renderedGroups as group>
		<div class="input radio option">
			<@render group.check />
		</div>
		<div id="${id}_${group_index?c}" class="indent${c.nestingLevel + 1} wizard-parentcontrol">
	        <#list group.results as result>
	        	<#if result.result??><@render result.result/></#if>
	        </#list>
		</div>
	</#list>
</@render>
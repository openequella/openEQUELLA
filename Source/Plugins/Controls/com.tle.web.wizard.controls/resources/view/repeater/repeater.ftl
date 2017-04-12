<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">

<@div id="${id}_groups">
	<#list m.renderedGroups as group>
		<@div id="${id}_gajax_${group_index?c}" writediv=false>
			<div class="repeater indent${c.nestingLevel + 1}">
				<@render section=group.deleteButton />
				<@render section=group.moveUpButton />
				<@render section=group.moveDownButton />
				<div id="${id}_${group_index?c}" class="wizard-parentcontrol">
					<#list group.results as control>
						<#if control.result??><@render control.result/></#if>
					</#list>
				</div>
			</div>
		</@div>
	</#list>
</@div>

<@button section=s.addButton class="add" showAs="add">${b.gkey("wizard.controls.repeater.add", [wc.noun])}</@button>

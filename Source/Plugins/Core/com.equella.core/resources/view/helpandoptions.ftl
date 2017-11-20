<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@div id="helpAndOptions">
	<div id="button-bar">
		<#list m.buttons as b>
			<@button section=b />
		</#list>
	</div>
	
	<#if m.content??>
		<div id="${m.content.id}" class="topblock" role="complementary">
			<@render m.content.renderable />
		</div>
	</#if>
</@div>
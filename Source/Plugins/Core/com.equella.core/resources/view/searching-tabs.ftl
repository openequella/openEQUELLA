<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@a.div id="searchtabs">
	<#list m.tabs as tab>
		<#if !tab.active>
			<@render tab.link>
				<@render tab.renderable />
			</@render>
		</#if>
	</#list>
</@a.div>
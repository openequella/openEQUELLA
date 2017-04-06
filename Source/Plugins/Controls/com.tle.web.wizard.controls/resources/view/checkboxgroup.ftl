<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.freemarker@/macro/table.ftl">
<#include "/com.tle.web.sections.standard@/list.ftl"/>
<#include "/com.tle.web.sections.standard@/radio.ftl"/>

<@table class="options-control" cellspacing="0" cellpadding="0" cols=s.size1; tabdata>
	<@boollist section=s.list ; option, check>
		<@tr table=tabdata>
			<@td style="width: ${s.columnPercent?c}%" colspan=1 valign="top" table=tabdata>
				<#if s.radio>
				<div class="input radio" >
					<@radio section=check />
				</div>	
				<#else>
				<div class="input radio" >
					<@render section=check />
				</div>	
				</#if>
			</@td>
		</@tr>			
	</@boollist>
</@table>

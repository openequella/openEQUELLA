<#macro attr extra><#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list></#macro>

<#macro table cols=2 extra...>
<#local data="com.tle.web.freemarker.MutableMapModel"?new()>
${data("column", 0)}<#t>
${data("cols", cols)}<#t>
<table<@attr extra/>>
	<#nested data>
</table>
</#macro>

<#macro tr table extra...>
${table("tr", extra)}<#t>
<#nested>
</#macro>

<#macro td table colspan=1 extra...>
<#local column = table.column>
<#if column == 0>
	<tr<@attr table.tr/>>
</#if> 

<#local endcolumn=column+colspan>
<#if endcolumn gt table.cols>
	<td colspan=${cols-column?c}>&nbsp;</td>
	<#local endcolumn=cols>
	</tr>
	<tr <@attr table.tr/>>
</#if>

<td <@attr extra/> <#if colspan gt 1>colspan="${colspan?c}"</#if>>
	<#nested>
</td>

<#if endcolumn==table.cols>
	</tr>
	<#local endcolumn=0>
</#if>

${table("column", endcolumn)}
</#macro>
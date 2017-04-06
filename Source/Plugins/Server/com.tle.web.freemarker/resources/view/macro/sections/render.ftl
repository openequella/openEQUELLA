<#macro render section type="" id="" class="" style="">
	<@_render section=section type=type id=id class=class style=style><#nested/></@_render>
</#macro>

<#macro renderList list separator=""><#t>	
	<#if !(separator?has_content)>
		<#local separator><#nested></#local>
	</#if>
	<#list list as section><#t>
		<#if section??>
			<#if section_index != 0>${separator}</#if><@render section/><#t>
		</#if>
	</#list><#t>
</#macro>

<#macro renderAsHtmlList list class=""><#t>
	<ul<#if class?length gt 0> class="${class}"</#if>><#t>
		<#list list as section><#t>
			<li><@render section/></li><#t>
		</#list><#t>
	</ul><#t>
</#macro>

<#macro row cols current total><#t>
<#local _row="com.tle.web.freemarker.methods.RowMethod"?new()>
<#local nested><#nested></#local><#t>
${_row(cols, current, total, nested)}<#t>
</#macro>
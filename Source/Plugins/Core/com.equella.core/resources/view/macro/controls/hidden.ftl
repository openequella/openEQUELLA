<#ftl strip_whitespace=true strip_text=true />

<#include "../_input.ftl"/>
<#macro hidden id value name=id>
	<#if !value?is_sequence>
		<@input id=id name=name type="hidden" value=value />
	<#else>
		<#list value as val>
			<@input id=id name=name type="hidden" value=val />			
		</#list>
	</#if>
</#macro>
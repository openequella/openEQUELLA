<#ftl strip_whitespace=true strip_text=true />

<#include "../_visinput.ftl"/>
<#macro hidden_method value="">
	<@visinput id="${pfx}method" type="text" 
		value=value style="display:none" class="equellaSectionMethod" />
</#macro>

<#macro method_script method>return set${id}${method}method();<#rt/>
	<#assign PART_FUNCTION_DEFINITIONS>
		<#if PART_FUNCTION_DEFINITIONS??>
			${PART_FUNCTION_DEFINITIONS}
		</#if>
		
		function set${id}${method}method()
		{
			document.getElementById('${pfx}method').value = '${method}';
		}
	</#assign>
</#macro>
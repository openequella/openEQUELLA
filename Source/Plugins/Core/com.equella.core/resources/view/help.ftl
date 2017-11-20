<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}
</script>

<#if m.editing>
	${b.key('help.edit')}
<#else>
	${b.key('help.list')}
</#if>
<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}
</script>

<#if m.tabId == 'summary'>
	${b.key('summary.content.summary.item.selection.summary.help')}
	${b.key('summary.content.summary.item.selection.help')}
<#else>
	${b.key('summary.content.summary.item.selection.details.help')}
</#if>
	${b.key('summary.content.help')}
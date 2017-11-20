<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}
</script>

<#if !m.noCreatePrivs>
<#assign str=b.key('common.createnew.help')>
<#else>
<#assign str="">
</#if>

${b.key('helppage',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'),p.plugUrl('com.tle.web.sections.equella', 'images/component/box_edit.png'),p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'),str)}


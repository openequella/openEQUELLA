<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}
</script>

<#if m.courseSelectionSession??>
	${b.key('searching.selection.helppage',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'),p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
<#else>
	${b.key('searching.helppage',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'),p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
	${b.key('helppage.moreNavigation')}
</#if>


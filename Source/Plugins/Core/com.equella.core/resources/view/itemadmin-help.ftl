<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}

function slideText(id)
{
	$('#' + id).slideToggle();
}
</script>

${b.key('itemadmin.main', '')}

${b.key('itemadmin.moreSearchTerms', '')}
${b.key('itemadmin.moreSearchResults','')}
${b.key('itemadmin.moreSelection',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}
${b.key('itemadmin.moreActions',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}
${b.key('itemadmin.moreShare',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}

${b.key('itemadmin.moreNavigation','')}
${b.key('itemadmin.moreSortBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
${b.key('itemadmin.moreFilterBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}


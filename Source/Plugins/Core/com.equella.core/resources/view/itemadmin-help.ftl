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

${b.key('iteadmin.moreSearchTerms', '')}
${b.key('iteadmin.moreSearchResults','')}
${b.key('iteadmin.moreSelection',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}
${b.key('iteadmin.moreActions',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}
${b.key('iteadmin.moreShare',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}

${b.key('iteadmin.moreNavigation','')}
${b.key('iteadmin.moreSortBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
${b.key('iteadmin.moreFilterBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}


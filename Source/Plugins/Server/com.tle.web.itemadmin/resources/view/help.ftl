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

${b.key('main', '')}

${b.key('moreSearchTerms', '')}
${b.key('moreSearchResults','')}
${b.key('moreSelection',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}
${b.key('moreActions',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}
${b.key('moreShare',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'))}

${b.key('moreNavigation','')}
${b.key('moreSortBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
${b.key('moreFilterBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}


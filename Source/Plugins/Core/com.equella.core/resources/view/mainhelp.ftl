<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}
</script>

${b.key('myresources.main', '')}
${b.key('moreCategories', '')}
${b.key('moreListed','')}
${b.key('moreAddItemFavourites','')}
${b.key('moreAddSearchFavourites','')}
${b.key('moreShare','')}
${b.key('myresources.moreNavigation','')}
${b.key('moreSortBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'),p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
${b.key('moreFilterBox',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_close.png'),p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}


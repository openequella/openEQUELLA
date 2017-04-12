<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}
</script>

${b.key('mainEdit', '')}
${b.key('moreDetailsEdit', '')}
${b.key('moreViewersEdit', '')}
${b.key('moreConfigureViewerEdit', '')}

${b.key('moreNavigation', '')}
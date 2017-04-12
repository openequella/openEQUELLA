<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
function switchText(id)
{     
	 $('#bluebar_help > div').hide();	 
	 $('#' + id).show();
}
</script>

${b.key('main', '')}
${b.key('moreMime', p.url('images/button-add-mime.png'))}
${b.key('moreNavigation', '')}
<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
  function switchText(id) {
    $("#bluebar_help > div").hide();
    $("#" + id).show();
  }
</script>

<#if m.courseSelectionSession??>
	<@bundlekey 'selection.helpfavouritessearches' />
	${b.key('selection.commonmoredivs',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
<#else>
	<@bundlekey 'helpfavouritessearches' />
	${b.key('commonmoredivs',p.plugUrl('com.tle.web.sections.equella', 'images/component/box_head_open.gif'))}
</#if>
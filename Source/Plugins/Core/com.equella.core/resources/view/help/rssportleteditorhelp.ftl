<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
  function switchText(id) {
    $("#bluebar_help > div").hide();
    $("#" + id).show();
  }
</script>
<#assign text=""/>
<#if m.isAdmin() >
	<#assign text><li class="float-left"><a href="javascript:void(0)" onclick="switchText('moreAdministrators')">${b.key('admin.text')}</a></li></#assign>
</#if>

${b.key('rssportleteditorhelp', [text])}
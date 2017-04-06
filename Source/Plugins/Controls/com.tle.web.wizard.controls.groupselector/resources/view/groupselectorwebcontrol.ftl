<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@div id="${id}groupselector">
	<@dialog section=s.selectGroupDialog class="selectgroupdialog"/>
	<@render section=s.groupsTable />
</@div>
<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@div id="${id}roleselector">
	<@dialog section=s.selectRoleDialog class="selectroledialog"/>
	<@render section=s.rolesTable />
</@div>
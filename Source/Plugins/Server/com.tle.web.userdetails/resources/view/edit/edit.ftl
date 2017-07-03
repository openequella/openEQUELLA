<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />

<div id="editUser" class="area">
	<div>
		<h2>${b.key('common.modify')}</h2>
		<#include "sections/user.ftl" /><hr>
		<#include "sections/general.ftl" /><hr>
		<#include "sections/itemdefs.ftl" /><hr>
		<#if m.showWorkflowOptInSection><#include "sections/workflow.ftl" /><hr></#if>
		<#include "sections/languages.ftl" />
	</div>
</div>
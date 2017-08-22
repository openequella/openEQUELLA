<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>

<@css "universalresource.css" />

<@div id="${m.id}universalresources" class="universalresources">
    <#if m.uploadProblem??>
        <div class="ctrlinvalid">
            <p class="ctrlinvalidmessage">${m.uploadProblem}</p>
        </div>
    </#if>
	<@render s.attachmentsTable />
	<#if m.showFileUpload>
        <#if m.canAdd>
            <@filedrop s.fileUpload/>
        </#if>
	</#if>
</@div>

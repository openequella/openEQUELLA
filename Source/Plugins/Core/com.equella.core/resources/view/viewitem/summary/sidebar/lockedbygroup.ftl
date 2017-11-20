<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#assign lockedByUser>
	<@render m.lockedByUser />
</#assign>

<@css "unlock.css" />
<div class="unlock-group">
	<h3>${b.key("summary.sidebar.lockedbygroup.title")}</h3>
	
	<#if m.notPreview>
		<#if m.sections?size gt 0 >
		    <p>${b.key("summary.sidebar.lockedbygroup.description.editing")} </p>
		  	<#list m.sections as s>
		  	    <div class="unlock-action-area">
					<@render s />
				</div>
			</#list>
		<#else>
			<p>
			${b.key("summary.sidebar.lockedbygroup.description", lockedByUser)} 
			<#if m.allowUnlock>
				${b.key("summary.sidebar.lockedbygroup.warning")}
			</#if>
			</p>
		</#if>
	</#if>
			
	<#if m.allowUnlock>
		<div class="unlock-action"><@render s.unlock /></div>
	</#if>
</div>
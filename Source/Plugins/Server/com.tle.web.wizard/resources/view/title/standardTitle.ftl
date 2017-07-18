<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<@css path="controls.css" hasRtl=true/>

<#-- closed in standardTail -->
<#assign controlinvalid=(c.mandatory || c.invalid) && c.message?? /><#t/>

<div class="control<#if controlinvalid> ctrlinvalid</#if>">
<@render c.labelTag>
	<#if c.title?? && c.title?length gt 0>
		<h3> ${c.title} <#if c.mandatory><span class="ctrlmandatory">*</span></#if></h3>
	</#if>
	
	<#if c.description?? && c.description != ''>
		<p>${c.description}</p>
	</#if>
	
	
</@render>
	<p class="ctrlinvalidmessage"><#if controlinvalid>${c.message}</#if></p>
<#if c.groupLabelNeeded>
	<@render c.fieldsetTag> <!--</@render>-->
<#else>
	<fieldset>
</#if>
	

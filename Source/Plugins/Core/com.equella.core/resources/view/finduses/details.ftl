<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<#if m.dateAdded??>
${m.dateAdded}
<#else>
${b.key('finduses.dateadded.unknown')}
</#if>
					
<div class="itemDetails">
	<div><b>${b.key('finduses.label.version')}</b>
		<#if m.version==0>
			${b.key('finduses.value.version.alwayslatest')}
		<#else>
			${m.version?c}
		</#if>
	</div>
	
	<#if m.dateModified??>
		<div><b>${b.key('finduses.datemodified')}: </b>${m.dateModified}</div>
	</#if>
	
	<#if m.attachmentName??>
		<div><b>${b.key("finduses.attachment")}: </b>${m.attachmentName}</div>
	</#if>
	
	<#if m.externalTitle??>
		<div><b>${b.key("finduses.externaltitle")}: </b>${m.externalTitle}</div>
	</#if>
	
	<#list m.attributes as att>
		<#if att.value??>
	    	<div><b>${b.gkey(att.labelKey)?html}: </b><@render att.value/></div>
	    </#if>
	</#list>
</div>
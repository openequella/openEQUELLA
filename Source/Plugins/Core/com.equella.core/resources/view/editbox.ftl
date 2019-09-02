<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@a.div id="${id}_editbox_duplicate_warning">
<#if m.displayDuplicateWarning>
  <span style="color:red">An exact text match has been detected in other items. View duplicate details </span>
  <@link section=s.duplicateMessageLink/>
</#if>
</@a.div>
<div class="input text">
	<#if c.size2 lt 2>
		<@textfield section=s.field maxlength=1000/>
	<#else>
		<@textarea section=s.field rows=c.size2 class="ctrlEditboxMultiLine"/>
	</#if>
</div>

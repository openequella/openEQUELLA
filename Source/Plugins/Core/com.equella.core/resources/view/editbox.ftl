<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@a.div id="${id}_editbox_duplicate_warning">
<#if m.displayDuplicateWarning>
    <p>
      <b style="color:red" role="alert">${m.duplicateWarningMessage}</b>
    </p>
  <@link section=s.duplicateWarningLink/>
</#if>
</@a.div>
<div class="input text">
	<#if c.size2 lt 2>
		<@textfield section=s.field maxlength=1000/>
	<#else>
		<@textarea section=s.field rows=c.size2 class="ctrlEditboxMultiLine"/>
	</#if>
</div>

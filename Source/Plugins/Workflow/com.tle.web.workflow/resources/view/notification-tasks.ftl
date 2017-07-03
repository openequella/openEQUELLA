<#macro tasklist tasks header>
<#assign sizeprefix=(tasks?size == 1)?string('.1','')>
${b.key("emailheader.taskreason."+header+sizeprefix)}

<#list tasks as t>
${b.key('email.itemname')} ${t.itemName}
${b.key('email.taskname')} ${t.taskName}
${b.key('email.tasklink')} ${t.link.href}
<#if t.dueDate??>
${b.key('email.taskdue')} ${t.dueDate()?date?string.medium} <#if t.autoAction??>${t.autoAction}</#if>
</#if>
<#if t.causeLabel??>
${t.causeLabel} ${t.causeTask}
</#if>
<#list t.messages as m>
${m.label} ${b.key('email.msgformat',[m.message, m.by])}
</#list>

</#list>
</#macro>

<#list m.groups.keySet() as key>
	<@tasklist tasks=m.groups[key] header=key/>
</#list>


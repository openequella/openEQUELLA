${m.header}

<#list m.notifications as t>
${b.key('email.itemname')} ${t.itemName}
${b.key('email.taskname')} ${t.taskName}
${b.key('email.tasklink')} ${t.taskLink.href}
<#if t.dueDate??>
${b.key('email.taskdue')} ${t.dueDate()?date?string.medium} <#if t.autoAction??>${t.autoAction}</#if>
</#if>
<#if t.causeLabel??>
${t.causeLabel} ${t.causeTask}
</#if>
<#list t.messages as msg>
${msg.label} ${b.key('email.msgformat',[msg.message, msg.by])}
</#list>

</#list>


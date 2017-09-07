${m.header}

<#list m.notifications as t>
${b.key('email.itemname')} ${t.itemName}
${b.key('email.owner')} ${t.owner}

<#if t.hasCauseInfo>
<#if t.causeLabel??>
${t.causeLabel} ${t.causeTask}
</#if>
<#list t.messages as msg>
${msg.label} ${b.key('email.msgformat',[msg.message, msg.by])}
<#if msg.hasFiles>
${msg.fileLabel} <#list msg.files as f>${f}
</#list>
</#if>
</#list>

</#if>
${b.key('email.currenttask')} ${t.taskName}
${b.key('email.tasklink')} ${t.taskLink.href}
<#if t.dueDate??>
${b.key('email.taskdue')} ${t.dueDate()?date?string.medium} <#if t.autoAction??>${t.autoAction}</#if>
</#if>

</#list>
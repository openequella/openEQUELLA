${m.header}

<#list m.notifications as t>
${b.key('email.itemname')} ${t.itemName}
${b.key('email.owner')} ${t.owner}
${b.key('email.taskname')} ${t.taskName}
<#if t.dueDate??>
${b.key('email.taskdue')} ${t.dueDate()?date?string.medium} <#if t.autoAction??>${t.autoAction}</#if>
</#if>
${b.key('email.tasklink')} ${t.taskLink.href}

</#list>
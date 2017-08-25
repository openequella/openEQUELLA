${m.header}

<#list m.notifications as t>
${b.key('email.itemname')} ${t.itemName}
${b.key('email.taskname')} ${t.taskName}
${b.key('email.tasklink')} ${t.taskLink.href}
 
</#list>

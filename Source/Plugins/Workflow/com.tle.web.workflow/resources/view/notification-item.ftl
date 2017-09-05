${m.header}

<#list m.notifications as i>
${b.key('email.itemname')} ${i.itemName}
${b.key('email.itemlink')} ${i.link.href}

</#list>

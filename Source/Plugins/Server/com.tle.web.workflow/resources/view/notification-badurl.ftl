${m.header}

<#list m.notifications as i>
${b.key('email.itemname')} ${i.itemName}
${b.key('email.itemlink')} ${i.link.href}
<#list i.urls as u>
${b.key('email.badurl')} ${u}
</#list>

</#list>

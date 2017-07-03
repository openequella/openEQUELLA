${m.header}

<#list m.notifications as i>
${b.key('email.itemname')} ${i.itemName}
${b.key('email.itemlink')} ${i.link.href}
<#if i.reassignMessage??>
${b.key('email.msg.r')} ${i.reassignMessage}
</#if>
${b.key('email.modhistory')} ${i.link.href}?is.summaryId=hc
 
</#list>

${m.header}

<#list m.notifications as i>
${b.key('email.itemname')} ${i.itemName}
${b.key('email.itemlink')} ${i.link.href}
${b.key('email.rejectedby')} ${i.rejectedBy}
${b.key('email.rejecttask')} ${i.rejectedTask}
${b.key('email.msg.r')} ${i.rejectedMessage}
${b.key('email.modhistory')} ${i.link.href}?is.summaryId=hc
 
</#list>
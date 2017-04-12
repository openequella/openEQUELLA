${m.header}

<#if m.emptyLabel??>
${m.emptyLabel}
<#else>
<#list m.notifications as i>
${b.key('email.purchaseupdate.itemname')} ${i.itemName}
${b.key('email.purchaseupdate.itemlink')} ${i.link.href}

</#list>
</#if>

${m.header}

<#if m.emptyLabel??>
${m.emptyLabel}
<#else>
<#list m.notifications as i>
${b.key('email.itemsale.itemname')} ${i.itemName}
${b.key('email.itemsale.itemlink')} ${i.link.href}
<#list i.saleItems as s>
${b.key('email.itemsale.sale')} ${s.front}, ${s.type}, ${s.price}
</#list>

</#list>

${b.key('email.itemsale.currencybycurrency.preamble')}
<#list m.currencySummary as currency>
	${currency}
</#list>
</#if>


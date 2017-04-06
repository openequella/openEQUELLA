<#ftl strip_whitespace=true />
<#-- as of original draft, there's only one 'taskName' per invocation of this template. -->
<#-- From the key we get a list of Notification beans (1 per user) -->

${m.header}

<#if m.emptyLabel??>
${m.emptyLabel}

<#else>
	<#list m.byUserNotifications as usrordrs><#t/>
${b.key('email.approvalandpayment.orderedby.title')} ${usrordrs.nameLabel}
${b.key('email.approvalandpayment.numberoforders.' + m.taskName)} ${usrordrs.numberOfOrders}
${b.key('email.approvalandpayment.numberofitems.title')} ${usrordrs.numberOfItems}
${b.key('email.approvalandpayment.summary.title')} ${usrordrs.summaryOfOrders}
		
	</#list>
	
${b.key('email.approvalandpayment.currencybycurrency.preamble')}
<#list m.currencySummary as currency>
	${currency}
</#list>
</#if>


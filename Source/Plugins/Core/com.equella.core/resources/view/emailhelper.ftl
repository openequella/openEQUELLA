<#macro row label><#t>
<tr><td style="font-weight:bold; padding-right: 10px; vertical-align: top;">${label}</td><td><#nested></td></tr>
</#macro>

<#macro stdHeader>
<html>
<p>${m.hello}</p>
<p>${m.reason}</p>
</#macro>

<#macro stdFooter>
</html>
</#macro>

<#macro itemLink item>
<a href="${item.link.href}">${item.itemName}</a>
</#macro>

<#macro taskLink n>
<tr><td><a href="${n.taskLink.href}">${b.key('email.tasklink')}</a></td></tr>
</#macro>

<#macro table>
<table style="margin-bottom:10px;">
<#nested>
</table>
</#macro>

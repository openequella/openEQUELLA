<#include "emailhelper.ftl">
<@stdHeader/>

<#list m.notifications as i>
<@table>
    <tr><td colspan="2">
    <@itemLink i/>
    </td></tr>
    <@row b.key('email.badurl')>
    <#list i.urls as u>
    <a href="${u}">${u}</a><br>
    </#list>
    </@row>
</@table>
</#list>
<@stdFooter/>

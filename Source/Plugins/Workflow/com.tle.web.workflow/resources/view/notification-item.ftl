<#include "emailhelper.ftl">
<@stdHeader/>

<p>
<#list m.notifications as i>
<@itemLink i/>
</#list>
</p>
<@stdFooter/>


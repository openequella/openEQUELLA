<#macro filedrop section style="" id="">
    <#local renderer=_choose(section, "filedrop")>
    <#if renderer?has_content>
        <@_render section=renderer style=style class="filedrop"  draggable="true" id=id><#nested></@_render>
    </#if>
</#macro>

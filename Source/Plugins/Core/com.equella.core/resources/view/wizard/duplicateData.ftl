<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="dupedata.css" />

<div class="area">
	<h2>${b.key('duplicatedatasection.pagename')}</h2>
  <#if m.textfieldDupIncluded>
    <h4>${b.key("duplicatedatasection.preamble")}</h4>
    <#if m.canAcceptAny>
      <p>${b.key("duplicatedatasection.checktheboxes")}</p>
    </#if>
    <#if m.mustChangeAny>
      <p>${b.key("duplicatedatasection.mustchange")}</p>
    </#if>
    <#list m.duplicateData as duplicateData>
      <#if !duplicateData.checkAttachmentDup>
        <#if duplicateData.visible>
          <div class="input checkbox">
            <label>
              <#if duplicateData.canAccept>
                <@render s.getCheckbox(_info, duplicateData.identifier)/>
              <#else>
                <span class="mandatory">${b.key("duplicatedatasection.mustchangesymbol")}</span>
              </#if>
              <span>${b.key("duplicatedatasection.usedby", [duplicateData.value])}</span>
            </label>
            <ul class="blue">
              <#list duplicateData.items as item>
                <li><@render item.link/></li>
              </#list>
            </ul>
          </div>
        </#if>
      </#if>
    </#list>
  </#if>

  <#if m.attachmentDupIncluded>
    <br/>
    <h4>${b.key("duplicatedatasection.preamble1")}</h4>
    <#list m.duplicateData as duplicateData>
      <#if duplicateData.checkAttachmentDup>
        <label>${b.key("duplicatedatasection.usedby", [duplicateData.value])}</label>
        <ul class="blue">
          <#list duplicateData.items as item>
            <li><@render item.link/></li>
          </#list>
        </ul>
        </#if>
    </#list>
  </#if>

</div>

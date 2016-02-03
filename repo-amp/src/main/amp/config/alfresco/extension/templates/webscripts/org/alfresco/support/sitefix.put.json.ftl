<#escape x as jsonUtils.encodeJSONString(x)>
{
"title": "Alfresco Support - Fix Broken Sites with Custom Role Web Script",
"summary":"${msgSitesSummary!''}",
"sitesFixed":[
 <#list lstFixedSites as fixedSite>
   "${fixedSite}"<#if fixedSite_has_next>,</#if>
 </#list>
],
"sitesAlreadyFixed":[
 <#list lstSitesAlreadyFixed as alreadyFixedSite>
   "${alreadyFixedSite}"<#if alreadyFixedSite_has_next>,</#if>
 </#list>
]
}
</#escape>
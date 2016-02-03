<#escape x as jsonUtils.encodeJSONString(x)>
{
"title": "Alfresco Support - Fix Site Roles In Wrong Zone Web Script",
"summary":"${msgGroupsSummary}",
"groupsFixed":[
 <#list lstFixedGroups as fixedGroup>
   "${fixedGroup}"<#if fixedGroup_has_next>,</#if>
 </#list>
],
"groupsAlreadyFixed":[
 <#list lstAlreadyFixedGroups as alreadyFixedGroup>
   "${alreadyFixedGroup}"<#if alreadyFixedGroup_has_next>,</#if>
 </#list>
]
"groupsToCreate":[
 <#list lstGroupsNeedCreation as needCreationGroup>
   "${needCreationGroup}"<#if needCreationGroup_has_next>,</#if>
 </#list>
]
}
</#escape>
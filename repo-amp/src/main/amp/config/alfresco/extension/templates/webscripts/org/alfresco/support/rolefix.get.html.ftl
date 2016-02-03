<html>
<head>
<title>Alfresco Support - List Sites Without Custom Role Web Script</title>
</head>
<body>
<p>
${msgGroupsSummary}
</p>
<p>
<b>Groups needing to be fixed(Fix by running the rolefix web script):</b><br>
 <#list lstGroupsToFix as groupToBeFixed>
   ${groupToBeFixed}<br>
 </#list>
</p>
<p>
<b>Groups that do not need to be fixed(No further action required):</b><br>
 <#list lstAlreadyFixedGroups as groupAlreadyFixed>
   ${groupAlreadyFixed}<br>
 </#list>
</p>
<p>
<b>Groups that still need to be created in their respective site(Run the sitefix web script):</b><br>
 <#list lstGroupsNeedCreation as groupNeedCreation>
   ${groupNeedCreation}<br>
 </#list>
</p>
</body>
</html>

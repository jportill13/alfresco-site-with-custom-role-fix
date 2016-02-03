<html>
<head>
<title>Alfresco Support - List Sites Without Custom Role Web Script</title>
</head>
<body>
<p>
${msgSitesSummary}
</p>
<p>
<b>Sites needing to be fixed(Fix by running the sitefix web script):</b><br>
 <#list lstNeedFixSites as toBeFixedSite>
   ${toBeFixedSite}<br>
 </#list>
</p>
<p>
<b>Sites that do not need to be fixed(No further action required):</b><br>
 <#list lstSitesAlreadyFixed as alreadyFixedSite>
   ${alreadyFixedSite}<br>
 </#list>
</p>
</body>
</html>

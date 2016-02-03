INTRO
========
This project contains a collection of web scripts that fixes share sites that are missing a custom share role group in them. This happens when a custom share role is introduced to a repository with existing sites
(see https://issues.alfresco.com/jira/browse/MNT-2456 & https://issues.alfresco.com/jira/browse/MNT-6919).
This project was created since the fix from the JIRA above does not work with alfresco 5 because groups are being created in the wrong zone and permissions are not being set properly in the existing sites.
These web scripts creates/moves the groups in the correct share zone.

WEB SCRIPTS DESCRIPTION
========================

There are two main web scripts in this project:

a) sitefix - This web script fixes repositories which have recently introduced a new share role and no attempt to fix existing sites has been done
b) rolefix - This web script fixes repositories where there has been an attempt to fix share roles with the script "fixsiteauthorities.zip" from https://issues.alfresco.com/jira/browse/MNT-2456

PROJECT FILES
---------------

The files for this web script are as follow:

* Web script java controller 
repo-amp/src/main/java/org/alfresco/support/repo/web/scripts/sitefix/FixSitesWithoutCustomShareRoleWebScriptGet.java
repo-amp/src/main/java/org/alfresco/support/repo/web/scripts/sitefix/FixSitesWithoutCustomShareRoleWebScriptPut.java
repo-amp/src/main/java/org/alfresco/support/repo/web/scripts/sitefix/FixSiteRolesInWrongZoneWebScriptGet.java
repo-amp/src/main/java/org/alfresco/support/repo/web/scripts/sitefix/FixSiteRolesInWrongZoneWebScriptPut.java

* web script files, descriptor and presentation
repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/sitefix.get.desc.xml
repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/sitefix.get.html.ftl

repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/sitefix.put.desc.xml
repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/sitefix.putt.json.ftl

repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/rolefix.get.desc.xml
repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/rolefix.get.html.ftl

repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/rolefix.put.desc.xml
repo-amp/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/support/rolefix.put.json.ftl


* Web script context file
repo-amp/src/main/amp/config/alfresco/module/repo-amp/context/webscript-context.xml

USAGE
=======

FIRST TIME FIX
---------------
The first set of web scripts are aimed for environments where it is the first time custom share roles are exposed to the system and no other attempt to fix the sites has been done, ie,
users are not able to accept an invite/not able to view an existing site/not able to view an existing site's document library after they are assigned the new share role.

To invoke the web script, as an administrator user, use the following url:

http://localhost:8080/alfresco/service/support/sitefix?customRoleName={CustomShareRoleName}

where {CustomShareRoleName} is the custom share role you want to fix.

This url supports the http methods GET and PUT. 
 - GET retrieves a list of sites that need to be fixed and the ones already fixed
 - PUT fixes the share sites that need to be fixed with the custom role provided, sample PUT calls are listed below
 
 curl -u admin:admin -v -X PUT "http://localhost:8080/alfresco/service/support/sitefix?customRoleName=SiteCustomManager" -H "Content-Type: application/json" -d "{}"                                             
 curl -v -X PUT "http://localhost:8080/alfresco/service/support/sitefix?customRoleName=SiteCustomManager&alf_ticket=TICKET_005d33ed8d3fb1b6a042c3870ff6db1aa8580221" -H "Content-Type: application/json" -d "{}"

NOTE: This web script can only be run as an alfresco administrator!

FIX EXISTING ROLE GROUPS
-------------------------
In the scenario where an administrator ran the the old web script(https://issues.alfresco.com/jira/secure/attachment/54733/fixsiteauthorities.zip) to fix broken sites as described in this JIRA
https://issues.alfresco.com/jira/browse/MNT-2456, users will experience issues with existing sites(public moderated), such issues include:

* Site content dashlet not loading
* Site Document Library not loading

Error in log reported is

Failed to execute script 'classpath*:alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/doclist.get.js': 01030023 Access Denied.  You do not have the appropriate permissions to perform this operation.

The reason why the javascript web script fails to fix the sites is because the javascript call to create the groups is creating the groups in the ZONE_APP_DEFAULT zone instead of the required
ZONE_APP_SHARE zone and it's also not setting the permissions for the custom share roles in the site and site's library. For this case this project includes two web scripts to help move the existing role groups in 
the correct zone and fixes the permissions. 

To invoke the web script, as an adminstrator user, use the following url:

http://localhost:8080/alfresco/service/support/rolefix?customRoleName={CustomShareRoleName}

where {CustomShareRoleName} is the custom share role you want to fix.

This url supports the http methods GET and PUT. 
 - GET retrieves a list of groups that need to be fixed, groups already fixed and groups that need to be created
 - PUT fixes the groups that need to be fixed that belong to the custom role provided, sample PUT calls are listed below
 
curl -u admin:admin -v -X PUT "http://localhost:8080/alfresco/service/support/rolefix?customRoleName=SiteCustomManager" -H "Content-Type: application/json" -d "{}"
curl -v -X PUT "http://localhost:8080/alfresco/service/support/rolefix?customRoleName=SiteCustomManager&alf_ticket=TICKET_005d33ed8d3fb1b6a042c3870ff6db1aa8580221" -H "Content-Type: application/json" -d "{}" 


DEBUG
======

To turn on debug logging add the following to your log4j.properties file

log4j.logger.org.alfresco.support.repo.web.scripts.sitefix=debug

EXAMPLE
========
To replicate the issue described in the JIRA and in the WIKI (https://wiki.alfresco.com/wiki/Custom_Permissions_in_Share#How_to_fix_broken_existing_sites) a sample custom share role is 
included for testing purposes. To test execute the following steps:

1) run the project as it is without changes
2) Login to share as the admin user
3) Create a site (or multiple sites)
4) Create test users
5) Shut down alfresco
6) Uncomment the bean that loads the custom role in the file

repo/src/main/resources/alfresco/extension/fixsitedemo-permission-context.xml

This will expose the role SiteCustomManager

		<permissionGroup name="SiteCustomManager"
			allowFullControl="false" expose="true">
			<includePermissionGroup permissionGroup="Coordinator"
				type="cm:cmobject" />
		</permissionGroup>
		
7) Re-run the project
8) Upon startup execute the custom web script to fix the existing sites by executing the following curl command:

curl -u admin:admin -v -X PUT "http://localhost:8080/alfresco/service/support/sitefix?customRoleName=SiteCustomManager" -H "Content-Type: application/json" -d "{}"

NOTE: Can also check the list of sites that need to be fixed prior fixing them by entering the following url in a web browser

http://localhost:8080/alfresco/service/support/sitefix?customRoleName=SiteCustomManager

Only administrators can run both web scripts!!!

9) Login to alfresco share as admin 
10) Invite a previously created user to an existing site with the custom role
11) Login to share as the invited user and verify the user can navigate the site without issue, look at the site dashboard, site dashlets and document library to make sure everything works as expected.


 

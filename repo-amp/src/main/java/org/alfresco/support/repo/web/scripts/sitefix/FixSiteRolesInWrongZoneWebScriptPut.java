/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.alfresco.support.repo.web.scripts.sitefix;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Java controller for the web script which fixes all site groups which are not in the share zone.
 *
 * @author jose.portillo@alfresco.com
 */
public class FixSiteRolesInWrongZoneWebScriptPut extends DeclarativeWebScript {
	
	private static Log logger = LogFactory.getLog(FixSiteRolesInWrongZoneWebScriptPut.class);
	
	private ServiceRegistry serviceRegistry;
	private AuthorityService authorityService;
	private SiteService siteService;
	private PermissionService permissionService;
	private NodeService nodeService;
	private static String PARAM_CUSTOM_ROLE_NAME = "customRoleName";
	
	//Define the zones where the new custom group for the custom role will be created.
    private static Set<String> SHARE_DEFAULT_ZONE_SET = new HashSet<String>();
    static
    {
    	SHARE_DEFAULT_ZONE_SET.add(AuthorityService.ZONE_APP_SHARE);
    }
    
    private static Set<String> APP_DEFAULT_ZONE_SET = new HashSet<String>();
    static
    {
    	APP_DEFAULT_ZONE_SET.add(AuthorityService.ZONE_APP_DEFAULT);
    }


	protected Map<String, Object> executeImpl(
            WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<String, Object>();
        
        logger.info("Starting fix site groups in wrong zone web script");
        SiteInfo nextSite = null;
        String nextSiteShortName = null;
        String customRoleGroupName = null;
        String actualName = null;
        List<String> fixedGroups = new ArrayList<String>();
        List<String> alreadyFixedGroups = new ArrayList<String>();
        List<String> groupsNeedToBeCreated = new ArrayList<String>();
        Set<String> zonesFromGroup = null;
        NodeRef nextSiteNodeRef = null;
        NodeRef docLibraryNodeRef = null;
        
        //TODO: Improvement - Check if there's a way to verify if the customRoleName exists in the repository as a valid role
        String customRoleName = req.getParameter(PARAM_CUSTOM_ROLE_NAME);
        logger.debug("customRoleName = " + customRoleName);
        
        //Check if the custom role name is not null or empty
        if(customRoleName == null || customRoleName.isEmpty())
        {
        	logger.info("Ending site groups in wrong zone web script - No custom role provided, nothing was fixed.");
        	model.put("msgGroupsSummary", "A custom role name must be provided. Nothing was fixed.");
            model.put("lstFixedGroups", fixedGroups);
            model.put("lstAlreadyFixedGroups", alreadyFixedGroups);
            model.put("lstGroupsNeedCreation", groupsNeedToBeCreated);
        	return model;
        }
        
        //Check if the custom role name contains the { character, if that's the case the user needs to enter a valid custom role name.
        if(customRoleName.contains("{"))
        {
        	logger.info("Ending site groups in wrong zone web script - Custom role name has invalid characters, nothing was fixed");
        	model.put("msgGroupsSummary", "A custom role name must not contain the character \'{\'. Nothing was fixed.");
            model.put("lstFixedGroups", fixedGroups);
            model.put("lstAlreadyFixedGroups", alreadyFixedGroups);
            model.put("lstGroupsNeedCreation", groupsNeedToBeCreated);
        	return model;
        }
        
        //Get a list of all sites in the repository
        List<SiteInfo> allSites = this.siteService.listSites(null,null,0);
        logger.debug("allSites = " + allSites);
        logger.info("Processing " + allSites.size() + " sites found in the repository...");
        
        //TODO: Improvement - break down the processing in different batches for repositories with a large number of sites.
        //Traverse all sites
        for(int nextSiteIndex = 0; nextSiteIndex < allSites.size(); nextSiteIndex++)
        {
        	//Get the current site's shortname
        	nextSite = allSites.get(nextSiteIndex);
        	logger.debug("nextSite = " + nextSite);
        	
        	nextSiteShortName = nextSite.getShortName();
        	logger.debug("nextSiteShortName = " + nextSiteShortName);
        	
        	nextSiteNodeRef = nextSite.getNodeRef();
        	logger.debug("nextSiteNodeRef = " + nextSiteNodeRef);
        	
        	//Build the custom role group name and parent group name for the current site
            customRoleGroupName = "site_" + nextSiteShortName + "_" + customRoleName;
            logger.debug("customRoleGroupName = " + customRoleGroupName);
            
            //Attempt to get the custom role group name
            actualName = this.authorityService.getName(AuthorityType.GROUP, customRoleGroupName);
            logger.debug("actualName = " + actualName);
            
            //Create custom role group in parent group if it doesn't exist
            if (authorityService.authorityExists(actualName) == true)
            {
            	//Retrieve the list of zones for the current group
            	zonesFromGroup = authorityService.getAuthorityZones(actualName);
            	logger.debug("zonesFromGroup = " + zonesFromGroup);
            	
            	//If the group belongs to the APP_DEFAULT zone then remove it from that zone
            	if(zonesFromGroup.contains(AuthorityService.ZONE_APP_DEFAULT))
            	{
            		authorityService.removeAuthorityFromZones(actualName, APP_DEFAULT_ZONE_SET);
            		logger.debug("Removed the group " + actualName + " from the default zone.");
            	}
            	
            	//If the group does not belong to the Share Zone then add it
            	if(!zonesFromGroup.contains(AuthorityService.ZONE_APP_SHARE))
            	{
            		authorityService.addAuthorityToZones(actualName, SHARE_DEFAULT_ZONE_SET);
            		fixedGroups.add(actualName);
            		logger.debug("Added the group " + actualName + " to the Share zone.");
            	}
            	else
            	{
            		alreadyFixedGroups.add(actualName);
            		logger.debug("The group " + actualName + " was already in the share zone.");
            	}
            	
                //Set permissions for the site object
                this.permissionService.setPermission(nextSiteNodeRef, actualName, customRoleName, true);
                logger.debug("Permissions wer set on the site object");
                
                //Get document library object
                docLibraryNodeRef = nodeService.getChildByName(nextSiteNodeRef, ContentModel.ASSOC_CONTAINS, "documentLibrary");
                logger.debug("Got document library object " + docLibraryNodeRef);
                
                //Set permission for the document library object
                if(docLibraryNodeRef != null)
                {
	                permissionService.setPermission(docLibraryNodeRef, actualName, customRoleName, true);
	                logger.debug("Added permissions to the site's document library");
                }
                else
                {
	                logger.debug("The site " + nextSiteShortName + " does not have a document library folder.");

                }
            }
            else
            {
            	//Add group to the groups to create list
            	logger.debug("Group " + actualName + " does not yet exist, please create this group.");
            	groupsNeedToBeCreated.add(actualName);
            }
        }
        
        logger.info("There were " + groupsNeedToBeCreated.size() + " groups that need to be created.");
        logger.info("There were " + alreadyFixedGroups.size() + " groups already fixed.");
        logger.info("There were " + fixedGroups.size() + " groups fixed in this process.");

        model.put("msgGroupsSummary", "There were " + fixedGroups.size() + " groups fixed, " + alreadyFixedGroups.size() + " groups that did not need fixing and " + groupsNeedToBeCreated.size() + " groups that need to be created in their respective sites.");
        model.put("lstFixedGroups", fixedGroups);
        model.put("lstAlreadyFixedGroups", alreadyFixedGroups);
        model.put("lstGroupsNeedCreation", groupsNeedToBeCreated);

        
        logger.info("Ending site groups in wrong zone web script successfully");

       
        return model;
    }

	
	
    public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
        this.authorityService = this.serviceRegistry.getAuthorityService();
        this.siteService = this.serviceRegistry.getSiteService();
        this.permissionService = this.serviceRegistry.getPermissionService();
        this.nodeService = this.serviceRegistry.getNodeService();
	}
}
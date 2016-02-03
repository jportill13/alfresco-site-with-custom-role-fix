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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * A Java controller for the web script which lists all sites that need to have
 * the custom share role group created and all sites that already have the group in them.
 *
 * @author jose.portillo@alfresco.com
 */
public class FixSitesWithoutCustomShareRoleWebScriptGet extends DeclarativeWebScript {
	
	private static Log logger = LogFactory.getLog(FixSitesWithoutCustomShareRoleWebScriptGet.class);
	
	private ServiceRegistry serviceRegistry;
	private AuthorityService authorityService;
	private SiteService siteService;
	private static String PARAM_CUSTOM_ROLE_NAME = "customRoleName";
	

	protected Map<String, Object> executeImpl(
            WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<String, Object>();
        
        logger.info("Starting list sites without custom share role web script.");
        SiteInfo nextSite = null;
        String nextSiteShortName = null;
        String customRoleGroupName = null;
        String parentGroupName = null;
        String actualName = null;
        List<String> needFixingSites = new ArrayList<String>();
        List<String> alreadyFixedSites = new ArrayList<String>();
        
        //TODO: Improvement - Check if there's a way to verify if the customRoleName exists in the repository as a valid role
        String customRoleName = req.getParameter(PARAM_CUSTOM_ROLE_NAME);
        
        logger.debug("customRoleName = " + customRoleName);
        
        //Check if the custom role name is not null or empty
        if(customRoleName == null || customRoleName.isEmpty())
        {
        	logger.info("Ending list sites without custom share role web script. - No custom role provided, nothing to report.");
        	model.put("msgSitesSummary", "A custom role name must be provided. Nothing to report.");
            model.put("lstNeedFixSites", needFixingSites);
            model.put("lstSitesAlreadyFixed", alreadyFixedSites);
        	return model;
        }
        
        //Check if the custom role name contains the { character, if that's the case the user needs to enter a valid custom role name.
        if(customRoleName.contains("{"))
        {
        	logger.info("Ending list sites without custom share role web script. - Custom role name is invalid, nothing was fixed");
        	model.put("msgSitesSummary", "A custom role name must not contain the character \'{\'. Nothing was fixed.");
            model.put("lstNeedFixSites", needFixingSites);
            model.put("lstSitesAlreadyFixed", alreadyFixedSites);
        	return model;
        }
        
        //Get a list of all sites in the repository
        List<SiteInfo> allSites = this.siteService.listSites(null,null,0);
        
        logger.debug("allSites = " + allSites);
        logger.info("Processing " + allSites.size() + " sites found in the repository...");
        
        //Traverse all sites
        for(int nextSiteIndex = 0; nextSiteIndex < allSites.size(); nextSiteIndex++)
        {
        	//Get the current site's shortname
        	nextSite = allSites.get(nextSiteIndex);
        	logger.debug("nextSite = " + nextSite);
        	nextSiteShortName = nextSite.getShortName();
        	logger.debug("nextSiteShortName = " + nextSiteShortName);
        	
        	//Build the custom role group name and parent group name for the current site
            customRoleGroupName = "site_" + nextSiteShortName + "_" + customRoleName;
            logger.debug("customRoleGroupName = " + customRoleGroupName);
            parentGroupName = "GROUP_site_" + nextSiteShortName;
            logger.debug("parentGroupName = " + parentGroupName);
            
            //Attempt to get the custom role group name
            actualName = this.authorityService.getName(AuthorityType.GROUP, customRoleGroupName);
            logger.debug("actualName = " + actualName);
            
            //Add the site to the list of sites that need fixing if the group doesn't exist
            if (authorityService.authorityExists(actualName) == false)
            {
                //Add site to needFixingSites list
                logger.debug("Fixed site: " + nextSiteShortName);
                needFixingSites.add(nextSiteShortName);
            }
            else
            {
            	//Add site to alreadyFixedSites list since the group already exists
            	logger.debug("Site: " + nextSiteShortName + " does not need to be fixed.");
            	alreadyFixedSites.add(nextSiteShortName);
            }
        }
        
        logger.info("There were " + alreadyFixedSites.size() + " sites already fixed.");
        logger.info("There were " + needFixingSites.size() + " sites that need to be fixed.");

        model.put("msgSitesSummary", "There are " + needFixingSites.size() + " sites that need to be fixed and " + alreadyFixedSites.size() + " sites that do not need fixing.");
        model.put("lstNeedFixSites", needFixingSites);
        model.put("lstSitesAlreadyFixed", alreadyFixedSites);

        
        logger.info("Ending list sites without custom share role web script.");

       
        return model;
    }

	
	
    public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
        this.authorityService = this.serviceRegistry.getAuthorityService();
        this.siteService = this.serviceRegistry.getSiteService();
	}
}
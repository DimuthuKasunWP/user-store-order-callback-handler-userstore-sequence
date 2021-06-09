/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.identity.custom.callback.userstore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="custom.callback.userstore.service.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="identity.application.management.component"
 * interface="org.wso2.carbon.identity.application.mgt.ApplicationManagementService"
 * cardinality="1..1" policy="dynamic" bind="setApplicationManagementService"
 * unbind="unsetApplicationManagementService"
 **/
public class CustomCallbackUserstoreServiceComponent {
    private static Log log = LogFactory.getLog(CustomCallbackUserstoreServiceComponent.class);

    public static final String REG_PATH = "userstore-metadata.xml";
    public static final String REG_PROPERTY_SP_TRAVELOCITY = "travelocity.com";
    public static final String REG_PROPERTY_USER_DOMAINS_TRAVELOCITY = "PRIMARY,USERSTORE-MYSQL-2";
    public static final String REG_PROPERTY_SP_PICKUP_MANAGER = "pickup-manager";
    public static final String REG_PROPERTY_USER_DOMAINS_PICKUP_MANAGER = "USERSTORE-MYSQL-1";


    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("CustomCallbackUserstoreServiceComponent bundle is initializing");
        }

        try {
            createUserStoreMetadataResource();
            log.info("CustomCallbackUserstoreServiceComponent bundle is activated.");
        } catch (Exception e) {
            log.error("Error while activating CustomCallbackUserstoreServiceComponent bundle", e);
        }
    }

    private void createUserStoreMetadataResource() {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();

        try {
            UserRealm realm =
                    CustomCallbackUserstoreServiceComponentHolder.getInstance().getRealmService().getTenantUserRealm(-1234);

            //Logged in user is not authorized to create the permission.
            // Temporarily change the user to the admin for creating the permission
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                    realm.getRealmConfiguration().getAdminUserName());


            Registry registry = (Registry) CarbonContext.getThreadLocalCarbonContext().getRegistry(
                    RegistryType.USER_CONFIGURATION);

            if (!registry.resourceExists(REG_PATH)) {
                if (log.isDebugEnabled()) {
                    log.debug("Userstore metadata registry resource not exists in the path: " + REG_PATH);
                }

                Resource metadata = new ResourceImpl();
                metadata.setProperty(REG_PROPERTY_SP_TRAVELOCITY, REG_PROPERTY_USER_DOMAINS_TRAVELOCITY);
                metadata.setProperty(REG_PROPERTY_SP_PICKUP_MANAGER, REG_PROPERTY_USER_DOMAINS_PICKUP_MANAGER);

                registry.put(REG_PATH, metadata);

                if (log.isDebugEnabled()) {
                    log.debug("Userstore metadata registry resource created succesfully in path: " + REG_PATH +
                            "with properties, " + REG_PROPERTY_SP_TRAVELOCITY + ": " + REG_PROPERTY_USER_DOMAINS_TRAVELOCITY);
                    log.debug("Userstore metadata registry resource created succesfully in path: " + REG_PATH +
                            "with properties, " + REG_PROPERTY_SP_PICKUP_MANAGER + ": " + REG_PROPERTY_USER_DOMAINS_PICKUP_MANAGER);
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Userstore metadata registry resource exists in the path: " + REG_PATH);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error while setting authorization.", e);
        } catch (RegistryException e) {
            log.error("Error while creating registry resource:" + REG_PATH, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while loading tenant user realm.", e);
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
        }
    }

    protected void deactivate(ComponentContext context) {
        log.info("CustomCallbackUserstoreServiceComponent bundle is deactivated");
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in CustomCallbackUserstoreServiceComponent bundle");
        }
        CustomCallbackUserstoreServiceComponentHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in CustomCallbackUserstoreServiceComponent bundle");
        }
        CustomCallbackUserstoreServiceComponentHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Realm Service set in CustomCallbackUserstoreServiceComponent bundle");
        }
        CustomCallbackUserstoreServiceComponentHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Realm Service unset in CustomCallbackUserstoreServiceComponent bundle");
        }
        CustomCallbackUserstoreServiceComponentHolder.getInstance().setRealmService(null);
    }

    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Application Management Service set in CustomCallbackUserstoreServiceComponent bundle");
        }
        CustomCallbackUserstoreServiceComponentHolder.getInstance().setApplicationManagementService(applicationManagementService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Application Management Service unset in CustomCallbackUserstoreServiceComponent bundle");
        }
        CustomCallbackUserstoreServiceComponentHolder.getInstance().setApplicationManagementService(null);
    }
}

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

package org.wso2.carbon.identity.custom.callback.userstore;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.custom.callback.userstore.internal.CustomCallbackUserstoreServiceComponent;
import org.wso2.carbon.identity.custom.callback.userstore.internal.CustomCallbackUserstoreServiceComponentHolder;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.UserStorePreferenceOrderSupplier;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class CustomUserStoreOrderCallbackHandler implements UserStorePreferenceOrderSupplier<List<String>> {

    private static final Log log = LogFactory.getLog(CustomUserStoreOrderCallbackHandler.class);
    private AuthenticationContext context;
    private ServiceProvider serviceProvider;
    private final String travelocity_label = "travelocity.com";
    private final String pickup_manager_label = "pickup-manager";
    private final String current_domains = "PRIMARY,USERSTORE-MYSQL-1,USERSTORE-MYSQL-2";

    public CustomUserStoreOrderCallbackHandler(AuthenticationContext context, ServiceProvider serviceProvider) {

        this.context = context;
        this.serviceProvider = serviceProvider;
    }

    public CustomUserStoreOrderCallbackHandler() {

    }

    public List<String> get() throws UserStoreException {

        List<String> userStoreOrder;
        if (log.isDebugEnabled()) {
            log.debug("SimpleUserStoreOrderCallbackHandler invoked");
        }
        if (context != null) {
            userStoreOrder = buildOrderBySP(context.getServiceProviderName());
        } else {
            userStoreOrder = buildOrderBySP(serviceProvider.getApplicationName());
        }
        return userStoreOrder;
    }

    private List<String> buildOrderBySP(String spName) {

        List<String> defaultUserStoreDomainList = getUserStoreDomainList();
        List<String> userStoreOrder = excludeUserStoresForDefaultServiceProviders(spName, defaultUserStoreDomainList);

        if (log.isDebugEnabled()) {
            log.info("UserStore domains selected for the service provider: " + spName + " are: " + userStoreOrder);
        }
        return userStoreOrder;
    }

    private List<String> excludeUserStoresForDefaultServiceProviders(String spName, List<String> domainNames) {

        List<String> userStoreOrder = new ArrayList<String>();

        String specialSP = getSpecialSP(spName);
        String specialUserStoreDomainNames = getSpecialUserStoreDomainNames(spName);
        String[] domains = specialUserStoreDomainNames.split(",");
        if (StringUtils.equals(spName, specialSP)) {
            for (String allowedDomain : domains) {
                userStoreOrder.add(allowedDomain);
            }
        } else {
            for (String domainName : domainNames) {
                if (!Arrays.asList(domains).contains(domainName)) {
                    userStoreOrder.add(domainName);
                }
            }
        }

        return userStoreOrder;
    }

    private List<String> getUserStoreDomainList() {

        List<String> domainNames = new ArrayList<String>();

        try {
            RealmService realmService = CustomCallbackUserstoreServiceComponentHolder.getInstance().getRealmService();
            UserStoreManager secondaryManager =
                    realmService.getTenantUserRealm(-1234).getUserStoreManager();
            RealmConfiguration secondaryConfig;

            while (true) {
                if (secondaryManager instanceof AbstractUserStoreManager) {
                    AbstractUserStoreManager abstractUserStoreManager = (AbstractUserStoreManager) secondaryManager;
                    secondaryConfig = abstractUserStoreManager.getRealmConfiguration();
                    String domainName =
                            secondaryConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    if (domainName != null && domainName.trim().length() > 0) {
                        domainNames.add(domainName.toUpperCase());
                    }
                    secondaryManager = abstractUserStoreManager.getSecondaryUserStoreManager();
                    if (secondaryManager == null) {
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (UserStoreException e) {
            log.error("Error while listing user store list", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while loading realm", e);
        }

        return domainNames;
    }

    protected String getSpecialUserStoreDomainNames(String sp) {

        switch (sp) {
            case pickup_manager_label:
                return CustomCallbackUserstoreServiceComponent.REG_PROPERTY_USER_DOMAINS_PICKUP_MANAGER;
            case travelocity_label:
                return CustomCallbackUserstoreServiceComponent.REG_PROPERTY_USER_DOMAINS_TRAVELOCITY;
            default:
                return current_domains;
        }
    }

    protected String getSpecialSP(String sp) {
        switch (sp) {
            case pickup_manager_label:
                return CustomCallbackUserstoreServiceComponent.REG_PROPERTY_SP_PICKUP_MANAGER;
            case travelocity_label:
                return CustomCallbackUserstoreServiceComponent.REG_PROPERTY_SP_TRAVELOCITY;
            default:
                return sp;
        }
    }

}

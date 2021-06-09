package org.wso2.carbon.identity.custom.callback.userstore;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.CallBackHandlerFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.config.UserStorePreferenceOrderSupplier;

import java.util.List;

public class CustomUserStoreOrderCallbackFactory extends CallBackHandlerFactory {

    @Override
    public UserStorePreferenceOrderSupplier<List<String>> createUserStorePreferenceOrderSupplier(AuthenticationContext
                                                                                                        context,
                                                                                                 ServiceProvider serviceProvider) {
        return new CustomUserStoreOrderCallbackHandler(context, serviceProvider);
    }
}

package com.challenge.ct.mbs.google.cloudmessage;

import com.challenge.ct.mbs.aws.SNSRegistryHelper;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Handles when the instance token refreshes
 */
public class MessageIdHandler extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();

        SNSRegistryHelper.registerApplication(getApplicationContext(), token);

        // todo: improve if productizing
        // A valid token is necessary for a backend service.
        // it is rare that the firebase refreshes its token but should consider
        // a method to send refreshed token for accessing/requesting firebase cloud messages.
    }
}
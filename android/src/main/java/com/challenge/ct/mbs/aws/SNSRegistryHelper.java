package com.challenge.ct.mbs.aws;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.challenge.ct.mbs.ContactApplication;
import com.challenge.ct.mbs.object.Credential;
import com.challenge.ct.mbs.utils.Const;

/**
 * SNS Registry allows a self-registration of AWS SNS endpoint and its subscription.
 * A AWS topic and an application should be configured on the cloud. They are the entry and exit,
 * respectively, to SNS service. For this application, database interfacing AWS Lambda generates the topic
 * and Firebase cloud messaging service, the application, will consume it.
 * 
 * The SNS process requires message forwarding chain is as Topic -> subscription -> endpoint -> application.
 * A creation of this chain is done in reverse order, ie. create an endpoint for an application
 * then create subscription for a topic and endpoint.
 */
public class SNSRegistryHelper {

    private Credential credential;
    private AmazonSNSClient client;

    private SharedPreferences pref;
    private String appToken;

    public static SNSRegistryHelper build(Context context) {
        return new SNSRegistryHelper(context);
    }

    /**
     * Registers Application for receiving cloud messages
     *
     * @param context Application context
     * @param token   A token generated and transmitted by the Firebase service
     */
    public static void registerApplication(Context context, String token) {
        SNSRegistryHelper registry = new SNSRegistryHelper(context);

        if (registry.isTokenExist(token)) {
            return;
        }

        // application identity changed, remove everything possible
        registry.unregisterSnsSubscription();

        registry.persistToken(token);
        registry.registerSnsSubscription();
    }

    /**
     * Unregisters cloud messages from transmitting
     *
     * Although this is possible, there will be residues left on the backend. It isn't recommended
     * to allow user's trigger registration of cloud message subscription.
     *
     * @param context appliation context
     */
    public static void unregisterApplication(Context context) {
        SNSRegistryHelper registry = new SNSRegistryHelper(context);

        registry.unregisterSnsSubscription();
    }

    private SNSRegistryHelper(Context context) {
        credential = ContactApplication.getCredential();
        client = new AmazonSNSAsyncClient(ContactApplication.getCognito().getProvider());

        pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        appToken = pref.getString(Const.PREF_APPLICATION_FB_TOKEN, null);
    }

    public boolean isTokenExist(String token) {
        return !TextUtils.isEmpty(appToken) && appToken.equals(token);
    }

    public void persistToken(@NonNull String token) {
        // persist token generated from the firebase
        // this will not likely to update unless when app gets reinstalled
        appToken = token;
        pref.edit().putString(Const.PREF_APPLICATION_AWS_SNS_ENDPOINT_ARN, token).apply();
    }

    public void registerSnsSubscription() {
        // Create endpoint that connects sns to firebase instance
        CreatePlatformEndpointRequest endpointRequest = new CreatePlatformEndpointRequest();
        endpointRequest.withPlatformApplicationArn(credential.getSnsApp()).withToken(appToken);

        CreatePlatformEndpointResult endpointResult;
        try {
            endpointResult = client.createPlatformEndpoint(endpointRequest);
        } catch (AmazonServiceException ex) {
            ex.printStackTrace();
            return;
        }

        String arnEndpoint = endpointResult.getEndpointArn();

        // Create subscription that connects sns topic to an endpoint
        SubscribeRequest subscribeRequest = new SubscribeRequest()
                .withEndpoint(arnEndpoint)
                .withTopicArn(credential.getSnsTopic())
                .withProtocol("Application");

        SubscribeResult subscribeResult = client.subscribe(subscribeRequest);
        String arnSubscription = subscribeResult.getSubscriptionArn();

        pref.edit()
                .putString(Const.PREF_APPLICATION_AWS_SNS_ENDPOINT_ARN, arnEndpoint)
                .putString(Const.PREF_APPLICATION_AWS_SNS_SUBSCRIPTION_ARN, arnSubscription)
                .apply();
    }

    public void unregisterSnsSubscription() {
        // todo: Find an appropriate method to remove endpoint and subscription from aws sns service
        // There's no good way to get un-installation event from android tp process this.
        // There's no way to remove aws sns subscription from aws sdk.

        String awsSnsEndpointArn = pref.getString(Const.PREF_APPLICATION_AWS_SNS_ENDPOINT_ARN, null);
        if (TextUtils.isEmpty(awsSnsEndpointArn)) {
            // not registered - first time launching the app or already been unregistered.
            return;
        }

        String endpoint = pref.getString(Const.PREF_APPLICATION_AWS_SNS_ENDPOINT_ARN, null);

        DeleteEndpointRequest request = new DeleteEndpointRequest();
        request.withEndpointArn(endpoint);
        client.deleteEndpoint(request);

        // this will also unregister application to receive firebase cloud message
        // clear shared preference
        pref.edit()
                .putString(Const.PREF_APPLICATION_AWS_SNS_ENDPOINT_ARN, null)
                .putString(Const.PREF_APPLICATION_AWS_SNS_SUBSCRIPTION_ARN, null)
                .apply();
    }
}

package com.ct.challenge.mbs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import org.apache.http.util.TextUtils;

import java.util.List;
import java.util.Map;

/**
 * An AWS Lambda function allows publishing an AWS SNS topic when a CRUD action occurs on DynamoDB table
 * This provides a link between DynamoDB to SNS Service.
 * <p>
 * method: max mem: 57MB - processing time: 5.82 ms
 * aws configuration: 128MB - 100 ms (timeout: 5 sec)
 */
@SuppressWarnings("unused")
public class LambdaHandler {

    @SuppressWarnings({"unchecked", "unused"})
    public String handler(Object input, Context context) {

        if (!((Map<String, Object>) input).containsKey("Records")) {
            return "Didn't find appropriate input data";
        }

        String type = "", table = "";

        List<Object> records = (List<Object>) ((Map<String, Object>) input).get("Records");
        for (Object record : records) {
            if (record instanceof Map) {
                for(Object key : ((Map) record).keySet()) {
                    if (key instanceof String) {
                        if (key.equals("eventName")) {
                            type = (String) ((Map) record).get(key);
                        } else if (key.equals("eventSourceARN")) {
                            table = ((Map) record).get(key).toString().contains("MBSContacts_User") ?
                                    "user" : "address";
                        }
                    }
                }
            }
        }

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(table)) {
            System.out.println("data incomplete");
            return "incomplete data";
        }

        Credential credential = Credential.load();

        // Using async client to minimize processing time
        AmazonSNSAsyncClient snsClient = new AmazonSNSAsyncClient(new CredentialProvider(credential));
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

        String msg = "Message,".concat("type:").concat(type).concat(",table:").concat(table);
        PublishRequest publishRequest = new PublishRequest(credential.getArn(), msg);

        PublishResult result = snsClient.publish(publishRequest);
        return "A Message Published: " + result.getMessageId();
    }

    /**
     * A Custom simple key provider
     * Provides necessary key values to publish sns message
     */
    private class CredentialProvider implements AWSCredentialsProvider {
        private final AWSCredentials credential;

        public CredentialProvider(Credential credential) {
            this.credential = new BasicAWSCredentials(credential.getAccess(), credential.getSecret());
        }

        @Override
        public AWSCredentials getCredentials() {
            return credential;
        }

        @Override
        public void refresh() {

        }
    }
}

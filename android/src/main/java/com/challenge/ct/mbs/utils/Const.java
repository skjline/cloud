package com.challenge.ct.mbs.utils;

public class Const {
    public static final int REQUEST_FRAGMENT_INSERT = 1;
    public static final int REQUEST_FRAGMENT_REMOVE = 2;
    public static final int REQUEST_USER_SIGN_IN = 3;

    // Google Api Authorization
    public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 1101;
    public static final int GOOGLE_SIGN_OUT_REQUEST_CODE = 1102;

    // Database Status
    public static final String INITIALIZE = "launch";
    public static final String CONNECTING = "connecting";
    public static final String CONNECTED = "active";
    public static final String DISCONNECT = "disconnect";
    public static final String UPDATE = "update";
    public static final String ERROR = "error";

    public static final String ERROR_INVALID_FRAGMENT = "Invalid fragment id";
    public static final String ERROR_INVALID_AWS_CRENDETIAL = "AWS credential is not properly set";

    // Fragment Transition Constants
    public static final String TRANSITION_FRAGMENT_ID = "fragment_id";
    public static final String TRANSITION_CONTACT_UID = "contact_uid";

    // DynamoDB Constants
    public static final String TABLE_NAME_USER = "MBSContacts_User";
    public static final String TABLE_NAME_ADDRESS = "MBSContact_Address";

    public static final String PREF_APPLICATION_FB_TOKEN = "firebase_app_token";
    public static final String PREF_APPLICATION_AWS_SNS_ENDPOINT_ARN = "aws_sns_endpoint_arn";
    public static final String PREF_APPLICATION_AWS_SNS_SUBSCRIPTION_ARN = "aws_sns_subscription_arn";

    public static class TABLE_USER_COLUMN {
        public static final String KEY = "uid";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String PHONE = "phone";
        public static final String EMAIL = "email";
        public static final String ADDRESS_ID = "addressId";
    }

    public static class TABLE_ADDRESS_COLUMN {
        public static final String KEY = "address_id";
        public static final String STREET = "street1";
        public static final String STREET2 = "street2";
        public static final String UNIT = "unit";
        public static final String COUNTY = "county";
        public static final String CITY = "city";
        public static final String STATE = "state";
        public static final String ZIP = "zip";
        public static final String COUNTRY = "country";
    }
}

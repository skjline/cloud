package com.challenge.ct.mbs.object;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * AWS IoT Credential information.
 * This should be instantiated by parsing a json file
 */
public class Credential {
    @SerializedName("pool_id")
    private String poolId;
    @SerializedName("regions")
    private String regions;

    @SerializedName("arnsnstopic")
    private String snstopic;
    @SerializedName("arnsnsapp")
    private String snsapp;

    public String getPoolId() {
        return poolId;
    }

    public String getRegion() {
        return regions;
    }

    public String getSnsTopic() {
        return snstopic;
    }

    public String getSnsApp() {
        return snsapp;
    }

    private static final String AWS_ACCESS_ASSETS = "aws-mobile.json";

    public static Credential build(Context context) {
        Credential credential;

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getApplicationContext()
                            .getAssets().open(AWS_ACCESS_ASSETS)));
            credential = new Gson().fromJson(reader, Credential.class);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            credential = null;
        }

        return credential;
    }
}

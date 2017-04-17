package com.ct.challenge.mbs;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;

@SuppressWarnings("unused")
public class Credential {
    private String arn;
    private String access;
    private String secret;

    public Credential() {
    }

    public String getArn() {
        return arn;
    }

    public String getAccess() {
        return access;
    }

    public String getSecret() {
        return secret;
    }

    public static Credential load() {
        try {
            return new Gson().fromJson(new FileReader("aws-lambda.json"), Credential.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}


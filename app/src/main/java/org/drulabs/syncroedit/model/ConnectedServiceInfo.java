package org.drulabs.syncroedit.model;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Authored by KaushalD on 9/27/2016.
 */

public class ConnectedServiceInfo implements Serializable {

    @User.SignInService
    private String serviceName;
    private boolean isConnected;
    private String serviceIdentifier;
    private String accessToken;
    private String accessSecret;
    private String secretKey;

    public ConnectedServiceInfo() {

    }

    public ConnectedServiceInfo(@User.SignInService String serviceName, boolean isConnected) {
        this.serviceName = serviceName;
        this.isConnected = isConnected;
    }

    @User.SignInService
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(@User.SignInService String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String toString() {
        String stringRep = (new Gson()).toJson(this);
        return stringRep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectedServiceInfo)) return false;

        ConnectedServiceInfo that = (ConnectedServiceInfo) o;

        return getServiceName().equals(that.getServiceName());

    }

    @Override
    public int hashCode() {
        return getServiceName().hashCode();
    }

    public static ConnectedServiceInfo fromJSON(@NonNull String jsonRep) {
        Gson gson = new Gson();
        ConnectedServiceInfo obj = gson.fromJson(jsonRep, ConnectedServiceInfo.class);
        return obj;
    }
}

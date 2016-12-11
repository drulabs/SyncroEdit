package org.drulabs.syncroedit.model;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authored by KaushalD on 9/26/2016.
 */

public class User implements Serializable {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({GOOGLE, FACEBOOK, GITHUB, TWITTER, EMAIL})
    public @interface SignInService {
    }

    public static final String GOOGLE = "google";
    public static final String FACEBOOK = "facebook";
    public static final String GITHUB = "github";
    public static final String TWITTER = "twitter";
    public static final String EMAIL = "email";

    @SignInService
    public static String getSignInService(String serviceName) {
        switch (serviceName) {
            case GOOGLE:
                return GOOGLE;
            case FACEBOOK:
                return FACEBOOK;
            case GITHUB:
                return GITHUB;
            case TWITTER:
                return TWITTER;
            case EMAIL:
                return EMAIL;
            default:
                throw new IllegalArgumentException("Invalid service type: " + serviceName);
        }
    }

    private String userId;
    private String name;
    private String photoUrl;
    private String signInServiceType;
    private String serviceProviderId;
    private String email;
    private String phone;
    private String fcmToken;
    private List<ConnectedServiceInfo> services;

    public User() {
        services = new ArrayList<>();
        services.add(new ConnectedServiceInfo(GOOGLE, false));
        services.add(new ConnectedServiceInfo(FACEBOOK, false));
        services.add(new ConnectedServiceInfo(TWITTER, false));
        services.add(new ConnectedServiceInfo(GITHUB, false));
    }

    private long localTimeStamp = System.currentTimeMillis();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @SignInService
    public String getSignInServiceType() {
        return signInServiceType;
    }

    public void setSignInServiceType(@SignInService String signInServiceType) {
        this.signInServiceType = signInServiceType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public long getLocalTimeStamp() {
        return localTimeStamp;
    }

    public void setLocalTimeStamp(long localTimeStamp) {
        this.localTimeStamp = localTimeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void addService(ConnectedServiceInfo serviceInfo) {
        if (!services.contains(serviceInfo)) {
            services.add(serviceInfo);
        } else {
            // This will remove the old service info (since only identifier and service name are
            // checked)
            services.remove(serviceInfo);
            // This will add the new service info with all new states
            services.add(serviceInfo);
        }
    }

    public void removeService(ConnectedServiceInfo serviceInfo) {
        services.remove(serviceInfo);
    }

    public List<ConnectedServiceInfo> getServices() {
        return services;
    }

    @Override
    public String toString() {
        String jsonRep = (new Gson()).toJson(this);
        return jsonRep;
    }

    public static User fromJSON(@NonNull String json) {
        Gson gson = new Gson();
        User user = gson.fromJson(json, User.class);
        return user;
    }

    public static Map<String, Object> getMapRep(User user) {
        Map<String, Object> mapRep = new HashMap<>();
        mapRep.put("userId", user.getUserId());
        mapRep.put("name", user.getName());
        mapRep.put("photoUrl", user.getPhotoUrl());
        mapRep.put("signInServiceType", user.getSignInServiceType());
        mapRep.put("serviceProviderId", user.getServiceProviderId());
        mapRep.put("email", user.getEmail());
        mapRep.put("phone", user.getPhone());
        mapRep.put("fcmToken", user.getFcmToken());

        //Set this information separately
        //ConnectedServiceInfo[] serviceArray = new ConnectedServiceInfo[user.getServices().size()];
        //serviceArray = user.getServices().toArray(serviceArray);
        //mapRep.put("services", serviceArray);

        return mapRep;
    }
}

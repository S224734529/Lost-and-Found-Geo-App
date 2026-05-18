package com.example.lostfoundgeoapp;

public class LostFoundItem {

    private int id;
    private String postType;
    private String name;
    private String phone;
    private String description;
    private String date;
    private String locationName;
    private double latitude;
    private double longitude;

    public LostFoundItem(
            int id,
            String postType,
            String name,
            String phone,
            String description,
            String date,
            String locationName,
            double latitude,
            double longitude
    ) {
        this.id = id;
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public String getPostType() {
        return postType;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
# Lost and Found Map Mobile App

## Overview

This Android application allows users to create lost or found item adverts with location details. A user can choose a location through Google Places Autocomplete or use the phone's current GPS location. Saved items are displayed on Google Maps. The app also supports radius-based searching, where only items within a selected distance from the user's current location are shown.

## Main Features

- Create lost or found item adverts
- Store item name, phone, description, date, and location
- Select location using Google Places Autocomplete
- Get current location using Android location services
- Save advert data in SQLite
- Display all saved lost and found items on Google Maps
- Search items within a user-defined radius in kilometres

## Technologies Used

- Java
- Android Studio
- SQLite
- Google Maps SDK for Android
- Google Places API
- Fused Location Provider API

## Setup Instructions

1. Open the project in Android Studio.
2. Create a Google Cloud project.
3. Enable these APIs:
   - Maps SDK for Android
   - Places API
4. Create an Android API key.
5. Replace `YOUR_GOOGLE_MAPS_API_KEY` in:
   - `AndroidManifest.xml`
   - `AddAdvertActivity.java`
6. Run the application on a real Android device or emulator with Google Play services.

## How the App Works

The home screen has two options. The user can create a new advert or view saved adverts on a map.

When creating an advert, the user chooses whether the post is a lost item or a found item. The user then enters contact and item details. The location can be entered by tapping the location field, which opens Google Places Autocomplete. The user can also tap the current location button to use the phone's GPS location.

The app stores each advert in a local SQLite database. Latitude and longitude are saved with the advert so the item can be shown on the map later.

The map screen reads all saved adverts from SQLite and places markers on Google Maps. The radius search allows the user to enter a distance in kilometres. The app compares each item's saved coordinates with the user's current location and only displays markers within that distance.

## Radius Search Logic

The app uses Android's `Location.distanceBetween()` method to calculate the distance between the user's current location and each lost or found item. The result is returned in metres and converted into kilometres. If the item is within the entered radius, it is shown on the map.

## Limitations

This version stores data locally on one device. It is suitable for assessment and prototype use. A commercial version should use a cloud database, secure authentication, moderation, image storage, and server-side location search.

## Commercial Extension

A commercial version should be structured as a cloud-backed mobile service. The Android app would remain the main user interface, but adverts would be stored in a central backend instead of SQLite. This would allow users in the same city or organisation to see the same lost and found records.

The backend should provide APIs for creating adverts, searching items, uploading images, reporting suspicious posts, and closing recovered cases. Location search should be handled on the server using geospatial queries. This would be more reliable than asking every phone to download all items and filter them locally.

The system should also include account management, admin review, and secure storage for phone numbers. In a real lost and found service, contact details must be protected because they can be misused. Users could contact each other through masked messages rather than seeing personal phone numbers immediately.

This structure would make the app useful for universities, shopping centres, transport hubs, and large workplaces where many items are lost and reported each week.

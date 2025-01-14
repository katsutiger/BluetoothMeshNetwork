//package com.example.androidtraining;
//
//import android.location.Location;
//import java.util.Locale;
//
//public class DistanceHelper {
//    public static String getDistanceString(Location location1, Location location2) {
//        float[] results = new float[3];
//        Location.distanceBetween(
//                location1.getLatitude(), location1.getLongitude(),
//                location2.getLatitude(), location2.getLongitude(),
//                results
//        );
//
//        float distance = results[0];
//        float bearing = results[1];
//
//        String direction = getDirectionString(bearing);
//        String distanceStr = formatDistance(distance);
//
//        return String.format("%s %s", distanceStr, direction);
//    }
//
//    private static String formatDistance(float distance) {
//        if (distance < 1000) {
//            return String.format(Locale.getDefault(), "%.0fm", distance);
//        } else {
//            return String.format(Locale.getDefault(), "%.1fkm", distance / 1000);
//        }
//    }
//
//    private static String getDirectionString(float bearing) {
//        String[] directions = {"北", "北東", "東", "南東", "南", "南西", "西", "北西"};
//        int index = Math.round(bearing / 45) % 8;
//        return directions[index];
//    }
//}
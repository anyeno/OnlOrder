package com.sky.utils;

import java.lang.Math;

public class DistanceCalculator {

    // 地球半径（单位：米）
    private static final double EARTH_RADIUS = 6371000; // 平均半径6371公里

    /**
     * 使用Haversine公式计算两点间距离
     * @param lat1 点1纬度（度）
     * @param lon1 点1经度（度）
     * @param lat2 点2纬度（度）
     * @param lon2 点2经度（度）
     * @return 距离（米）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 纬度差
        double latDiff = lat2Rad - lat1Rad;
        // 经度差
        double lonDiff = lon2Rad - lon1Rad;

        // Haversine公式
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离
        return EARTH_RADIUS * c;
    }
}
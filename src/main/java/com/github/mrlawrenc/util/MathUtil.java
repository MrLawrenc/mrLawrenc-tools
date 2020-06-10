package com.github.mrlawrenc.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 数学工具类
 *
 * @author MrLawrenc
 * date  2020/5/27 23:51
 */
public final class MathUtil {
    /**
     * 计算两点距离
     *
     * @param x  x
     * @param y  y
     * @param x1 x1
     * @param y1 y1
     * @return 距离
     */
    public static double calculateDistance(int x, int y, int x1, int y1) {
        double y2 = (y1 - y) * (y1 - y);
        double x2 = (x1 - x) * (x1 - x);
        double v = Math.sqrt(Math.pow(y2, 2) + Math.pow(x2, 2));
        return round(v, 4);
    }

    /**
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     * 提供精确的小数位四舍五入处理。
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }

        BigDecimal b = BigDecimal.valueOf(v);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * decimal求平方根
     *
     * @param value 源
     * @param scale 精度
     * @return 结果
     */
    public static BigDecimal sqrt(BigDecimal value, int scale) {
        BigDecimal num2 = BigDecimal.valueOf(2);
        int precision = 100;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal deviation = value;
        int cnt = 0;
        while (cnt < precision) {
            deviation = (deviation.add(value.divide(deviation, mc))).divide(num2, mc);
            cnt++;
        }
        deviation = deviation.setScale(scale, RoundingMode.HALF_UP);
        return deviation;
    }

    public static int binarySearchNearbyIndex(int[] a, int key) {
        int low, mid, high;
        low = 0;
        high = a.length - 1;
        while (low <= high) {
            mid = (high + low) / 2;
            if (key > a[mid]) {
                low = mid + 1;
            } else if (key < a[mid]) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return high < 0 || high > a.length - 1 ? -1 : high;
    }
}
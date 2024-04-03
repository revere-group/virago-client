package net.optifine.util;

import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils
{
    public static final float PI = (float)Math.PI;
    public static final float PI2 = ((float)Math.PI * 2F);
    public static final float PId2 = ((float)Math.PI / 2F);
    private static final float[] ASIN_TABLE = new float[65536];

    public static float asin(float value)
    {
        return ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & 65535];
    }

    public static float acos(float value)
    {
        return ((float)Math.PI / 2F) - ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & 65535];
    }

    public static double min(final double a, final double b) {
        if (a > b) {
            return b;
        }
        if (a < b) {
            return a;
        }
        /* if either arg is NaN, return NaN */
        if (a != b) {
            return Double.NaN;
        }
        /* min(+0.0,-0.0) == -0.0 */
        /* 0x8000000000000000L == Double.doubleToRawLongBits(-0.0d) */
        long bits = Double.doubleToRawLongBits(a);
        if (bits == 0x8000000000000000L) {
            return a;
        }
        return b;
    }

    public static int getAverage(int[] vals)
    {
        if (vals.length <= 0)
        {
            return 0;
        }
        else
        {
            int i = getSum(vals);
            int j = i / vals.length;
            return j;
        }
    }

    public static int getSum(int[] vals)
    {
        if (vals.length <= 0)
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (int j = 0; j < vals.length; ++j)
            {
                int k = vals[j];
                i += k;
            }

            return i;
        }
    }

    public static int roundDownToPowerOfTwo(int val)
    {
        int i = MathHelper.roundUpToPowerOfTwo(val);
        return val == i ? i : i / 2;
    }

    public static double getDistance(double srcX, double srcZ, double dstX, double dstZ) {
        double xDiff = dstX - srcX;
        double zDiff = dstZ - srcZ;
        return MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
    }

    public static boolean equalsDelta(float f1, float f2, float delta)
    {
        return Math.abs(f1 - f2) <= delta;
    }

    public static float toDeg(float angle)
    {
        return angle * 180.0F / MathHelper.PI;
    }

    public static float toRad(float angle)
    {
        return angle / 180.0F * MathHelper.PI;
    }

    public static float roundToFloat(double d)
    {
        return (float)((double)Math.round(d * 1.0E8D) / 1.0E8D);
    }

    static
    {
        for (int i = 0; i < 65536; ++i)
        {
            ASIN_TABLE[i] = (float)Math.asin((double)i / 32767.5D - 1.0D);
        }

        for (int j = -1; j < 2; ++j)
        {
            ASIN_TABLE[(int)(((double)j + 1.0D) * 32767.5D) & 65535] = (float)Math.asin((double)j);
        }
    }

    public static double round(double num, double increment) {
        if (increment < 0) {
            throw new IllegalArgumentException();
        }

        return new BigDecimal(num).setScale((int) increment, RoundingMode.HALF_UP).doubleValue();
    }

    public static double randomNumber(double max, double min) {
        return (Math.random() * (max - min)) + min;
    }

    public static double clamp(double min, double max, double n) {
        return Math.max(min, Math.min(max, n));
    }
}

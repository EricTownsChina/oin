package priv.eric.oin.common.utils;

/**
 * Desc:
 *
 * @author EricTownsChina@outlook.com
 * create 2022/5/2 10:03
 */
public class TimeUtil {

    private TimeUtil() {}

    public static void main(String[] args) {
        System.out.println(secondToMillis(900000000000000L));
    }

    public static long secondToMillis(Long second) {
        if (null == second) {
            return 0L;
        }
        int length = String.valueOf(second).length();
        int i = 13 - length;
        if (i == 0) {
            return second;
        } else {
            return (long) (second * (Math.pow(10, i)));
        }
    }
}

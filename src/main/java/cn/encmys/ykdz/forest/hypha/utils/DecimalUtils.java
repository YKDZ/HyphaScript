package cn.encmys.ykdz.forest.hypha.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalUtils {
    public static boolean isEquals(BigDecimal a, BigDecimal b) {
        return a.compareTo(b.setScale(a.scale(), RoundingMode.HALF_UP)) == 0;
    }

    /**
     * 解析数字字符串为对应的数字对象
     *
     * @param input 数字字符串
     * @return 对应的数字对象
     */
    public static Object parseNumericString(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        input = input.trim();
        char lastChar = input.charAt(input.length() - 1);

        try {
            switch (lastChar) {
                case 'd':
                case 'D':
                    return Double.parseDouble(input.substring(0, input.length() - 1));
                case 'f':
                case 'F':
                    return Float.parseFloat(input.substring(0, input.length() - 1));
                case 'l':
                case 'L':
                    return Long.parseLong(input.substring(0, input.length() - 1));
                default:
                    if (input.contains(".") || input.toLowerCase().contains("e")) {
                        return Double.parseDouble(input);
                    } else {
                        return Integer.parseInt(input);
                    }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric format: " + input, e);
        }
    }
}

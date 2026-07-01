// src/common/utils/StringFormatter.java
package common.utils;

/**
 * 字符串格式化工具类
 * 用于处理特殊字符的转义和还原
 */
public class StringFormatter {

    /**
     * 将字符串中的特殊字符转义为可见的转义序列（用于显示）
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    public static String escape(String str) {
        if (str == null) {
            return "null";
        }
        return str.replace("\\", "\\\\")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\"", "\\\"");
    }

    /**
     * 将转义序列还原为实际的特殊字符（用于存储和处理）
     *
     * @param escapedStr 转义后的字符串
     * @return 还原后的原始字符串
     */
    public static String unescape(String escapedStr) {
        if (escapedStr == null) {
            return null;
        }
        // 注意：替换顺序很重要，先处理 \\ 避免重复替换
        return escapedStr.replace("\\\\", "\u0000")  // 临时占位
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t")
                  .replace("\\\"", "\"")
                  .replace("\u0000", "\\");  // 恢复反斜杠
    }

    /**
     * 截断字符串并添加省略号
     *
     * @param str 原始字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 转义并截断字符串（用于日志输出）
     *
     * @param str 原始字符串
     * @param maxLength 最大长度
     * @return 转义并截断后的字符串
     */
    public static String escapeAndTruncate(String str, int maxLength) {
        String escaped = escape(str);
        return truncate(escaped, maxLength);
    }

    /**
     * 安全地获取字符串（null 转为空字符串）
     *
     * @param str 原始字符串
     * @return 非空字符串
     */
    public static String safeString(String str) {
        return str != null ? str : "";
    }

    /**
     * 格式化多行文本为单行显示（用于日志）
     *
     * @param text 多行文本
     * @return 单行显示的文本
     */
    public static String toSingleLine(String text) {
        if (text == null) {
            return "null";
        }
        return text.replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}

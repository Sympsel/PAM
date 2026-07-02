package common.utils;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Java代码行数统计工具
 * 支持统计：总代码行、注释行、空白行、有效代码行
 *
 * @author CodeAnalyzer
 * @version 1.0
 */
public class CodeLineCounter {

    // 统计结果类
    public static class Statistics {
        private String fileName;
        private int totalLines;      // 总行数
        private int codeLines;       // 有效代码行
        private int commentLines;    // 注释行
        private int blankLines;      // 空白行

        public Statistics(String fileName) {
            this.fileName = fileName;
        }

        public void addLine(LineType type) {
            totalLines++;
            switch (type) {
                case CODE: codeLines++; break;
                case COMMENT: commentLines++; break;
                case BLANK: blankLines++; break;
            }
        }

        public String getFileName() { return fileName; }
        public int getTotalLines() { return totalLines; }
        public int getCodeLines() { return codeLines; }
        public int getCommentLines() { return commentLines; }
        public int getBlankLines() { return blankLines; }

        @Override
        public String toString() {
            return String.format("%-40s | %5d | %5d | %5d | %5d",
                    fileName, totalLines, codeLines, commentLines, blankLines);
        }
    }

    enum LineType { CODE, COMMENT, BLANK }

    /**
     * 分析单个Java文件的行数统计
     */
    public static Statistics analyzeFile(Path filePath) throws IOException {
        Statistics stats = new Statistics(filePath.toString());
        List<String> lines = Files.readAllLines(filePath);

        boolean inBlockComment = false;

        for (String line : lines) {
            String trimmed = line.trim();

            // 空白行
            if (trimmed.isEmpty()) {
                stats.addLine(LineType.BLANK);
                continue;
            }

            // 处理多行注释 /* ... */
            if (inBlockComment) {
                stats.addLine(LineType.COMMENT);
                if (trimmed.endsWith("*/") || trimmed.contains("*/")) {
                    inBlockComment = false;
                }
                continue;
            }

            // 单行注释 //
            if (trimmed.startsWith("//")) {
                stats.addLine(LineType.COMMENT);
                continue;
            }

            // 多行注释开始 /*
            if (trimmed.startsWith("/*")) {
                stats.addLine(LineType.COMMENT);
                if (!trimmed.endsWith("*/") && !trimmed.contains("*/")) {
                    inBlockComment = true;
                }
                continue;
            }

            // 混合注释：代码后面跟 // 注释
            if (trimmed.contains("//")) {
                // 检查 // 是否在字符串中
                int commentIndex = findCommentOutsideString(trimmed);
                if (commentIndex != -1) {
                    String codePart = trimmed.substring(0, commentIndex).trim();
                    if (codePart.isEmpty()) {
                        stats.addLine(LineType.COMMENT);
                    } else {
                        stats.addLine(LineType.CODE); // 算作代码行（简化处理）
                    }
                    continue;
                }
            }

            // 默认是代码行
            stats.addLine(LineType.CODE);
        }

        return stats;
    }

    /**
     * 查找不在字符串内的注释标记位置
     */
    private static int findCommentOutsideString(String line) {
        boolean inString = false;
        boolean escape = false;
        for (int i = 0; i < line.length() - 1; i++) {
            char c = line.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"' && !inString) {
                inString = true;
            } else if (c == '"' && inString) {
                inString = false;
            }
            if (!inString && c == '/' && line.charAt(i + 1) == '/') {
                return i;
            }
        }
        return -1;
    }

    /**
     * 递归扫描目录统计所有Java文件
     */
    public static List<Statistics> analyzeDirectory(Path dirPath) throws IOException {
        List<Statistics> results = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            results.add(analyzeFile(p));
                        } catch (IOException e) {
                            System.err.println("无法读取文件: " + p);
                        }
                    });
        }

        return results;
    }

    /**
     * 打印统计报告
     */
    public static void printReport(List<Statistics> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Java 代码行数统计报告");
        System.out.println("=".repeat(80));
        System.out.printf("%-40s | %5s | %5s | %5s | %5s\n",
                "文件名", "总行", "代码", "注释", "空白");
        System.out.println("-".repeat(80));

        int totalTotal = 0, totalCode = 0, totalComment = 0, totalBlank = 0;

        for (Statistics s : results) {
            System.out.println(s);
            totalTotal += s.getTotalLines();
            totalCode += s.getCodeLines();
            totalComment += s.getCommentLines();
            totalBlank += s.getBlankLines();
        }

        System.out.println("-".repeat(80));
        System.out.printf("%-40s | %5d | %5d | %5d | %5d\n",
                "总计", totalTotal, totalCode, totalComment, totalBlank);
        System.out.println("=".repeat(80));

        // 计算比例
        if (totalTotal > 0) {
            System.out.printf("\n代码占比: %.1f%%\n", (totalCode * 100.0 / totalTotal));
            System.out.printf("注释占比: %.1f%%\n", (totalComment * 100.0 / totalTotal));
            System.out.printf("空白占比: %.1f%%\n", (totalBlank * 100.0 / totalTotal));
        }
    }

    public static void main(String[] args) {
        // 直接指定要统计的目录路径
        String dirPath = "/mnt/d/shared/Code/javaCode/PAM/src";

        Path path = Paths.get(dirPath);

        // 验证路径是否存在且是目录
        if (!Files.exists(path)) {
            System.err.println("错误: 路径不存在 - " + dirPath);
            System.exit(1);
        }
        if (!Files.isDirectory(path)) {
            System.err.println("错误: 不是目录 - " + dirPath);
            System.exit(1);
        }

        try {
            List<Statistics> results = analyzeDirectory(path);

            if (results.isEmpty()) {
                System.out.println("未找到Java文件。");
            } else {
                printReport(results);
            }

        } catch (IOException e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
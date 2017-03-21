package net.yangziwen.hqlformatter.service;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.util.MultiMap;

import net.yangziwen.hqlformatter.util.FileUtils;
import net.yangziwen.hqlformatter.util.FileUtils.LineHandler;

public class ColorCheckService {

    private static final Pattern COLOR_PATTERN = Pattern.compile("#(?:[0-9a-fA-F]{3}){1,2}");



    public static Map<String, String> collectColorVariables(File... files) {
        CollectColorVariableHandler handler = new CollectColorVariableHandler();
        return FileUtils.handleFiles(Arrays.asList(files), handler);
    }

    public static MultiMap<String> transformToColorValueMap(Map<String, String> colorVariableMap) {
        MultiMap<String> colorValueMap = new MultiMap<String>();
        for (Entry<String, String> entry : colorVariableMap.entrySet()) {
            colorValueMap.add(entry.getValue(), entry.getKey());
        }
        return colorValueMap;
    }

    public static Map<File, List<Report>> checkLessFiles(File rootDir, File... variableFiles) {
        Map<String, String> colorVariableMap = collectColorVariables(variableFiles);
        MultiMap<String> colorValueMap = transformToColorValueMap(colorVariableMap);
        CheckLessColorHandler handler = new CheckLessColorHandler(colorValueMap);
        final List<File> fileList = Arrays.asList(variableFiles);
        return FileUtils.handleFileTree(rootDir, new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isHidden()) {
                    return false;
                }
                if (file.isDirectory()) {
                    return true;
                }
                if (!file.getName().endsWith(".less")) {
                    return false;
                }
                if (fileList.contains(file)) {
                    return false;
                }
                return true;
            }
        }, handler);

    }

    private static String normalizeColor(String color) {
        if (color == null) {
            return null;
        }
        if (!COLOR_PATTERN.matcher(color).matches()) {
            throw new IllegalArgumentException(String.format("color [%s] is illegal!", color));
        }
        if (color.length() == 7) {
            return color.toUpperCase();
        }
        StringBuilder buff = new StringBuilder("#");
        for (int i = 1; i <= 3; i++) {
            for (int j = 0; j < 2; j++) {
                buff.append(color.charAt(i));
            }
        }
        return buff.toString().toUpperCase();
    }

    static class CheckLessColorHandler implements LineHandler<Map<File, List<Report>>> {

        private Map<File, List<Report>> reportMap = new LinkedHashMap<File, List<Report>>();

        private MultiMap<String> colorValueMap = new MultiMap<String>();

        public CheckLessColorHandler(MultiMap<String> colorValueMap) {
            this.colorValueMap = colorValueMap;
        }

        @Override
        public void handle(File file, int lineNumber, String line) {
            if (!reportMap.containsKey(file)) {
                reportMap.put(file, new ArrayList<Report>());
            }
            String color = normalizeColor(findColor(line));
            if (color == null) {
                return;
            }
            Report report = new Report(file, lineNumber, line, colorValueMap.get(color));
            reportMap.get(file).add(report);
        }

        @Override
        public Map<File, List<Report>> getResult() {
            return reportMap;
        }

    }

    private static class CollectColorVariableHandler implements LineHandler<Map<String, String>> {
        private Map<String, String> colorVariableMap = new LinkedHashMap<String, String>();
        @Override
        public void handle(File file, int lineNumber, String line) {
            String[] array = line.split(":");
            if (array.length < 2) {
                return;
            }
            String name = array[0].trim();
            String value = array[1].trim();
            if (colorVariableMap.containsKey(value)) {
                colorVariableMap.put(name, colorVariableMap.get(value));
            }
            String color = findColor(value);
            if (color == null) {
                return;
            }
            colorVariableMap.put(name, normalizeColor(color));
        }
        @Override
        public Map<String, String> getResult() {
            return colorVariableMap;
        }
    }

    private static String findColor(String str) {
        Matcher matcher = COLOR_PATTERN.matcher(str);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    static class Report {

        private File file;
        private int lineNumber;
        private String line;
        private List<String> suggests;

        public Report(File file, int lineNumber, String line, List<String> suggests) {
            this.file = file;
            this.lineNumber = lineNumber;
            this.line = line;
            this.suggests = suggests;
        }

        public File getFile() {
            return file;
        }
        public int getLineNumber() {
            return lineNumber;
        }
        public String getLine() {
            return line;
        }
        public List<String> getSuggests() {
            return suggests;
        }

        @Override
        public String toString() {
            return "line: " + getLineNumber()
                + "\ncontent: " + getLine().trim()
                + "\nsuggest: " + getSuggests()
                + "\n-------------------------";
        }

    }

    public static void main(String[] args) {
        File variableFile = null;
        File rootDir = null;
        Map<File, List<Report>> reportMap = checkLessFiles(rootDir, variableFile);
        for (Entry<File, List<Report>> entry : reportMap.entrySet()) {
            if (entry.getValue() == null || entry.getValue().size() == 0) {
                continue;
            }
            System.out.println(entry.getKey());
            for (Report report : entry.getValue()) {
                System.out.println(report);
            }
        }
    }

}

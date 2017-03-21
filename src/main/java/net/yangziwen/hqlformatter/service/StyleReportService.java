package net.yangziwen.hqlformatter.service;

import static net.yangziwen.hqlformatter.util.DataSourceFactory.getDataSource;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.util.MultiMap;

import net.yangziwen.hqlformatter.model.StyleReport;
import net.yangziwen.hqlformatter.repository.StyleReportRepo;
import net.yangziwen.hqlformatter.util.FileUtils;
import net.yangziwen.hqlformatter.util.FileUtils.LineHandler;

public class StyleReportService {

    private static final Pattern COLOR_PATTERN = Pattern.compile("#(?:[0-9a-fA-F]{3}){1,2}");

    private static StyleReportRepo styleReportRepo = new StyleReportRepo(getDataSource());

    public static List<StyleReport> getStyleReportList(int offset, int limit , Map<String, Object> params) {
        return styleReportRepo.list(offset, limit, params);
    }

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

    public static Map<File, List<StyleReport>> checkLessFiles(File rootDir, File... variableFiles) {
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

    static class CheckLessColorHandler implements LineHandler<Map<File, List<StyleReport>>> {

        private Map<File, List<StyleReport>> reportMap = new LinkedHashMap<File, List<StyleReport>>();

        private MultiMap<String> colorValueMap = new MultiMap<String>();

        public CheckLessColorHandler(MultiMap<String> colorValueMap) {
            this.colorValueMap = colorValueMap;
        }

        @Override
        public void handle(File file, int lineNumber, String line) {
            if (!reportMap.containsKey(file)) {
                reportMap.put(file, new ArrayList<StyleReport>());
            }
            String color = normalizeColor(findColor(line));
            if (color == null) {
                return;
            }
            StyleReport report = new StyleReport();
            report.setFilePath(file.getAbsolutePath().replaceAll("\\\\", "/"));
            report.setFileName(file.getName());
            report.setLineNumber(lineNumber);
            report.setLineContent(line.trim());
            report.setCapture(color);
            report.setSuggest(colorValueMap.getString(color));
            reportMap.get(file).add(report);
        }

        @Override
        public Map<File, List<StyleReport>> getResult() {
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

    public static void main(String[] args) {
        // update style_report set file_path =  substr(file_path, 28, length(file_path));
        File variableFile = null;
        File rootDir = null;
        Map<File, List<StyleReport>> reportMap = checkLessFiles(rootDir, variableFile);
        for (Entry<File, List<StyleReport>> entry : reportMap.entrySet()) {
            for (StyleReport report : entry.getValue()) {
                styleReportRepo.insert(report);
            }
        }
    }

}

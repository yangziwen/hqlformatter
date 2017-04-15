package net.yangziwen.hqlformatter.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.yangziwen.hqlformatter.util.StringUtils;
import net.yangziwen.hqlformatter.util.Utils;

public class RequestMappingAnalyzer {

	private static final Logger logger = LoggerFactory.getLogger(RequestMappingAnalyzer.class);

	private static final Pattern PACKAGE_PATTENR = Pattern.compile("package (.+);");

	private static final Pattern CLASS_PATTERN = Pattern.compile("\\s+class\\s+([A-Z][\\w\\d_]*)\\s*(?:extends .*?)?(?:implements .*?)?\\{[\\w\\W]*\\}\\s*$");

	private static final Pattern REQUEST_MAPPING_PATTERN = Pattern.compile("@RequestMapping\\(.*?(?:value\\s*=\\s*)?\"([^\"]*?)\"[^\\)]*?\\)");

	private static final Pattern METHOD_PATTERN = Pattern.compile("public\\s+([\\w\\d_<>, \\?]*)\\s+(\\w[\\w\\d_]*)\\s*\\(([^\\{]*)\\)\\s*(?:throws .*?)?\\{");

	private static final Pattern ACL_PATTERN = Pattern.compile("@Acl\\(\\{([\\w\\W]+?)\\}\\)");

	private static final Pattern ACL_ITEM_ROLE_PATTERN = Pattern.compile("roles\\s*=\\s*\\{([^\\}]+?)\\}");

	private static final Pattern ACL_ITEM_TARGET_PATTERN = Pattern.compile("convert\\s*=\\s*\".+?\\((.+?)\\)\"");

	public static List<Result> analyze(File file) {
		List<Result> resultList = new ArrayList<Result>();
		analyze0(file, resultList);
		return resultList;
	}

	private static void analyze0(File file, List<Result> resultList) {
		Project project = null;
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return !name.startsWith(".") && !(dir.isDirectory() && name.equals("target"));
				}
			})) {
				if (f.getName().equalsIgnoreCase("pom.xml")) {
					project = Project.fromPom(f);
				} else {
					analyze0(f, resultList);
				}
			}
		}
		if (project != null) {
			for (Result result : resultList) {
				if (result.getProject() == null) {
					result.setProject(project);
				}
			}
		}
		if (!file.getName().endsWith(".java")) {
			return;
		}
		resultList.addAll(doAnalyze(file));
	}

	private static List<Result> doAnalyze(File file) {
		List<Result> resultList = new ArrayList<Result>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuilder buff = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				buff.append(line).append("\n");
			}
			String content = buff.toString();
			if (!content.contains("@Controller") && !content.contains("@RestController")) {
				return Collections.emptyList();
			}

			Matcher packageMatcher = PACKAGE_PATTENR.matcher(content);
			if (!packageMatcher.find()) {
				logger.error("failed to parse package of file[{}]", file.getAbsolutePath());
				return Collections.emptyList();
			}
			String packageName = packageMatcher.group(1);

			Matcher classMatcher = CLASS_PATTERN.matcher(content);
			if (!classMatcher.find()) {
				logger.error("failed to parse class of file[{}]", file.getAbsolutePath());
				return Collections.emptyList();
			}
			String className = classMatcher.group(1);

			Matcher requestMappingMatcher = REQUEST_MAPPING_PATTERN.matcher(content);
			String baseUrl = "";
			int lastMethodEnd = 0;
			while (requestMappingMatcher.find()) {
				if (requestMappingMatcher.start() < classMatcher.start()) {
					baseUrl = requestMappingMatcher.group(1);
					continue;
				}
				Matcher methodMatcher = METHOD_PATTERN.matcher(content);
				if (methodMatcher.find(requestMappingMatcher.end())) {
					Result result = new Result();
					result.setRequestUrl(baseUrl + requestMappingMatcher.group(1));
					result.setClassName(packageName + "." + className);
					result.setReturnType(methodMatcher.group(1));
					result.setMethodName(methodMatcher.group(2));
					result.setAuthorities(parseAuthorities(content, lastMethodEnd, methodMatcher.start()));
					resultList.add(result);
					lastMethodEnd = methodMatcher.end();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utils.closeQuietly(reader);
		}
		return resultList;
	}

	private static List<String> parseAuthorities(String content, int start, int end) {
	    Matcher aclMatcher = ACL_PATTERN.matcher(content);
	    if (!aclMatcher.find(start)) {
	        return Collections.emptyList();
	    }
	    List<String> authorities = new ArrayList<String>();
	    String aclItems = aclMatcher.group(1);
	    for (String item : aclItems.split("@AclItem")) {
	        if (StringUtils.isBlank(item)) {
	            continue;
	        }
	        Matcher roleMatcher = ACL_ITEM_ROLE_PATTERN.matcher(item);
	        Matcher targetMatcher = ACL_ITEM_TARGET_PATTERN.matcher(item);
	        if(roleMatcher.find() && targetMatcher.find()) {
	            String role = roleMatcher.group(1).replaceAll("\"", "").toLowerCase();
	            String target = targetMatcher.group(1);
	            authorities.add(target + ":" + role);
	        }
	    }
	    return authorities;
	}

	public static class Project {

		private String groupId;

		private String artifactId;

		private String version;

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public void setArtifactId(String artifactId) {
			this.artifactId = artifactId;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		private static Project fromPom(File pom) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(pom);
				Project project = new Project();
				Element projectEle = doc.getDocumentElement();
				Element parentEle = null;
				NodeList list = projectEle.getChildNodes();
				for (int i = 0, l = list.getLength(); i < l; i++) {
					Node node = list.item(i);
					if ("groupId".equals(node.getNodeName())) {
						project.setGroupId(node.getTextContent());
					}
					else if ("artifactId".equals(node.getNodeName())) {
						project.setArtifactId(node.getTextContent());
					}
					else if ("version".equals(node.getNodeName())) {
						project.setVersion(node.getTextContent());
					}
					else if ("parent".equals(node.getNodeName())) {
						parentEle = (Element) node;
					}
				}
				if (StringUtils.isBlank(project.getGroupId()) && parentEle != null) {
					NodeList pl = parentEle.getElementsByTagName("groupId");
					if (pl.getLength() > 0) {
						project.setGroupId(pl.item(0).getTextContent());
					}
				}
				return project;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

	}

	public static class Result {

		/**
		 * 项目信息
		 */
		private Project project;

		/**
		 * 请求url
		 */
		private String requestUrl;

		/**
		 * 类名(包括包名)
		 */
		private String className;

		/**
		 * 方法名
		 */
		private String methodName;

		/**
		 * 权限注解
		 */
		private List<String> authorities;

		/**
		 * 返回值
		 */
		private String returnType;

		public Project getProject() {
			return project;
		}

		public void setProject(Project project) {
			this.project = project;
		}

		public String getRequestUrl() {
			return requestUrl;
		}

		public void setRequestUrl(String requestUrl) {
			this.requestUrl = requestUrl;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public List<String> getAuthorities() {
            return authorities;
        }

        public void setAuthorities(List<String> authorities) {
            this.authorities = authorities;
        }

        public String getReturnType() {
			return returnType;
		}

		public void setReturnType(String returnType) {
			this.returnType = returnType;
		}

	}

}

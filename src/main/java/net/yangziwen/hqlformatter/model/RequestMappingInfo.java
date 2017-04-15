package net.yangziwen.hqlformatter.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import net.yangziwen.hqlformatter.analyze.RequestMappingAnalyzer;
import net.yangziwen.hqlformatter.util.StringUtils;

@Table(name = "request_mapping_info")
public class RequestMappingInfo {

	@Id
	@Column
	private Long id;

	@Column
	private String requestUrl;

	@Column
	private String project;

	@Column
	private String className;

	@Column
	private String methodName;

	@Column
	private String authorities;

	@Column
	private String returnType;

	public RequestMappingInfo() {

	}

	public RequestMappingInfo(RequestMappingAnalyzer.Result result) {
		this.requestUrl = result.getRequestUrl();
		this.project = result.getProject().getArtifactId();
		this.className = result.getClassName();
		this.methodName = result.getMethodName();
		this.authorities = StringUtils.join(result.getAuthorities(), ",");
		this.returnType = result.getReturnType();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
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

	public String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

}

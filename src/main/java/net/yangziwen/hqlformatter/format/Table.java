package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;

public interface Table {
	
	public String table();
	
	public String alias();
	
	public int start();
	
	public int end();
	
	public StringWriter format(String indent, int nestedDepth, StringWriter writer);
	
}

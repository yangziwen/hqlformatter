package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;

public interface Table<T extends Table<T>> {
	
	public String table();
	
	public String alias();
	
	public T alias(String alias);
	
	public int start();
	
	public T start(int startPos);
	
	public int end();
	
	public T end(int endPos);
	
//	public String comment();
	
	public StringWriter format(String indent, int nestedDepth, StringWriter writer);
	
}

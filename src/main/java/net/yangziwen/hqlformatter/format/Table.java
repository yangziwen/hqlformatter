package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;

public interface Table<T extends Table<T>> {
	
	String table();
	
	String alias();
	
	T alias(String alias);
	
	int start();
	
	T start(int startPos);
	
	int end();
	
	T end(int endPos);
	
	Comment headComment();
	
	T headComment(Comment comment);
	
	Comment tailComment();
	
	T tailComment(Comment comment);
	
	StringWriter format(String indent, int nestedDepth, StringWriter writer);
	
}

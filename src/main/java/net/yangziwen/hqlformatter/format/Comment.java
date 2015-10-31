package net.yangziwen.hqlformatter.format;

public class Comment {
	
	private String conent;
	
	private int startPos;
	
	private int endPos;
	
	public String content() {
		return this.conent;
	}
	
	public Comment content(String content) {
		this.conent = content;
		return this;
	}
	
	public int start() {
		return startPos;
	}
	
	public Comment start(int startPos) {
		this.startPos = startPos;
		return this;
	}
	
	public int end() {
		return endPos;
	}
	
	public Comment end(int endPos) {
		this.endPos = endPos;
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%d, %d]", content(), start(), end());
	}

}

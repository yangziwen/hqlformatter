package net.yangziwen.hqlformatter.format;

import net.yangziwen.hqlformatter.util.StringUtils;

public class Keyword {
	
	public Keyword() {}
	
	public Keyword(String name, int start, int end) {
		this.name(name).start(start).end(end);
	}

	private String name;
	
	private int start;
	
	private int end;
	
	public String name() {
		return name;
	}
	
	public Keyword name(String name) {
		String[] arr = name.trim().toUpperCase().split("\\s+");
		this.name = StringUtils.join(arr, ' ');
		return this;
	}
	
	public int start() {
		return start;
	}
	
	public Keyword start(int start) {
		this.start = start;
		return this;
	}
	
	public int end() {
		return end;
	}
	
	public Keyword end(int end) {
		this.end = end;
		return this;
	}
	
	public boolean contains(String keyword) {
		if(StringUtils.isBlank(keyword) || StringUtils.isBlank(name())) {
			return false;
		}
		return name().toLowerCase().contains(keyword.toLowerCase());
	}
	
	public boolean is(String keyword) {
		if(StringUtils.isBlank(keyword) || StringUtils.isBlank(name())) {
			return false;
		}
		String[] names = name().trim().split("\\s+");
		String[] keywords = keyword.trim().split("\\s+");
		if(names.length != keywords.length) {
			return false;
		}
		for(int i = 0; i < names.length; i++) {
			if(!keywords[i].equalsIgnoreCase(names[i])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%d, %d]", name(), start(), end());
	}
	
	public static Keyword returnNull(String sql) {
		int len = sql.length();
		return new Keyword("null", len, len);
	}
	
}

package net.yangziwen.hqlformatter.format;

public abstract class AbstractTable<T extends Table<T>> implements Table<T> {

	protected String alias;
	
	protected int startPos;
	
	protected int endPos;
	
	protected Comment comment;
	
	@Override
	public String alias() {
		return alias;
	}
	
	@SuppressWarnings("unchecked")
	public T alias(String alias) {
		this.alias = alias;
		return (T) this;
	}
	
	public int start() {
		return startPos;
	}
	
	@SuppressWarnings("unchecked")
	public T start(int startPos) {
		this.startPos = startPos;
		return (T) this;
	}
	
	public int end() {
		return endPos;
	}
	
	@SuppressWarnings("unchecked")
	public T end(int endPos) {
		this.endPos = endPos;
		return (T) this;
	}
	
	public Comment comment() {
		return this.comment;
	}
	
	@SuppressWarnings("unchecked")
	public T comment(Comment comment) {
		this.comment = comment;
		return (T) this;
	}
	
}

package net.yangziwen.hqlformatter.format;

public abstract class AbstractTable<T extends Table<T>> implements Table<T> {

	protected String alias;
	
	protected int startPos;
	
	protected int endPos;
	
	protected Comment headComment;
	
	protected Comment tailComment;
	
	@Override
	public String alias() {
		return alias;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T alias(String alias) {
		this.alias = alias;
		return (T) this;
	}
	
	@Override
	public int start() {
		return startPos;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T start(int startPos) {
		this.startPos = startPos;
		return (T) this;
	}
	
	@Override
	public int end() {
		return endPos;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T end(int endPos) {
		this.endPos = endPos;
		return (T) this;
	}
	
	@Override
	public Comment headComment() {
		return this.headComment;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T headComment(Comment comment) {
		this.headComment = comment;
		return (T) this;
	}
	
	@Override
	public Comment tailComment() {
		return this.tailComment;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T tailComment(Comment comment) {
		this.tailComment = comment;
		return (T) this;
	}
	
}

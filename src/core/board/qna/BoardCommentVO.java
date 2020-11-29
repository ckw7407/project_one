package core.board.qna;

import java.util.Date;

public class BoardCommentVO {
	private int commentid;
	private int parentArticleID;
	private String authorid;
	private String content;
	private Date regdate;
	private int pos;
	private int parentID;
	private int depth;
	
	public int getCommentid() {
		return commentid;
	}
	public void setCommentid(int commentid) {
		this.commentid = commentid;
	}
	public int getParentArticleID() {
		return parentArticleID;
	}
	public void setParentArticleID(int parentArticleID) {
		this.parentArticleID = parentArticleID;
	}
	public String getAuthorid() {
		return authorid;
	}
	public void setAuthorid(String authorid) {
		this.authorid = authorid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getRegdate() {
		return regdate;
	}
	public void setRegdate(Date regdate) {
		this.regdate = regdate;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	public int getParentID() {
		return parentID;
	}
	public void setParentID(int parentID) {
		this.parentID = parentID;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
}

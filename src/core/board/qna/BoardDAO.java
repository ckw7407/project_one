package core.board.qna;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import core.DBConnectionMgr;
import core.SQLHelper;

/***
 * 테이블을 생성합니다.
 */
public class BoardDAO {
	private DBConnectionMgr pool; 
	private Connection conn;
	private PreparedStatement pstmt;
	
	protected HashMap<String, String> qlList;
	
	/***
	 * 
	 */
	public BoardDAO() {
		connect();
		initWithSQL();
	}
	
	/***
	 * 
	 */
	public void connect() {	
		try {
			pool = DBConnectionMgr.getInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
	
	public boolean isExistsTable() {
		boolean ret = false;
		try {
			conn = pool.getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			
			ResultSet tables = dbm.getTables(null, null, "tblQNABoard", null);
			
			if (tables.next()) {
				// 테이블이 존재합니다.
				ret = true;
			}
			else {
				// 테이블이 존재하지 않습니다.
				ret = false;
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * MYSQL과 호환되는 시간 형식을 만듭니다.
	 * @return
	 */
	public String getLocalTime() {
		LocalDateTime dateTime = LocalDateTime.now();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatedDate = dateTime.format(formatter);
		
		return formatedDate;
	}
	
	/***
	 * 
	 */
	public void initWithSQL() {
		qlList = new HashMap<String, String>();
		
		qlList.put("count", "select count(*) from tblQNABoard");
		qlList.put("list", 
				"select articleID, authorID, articleType, title, regdate, viewCount, recommandCount" 
					+ " from tblQNABoard"
					+ " order by parentID desc, pos limit ?, ?");
		
		// 게시물 목록을 JSON으로 출력합니다.
		qlList.put("toJSON", "select articleID, articleType, title, authorID, regdate, viewCount, recommandCount from tblQNABoard");
		
		// 조회수 증가
		qlList.put("updatePostViewCount", "update tblQNABoard set viewCount = viewCount + 1 where articleID = ?");
		
		// 글을 읽습니다.
		qlList.put("readPost", "select articleID, articleType, title, authorID, content, regdate, viewCount, recommandCount from tblQNABoard where articleID = ?");

		// 글을 수정합니다.
		qlList.put("updatePost", "UPDATE tblqnaboard SET title = ?, content=?, regdate = NOW() WHERE articleID = ?");
		
		// 글 삭제
		qlList.put("deleteComments", "delete from tblqnaboardcomments WHERE parent_articleID = ?");
		qlList.put("deletePost", "delete from tblqnaboard where articleID = ?");
		
		// 특정 게시물에 있는 코멘트를 읽습니다.
		qlList.put("readComments", 
					"select authorID, content, regdate, pos, parentID, depth " 
				+ "from tblQNABoardComments "
				+ "where parent_articleID = ? order by parentID desc, pos, commentID");
		
		// 댓글을 작성합니다.
		qlList.put("writeComment", 
				"INSERT INTO tblQNABoardComments(parent_articleID, authorID, content, regdate)"
				+ " VALUES(?, ?, ?, NOW())");
		
		// 댓글의 댓글을 작성합니다.
		qlList.put("writeChildComment", "INSERT INTO tblqnaboardcomments(parent_articleID, authorID, content, regdate, pos, parentID, depth) "
				+ "VALUES(?, ?, ?, NOW(), ?, ?, ?)");
		
		// 댓글을 수정합니다.
		qlList.put("modifyComment", "UPDATE tblqnaboardcomments SET content = ? WHERE commentID = ? AND authorID = ?");
		
		// 댓글을 삭제합니다.
		qlList.put("deleteComments", "delete from tblqnaboardcomments WHERE parent_articleID = ?");
		
		// 글을 작성합니다.
		qlList.put("writePost", "insert into tblQNABoard(authorID, articleType, title, content, regdate, imageFileName) VALUES(?, ?, ?, ?, NOW(), ?)");
	}
	
	/***
	 * 
	 * @param commandName
	 * @return
	 */
	public String getQL(String commandName) {
		return qlList.get(commandName);
	}
	
	/***
	 * 
	 * @param query
	 * @return
	 */
	public boolean execute(String query, int... args) {
		boolean ret = false;
		
		try {
			conn = pool.getConnection();
						
			pstmt = conn.prepareStatement(query);
			
			for(int i = 0; i < args.length; i++) {
				pstmt.setInt(i + 1, args[i]);
			}			
			
			ret = pstmt.execute();
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
		
		return ret;
	}
	
	/***
	 * 
	 * @return
	 */
	public int getCount() {
		ResultSet rs = null;
		int count = 0;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("count"));
			
			rs = pstmt.executeQuery();
			
			count = rs.getInt(1);
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return count;
	}
	
	/***
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public List<BoardVO> getList(int start, int end) {
		ResultSet rs = null;
		List<BoardVO> list = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("count"));
			pstmt.setInt(1, start);
			pstmt.setInt(2, end);
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, BoardVO.class);
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return list;
		
	}
	
	/***
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray getListAll() {
		ResultSet rs = null;
		List<BoardVO> list = null;
		JSONArray arr = new JSONArray();
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("toJSON"));
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, BoardVO.class);
			
			if(list != null) {
				
				for(BoardVO vo : list) {
					JSONObject obj = new JSONObject();
					obj.put("postNumber", String.valueOf(vo.getArticleid()));
					obj.put("postType", vo.getArticletype());
					
					StringBuilder sb = new StringBuilder();
					sb.append(vo.getTitle());
					
					// 코멘트를 읽습니다.
					List<BoardCommentVO> comments = this.readAllComments(vo.getArticleid());
					int commentsCount = comments.size();
					
					
					sb.append("&nbsp<span class='comment'>[");
					sb.append(commentsCount);
					sb.append("]</span>");
					
					obj.put("postTitle", sb.toString());
					obj.put("name", vo.getAuthorid());
					obj.put("create_at", vo.getRegdate().toString());
					obj.put("view", vo.getViewcount());
					obj.put("recommandCount", vo.getRecommandcount());
					

					
					arr.add(obj);
				}
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return arr;		
	}	
	
	/***
	 * 
	 * @param postNumber
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray readPost(int postNumber) {
		ResultSet rs = null;
		List<BoardVO> list = null;
		JSONArray arr = new JSONArray();
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("readPost"));
			pstmt.setInt(1, postNumber);
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, BoardVO.class);
			
			
			if(list != null) {
				
				for(BoardVO vo : list) {
					JSONObject obj = new JSONObject();
					obj.put("boardType", "qna");
				    obj.put("postNumber", String.valueOf(vo.getArticleid()));
				    obj.put("title", vo.getTitle());
				    obj.put("contents", vo.getContent());
				    obj.put("view", vo.getViewcount());
				    obj.put("create_at", vo.getRegdate().toString());
				    obj.put("author", vo.getAuthorid());
				    
				    // 코멘트를 생성합니다.
				    JSONArray comments = readAllCommentsToJson(vo.getArticleid());
				    obj.put("comments", comments);
				    
					arr.add(obj);
				}
			}
						
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return arr;	
	}
		
	/***
	 * 
	 * @param authorID
	 * @param articleType
	 * @param title
	 * @param content
	 * @param imageFileName
	 * @return
	 */
	public boolean writePost(String authorID, String articleType, String title, String content, String imageFileName) {
		boolean ret = false;
				
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("writePost"));
			pstmt.setString(1, authorID);
			pstmt.setString(2, articleType);
			pstmt.setString(3, title);
			pstmt.setString(4, content);
			pstmt.setString(5, imageFileName);
			
			if(pstmt.executeUpdate() > 0) {
				ret = true;
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
		
		return ret;
	}
	
	public void deletePost(int articleID) {
		try {
			execute(getQL("deleteComments"), articleID);
			execute(getQL("deletePost"), articleID);			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updatePostViewCount(int articleID) {
		try {
			execute(getQL("updatePostViewCount"), articleID);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * 
	 * @param articleID
	 * @param authorID
	 * @param contents
	 */
	public boolean writeComment(int articleID, String authorID, String contents) {
		boolean isOK = false;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("writeComment"));
			pstmt.setInt(1, articleID);
			pstmt.setString(2, authorID);
			pstmt.setString(3, contents);
			
			if(pstmt.executeUpdate() > 0) {
				isOK = true;
			}
			
		} catch(SQLException e) {
			e.printStackTrace(); 
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return isOK;
	}
	
	public boolean writeChildComment(int articleID, String authorID, String contents, int pos, int parentCommentID, int depth) {
		boolean isOK = false;
		ResultSet rs = null;
				
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("writeChildComment"));
			pstmt.setInt(1, articleID);
			pstmt.setString(2, authorID);
			pstmt.setString(3, contents);
			pstmt.setInt(4, pos);
			pstmt.setInt(5, parentCommentID);
			pstmt.setInt(6, depth);
			
			if(pstmt.executeUpdate() > 0) {
				isOK = true;
			}
			
		} catch(SQLException e) {
			e.printStackTrace(); 
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return isOK;
	}
	
	public List<BoardCommentVO> readAllComments(int articleID) {
		ResultSet rs = null;
		List<BoardCommentVO> list = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("readComments"));
			pstmt.setInt(1, articleID);
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, BoardCommentVO.class);
						
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
		
		return list;
	}
	
	/**
	 * 댓글 목록을 JSON 데이터로 반환합니다.
	 * 
	 * @param articleID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray readAllCommentsToJson(int articleID) {
		JSONArray arr = new JSONArray();
		
		List<BoardCommentVO> list = readAllComments(articleID);
		
		if(list != null) {
			for(BoardCommentVO vo : list) {
				JSONObject data = new JSONObject();
				data.put("author", vo.getAuthorid());
				data.put("create_at", vo.getRegdate().toString());
				data.put("contents", vo.getContent());
				
				arr.add(data);
			}
		}
		
		return arr;
	}
	
	public boolean updateComment(String content, int commentID, String authorID) throws SQLException
	{
		boolean isOK = false;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("modifyComment"));
			pstmt.setString(1, content);
			pstmt.setInt(2, commentID);
			pstmt.setString(3, authorID);
			
			if(pstmt.executeUpdate() > 0) {
				isOK = true;
			}
			
		} catch(Exception e) {
			e.printStackTrace(); 
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return isOK;
	}
	
	public static void main(String[] args) {
		Path dir = Paths.get("WebContent");
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.*")) {
			for( Path file: stream ) {
				System.out.println(file.getFileName());
			}
		} catch(IOException | DirectoryIteratorException e) {
			e.printStackTrace();
		}
	}

}
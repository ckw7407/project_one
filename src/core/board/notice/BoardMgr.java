package core.board.notice;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import core.DBConnectionMgr;

public class BoardMgr {
	
	private DBConnectionMgr pool;
	private static final String SAVEFOLDER = "E:/work/filestorage";
	private static final String ENCTYPE = "EUC-KR";
	private static final int MAXSIZE = 5 * 1024 * 1024;
	
	public BoardMgr() {
		
		try {
			pool = DBConnectionMgr.getInstance();
		} catch(Exception e) {
			e.getMessage();
		}		
	}
		
	// 게시판 리스트
	public Vector<BoardBean> getBoardList(String keyField, String keyWord, int start, int end) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Vector<BoardBean> vlist = new Vector<BoardBean>();
		
		try {
			conn = pool.getConnection();
			if(keyWord.equals("null") || keyWord.equals("")) {
				sql = "select * from bbsNotice order by wrtdate, pos limit ?, ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, start);
				pstmt.setInt(2, end);
			} else {
				sql = "select * from bbsNotice where " + keyField + " like ?";
				sql += "order by wrtdate, pos limit ?, ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + keyWord + "%");
				pstmt.setInt(2, start);
				pstmt.setInt(3, end);
			}
			
			rs = pstmt.executeQuery();
			while(rs.next()) {
				BoardBean bean = new BoardBean();
				bean.setCtxtno(rs.getInt("num"));
				bean.setWrtnm(rs.getString("name"));
				bean.setCtitle(rs.getString("title"));
				bean.setPos(rs.getInt("pos"));
				bean.setWrtdate(rs.getString("date"));
				bean.setViewcnt(rs.getInt("cnt"));
				vlist.add(bean);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return vlist;
	}
	
	// 총 게시물 수
	public int getTotalCount(String keyField, String keyWord) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int totalCount = 0;
		
		try {
			conn = pool.getConnection();
			if(keyWord.equals("null") || keyWord.equals("")) {
				sql = "select count(ctxtno) from bbsnotice";
				pstmt = conn.prepareStatement(sql);
			} else {
				sql = "select count(ctxtno) from bbsnotice where " + keyField + " like ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + keyWord + "%");
			}
			
			rs = pstmt.executeQuery();
			if(rs.next()) {
				totalCount = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return totalCount;
	}
	
	
	// 게시판 입력
	public void insertBoard(HttpServletRequest req) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		MultipartRequest multi = null;
		int filesize = 0;
		String filename = null;
		
		try {
			conn = pool.getConnection();
			sql = "select max(ctxtno) from bbsnotice";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			int ref = 1;
			
				if(rs.next()) 
					ref = rs.getInt(1) + 1;
				
				File file = new File(SAVEFOLDER);
				
				if(!file.exists())
					file.mkdirs();
				
				multi = new MultipartRequest(req, SAVEFOLDER, MAXSIZE, ENCTYPE, new DefaultFileRenamePolicy());
				
				if(multi.getFilesystemName("filename") != null) {
					filename = multi.getFilesystemName("filename");
					filesize = (int)multi.getFile("filename").length();
				}
				
				String content = multi.getParameter("ctxt");
				
				if(multi.getParameter("contentType").equalsIgnoreCase("TEXT")) {
					content = UtilMgr.replace(content, "<", "&lt;");
				}
				sql = "insert bbsnotice(wrtnm,ctxt,ctitle,pos,depth,regdate,pass,count,ip,filename,filesize) ";
				sql += "values(?, ?, ?, ?, 0, 0, now(), ?, 0, ?, ?, ?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, multi.getParameter("name"));
				pstmt.setString(2, content);
				pstmt.setString(3, multi.getParameter("subject"));
				pstmt.setInt(4, ref);
				pstmt.setString(5, multi.getParameter("pass"));
				pstmt.setString(6, multi.getParameter("ip"));
				pstmt.setString(7, filename);
				pstmt.setInt(8, filesize);
				pstmt.executeUpdate();
				
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
	}
	
	
	// 게시물 리프레싱 및 재출력
	public BoardBean getBoard(int num) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		BoardBean bean = new BoardBean();
		
		try {
			conn = pool.getConnection();
			sql = "select * from tblBoard where num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				bean.setNum(rs.getInt("num"));
				bean.setName(rs.getString("name"));
				bean.setSubject(rs.getString("subject"));
				bean.setContent(rs.getString("content"));
				bean.setPos(rs.getInt("pos"));
				bean.setRef(rs.getInt("ref"));
				bean.setDepth(rs.getInt("depth"));
				bean.setRegdate(rs.getString("regdate"));
				bean.setPass(rs.getString("pass"));
				bean.setCount(rs.getInt("count"));
				bean.setFilename(rs.getString("filename"));
				bean.setFilesize(rs.getInt("filesize"));
				bean.setIp(rs.getString("ip"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		return bean;
	}
	
	// 조회수 증가
	public void upCount(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		try {
			conn = pool.getConnection();
			sql = "update tblBoard set count = count+1 where num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			pstmt.executeUpdate();
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
	}
	
	// 게시물 삭제 
	public void deleteBoard(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			conn = pool.getConnection();
			sql = "select filename from tblBoard where num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			
			if(rs.next() && rs.getString(1) != null) {
				if(!rs.getString(1).equals("")) {
					
					File file = new File(SAVEFOLDER + "/" + rs.getString(1));
					if(file.exists())
						UtilMgr.delete(SAVEFOLDER + "/" + rs.getString(1));
				}				
			} 
			
			sql = "delete from tblBoard where num = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			pstmt.executeUpdate();			
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}		
	}
	
	
	// 파일 다운로드
	public void downLoad(HttpServletRequest req, HttpServletResponse res, JspWriter out, PageContext pageContext) {
		
		try {
			String filename = req.getParameter("filename");
			File file = new File(UtilMgr.con(SAVEFOLDER) + File.separator + filename);
			byte b[] = new byte[(int) file.length()];
			
			res.setHeader("Accept-Ranges", "bytes");
			
			String strClient = req.getHeader("User-Agent");
			
			if(strClient.indexOf("MSIE6.0") != -1) {
				res.setContentType("application/smnet;charset=euc-kr");
				res.setHeader("Content-Disposition", "filename=" + filename + ";");
			} else {
				res.setContentType("application/smnet;charset=euc-kr");
				res.setHeader("Content-Disposition", "attachment;filename=" + filename + ";");				
			}
			out.clear();
			out = pageContext.pushBody();
			
			if(file.isFile()) {
				BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
				BufferedOutputStream outs = new BufferedOutputStream(res.getOutputStream());
				
				int read = 0;
				while((read = fin.read(b)) != -1) {
					outs.close();
					fin.close();
				}				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	
	
	
}
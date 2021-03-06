package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;

import core.SQLHelper;
import sql.ContentLoader;
import utils.DBConnectionMgr;
import vo.CustomerVO;
import vo.ProductVO;

public class ContentDAO implements IDAO {
	
	private DBConnectionMgr pool; 
	private Connection conn;
	private PreparedStatement pstmt;
	
	private static ContentDAO instance = null;
	private ContentLoader qlList = null;
	
	public final String[] CATEGORY = {
        "", 
        "트렌드", 
        "댄디", 
        "유니크", 
        "레플리카·제작", 
        "스트릿", 
        "클래식수트", 
        "빅사이즈", 
        "슈즈", 
        "액세서리"				
	};
	
	public final String[] ITEM_CATEGORY = {
			"", "상의", "아우터", "하의", "트레이닝", "수트", "신발", "아우터 수트", "가방", "액세서리"			
	};
	
	public final String[] AGES = {
			"",
			"10대",
			"20대",
			"30대"
	};
	
	private ContentDAO() {
		create();
	}
	
	public void create() {
		createPool();
		initWithSQL();
	}
	
	public void createPool() {
		try {
			pool = DBConnectionMgr.getInstance();	
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initWithSQL() {
		qlList = new ContentLoader();
	}
	
	public static synchronized ContentDAO getInstance() {
		if(instance == null) {
			instance = new ContentDAO();
		}
		
		return instance;
	}
	
	public String getQL(String command) {
		return qlList.get(command);
	}
	
	public String getCategory(String pageType, String typeValue) {
		int type = 100;
		
		switch(pageType) {
		default:
		case "shop":
			type = Integer.parseInt(typeValue);
			
			return CATEGORY[type - 100];			
		case "item":
			type = Integer.parseInt(typeValue);
			
			return ITEM_CATEGORY[type - 100];
		}
	}
	
	public String getAge(String typeValue) {
		return typeValue + "대";
	}
	
	/**
	 * 
	 * @param pageType
	 * @param genderType
	 * @param shopType
	 * @return
	 */
	public List<ProductVO> getData(String pageType, String genderType, String shopType, String category, String ages) {
		
		ResultSet rs = null;
		List<ProductVO> list = null;
		
		if(category != null) {
			if(!category.equals("100")) {
				category = getCategory(pageType, category);
			} else {
				category = null;
			}
		}
		
		if(ages != null) {
			if(!ages.equals("all")) {
				ages = getAge(ages);
			} else {
				ages = null;
			}
		}
		
		
		System.out.println("카테고리 : " + category);
		System.out.println("연령대 : " + ages);
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(
					getQL("전체 데이터 추출")
					+ (category != null ? " AND texts LIKE ?" : "")
					+ (ages != null ? " AND texts LIKE ?" : "")
					+ " group by contentUrl"
					);
			pstmt.setString(1, pageType);
			pstmt.setString(2, genderType);
			pstmt.setString(3, shopType);
			
			int i = 3;
			
			if(category != null) {
				i += 1;
				pstmt.setString(i, "%" + category + "%");
			}
			
			if(ages != null) {
				i += 1;
				pstmt.setString(i, "%" + ages + "%");
			}			
			
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, ProductVO.class);
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return list;
	}
	
	/**
	 * 연령대로 JSON 데이터를 필터링합니다. category
	 * 
	 * @param pageType
	 * @param genderType
	 * @param shopType
	 * @param ages
	 * @return
	 */
	public List<ProductVO> searchAsAge(String pageType, String genderType, String shopType, String ages) {
		
		ResultSet rs = null;
		List<ProductVO> list = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("번호 붙여 검색"));
			pstmt.setString(1, pageType);
			pstmt.setString(2, genderType);
			pstmt.setString(3, shopType);
			pstmt.setString(4, "%" + ages + "%");
			
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, ProductVO.class);
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return list;
	}
	
	/**
	 * 카테고리로 검색
	 * @param pageType
	 * @param genderType
	 * @param shopType
	 * @param category
	 * @return
	 */
	public List<ProductVO> searchAsCategory(String pageType, String genderType, String shopType, String category) {
		return searchAsAge(pageType, genderType, shopType, category);
	}	
	
	/**
	 * 연령대로 JSON 데이터를 필터링합니다. category
	 * 
	 * @param pageType
	 * @param genderType
	 * @param shopType
	 * @param ages
	 * @return
	 */
	public List<ProductVO> searchAsAny(String pageType, String genderType, String shopType, String category, String ages) {
		ResultSet rs = null;
		List<ProductVO> list = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("나이 또는 카테고리로 필터링"));
			pstmt.setString(1, pageType);
			pstmt.setString(2, genderType);
			pstmt.setString(3, shopType);
			pstmt.setString(4, "%" + category + "%");
			pstmt.setString(5, "%" + ages + "%");
			
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, ProductVO.class);
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return list;
	}
	
	public JSONArray getItemCategories() {
		ResultSet rs = null;
		JSONArray categories = new JSONArray();
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("아이템 카테고리 생성"));
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				categories.add( rs.getString("category") );
			}
			
		}  catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return categories;
	}
	
	/** 
	 * 브랜드 명을 유일 키인 ID로 찾습니다.
	 * 
	 * @param id
	 * @return
	 */
	public String findShopName(int id) {
		ResultSet rs = null;
		String shopName = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("브랜드 명 찾기"));
			pstmt.setInt(1, id);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				shopName = rs.getString(1);
			}
			
		}  catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return shopName;
		
	}
	
	public List<ProductVO> findThumbnail(String shopName) {
		ResultSet rs = null;
		List<ProductVO> list = null;
		
		try {
			conn = pool.getConnection();
			
			pstmt = conn.prepareStatement(getQL("브랜드 썸네일 찾기"));
			pstmt.setString(1, shopName);
			
			rs = pstmt.executeQuery();
			
			List<ProductVO> myList = SQLHelper.putResult(rs, ProductVO.class);
			
			if(myList != null) {
				list = myList;
				
				System.out.println(Arrays.toString(list.toArray()));
			}
			
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return list;
	}	
	
	/**
	 * 특정 쇼핑몰의 전체 상품을 검색합니다 (DB에 있는 것만 찾습니다)
	 * 
	 * @param pageType
	 * @param shopName
	 * @return
	 */
	public List<ProductVO> searchAsShopName(String pageType, String shopName) {
		ResultSet rs = null;
		List<ProductVO> retList = null;
		
		try {
			conn = pool.getConnection();
			pstmt = conn.prepareStatement(getQL("브랜드 별 검색"));
			
			pstmt.setString(1, pageType);
			pstmt.setString(2, shopName);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				List<ProductVO> list = SQLHelper.putResult(rs, ProductVO.class);
				retList = list;
			}
			
		}  catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return retList;
		
	}
	
	public List<ProductVO> getDetail(String title, String price) {
		
		ResultSet rs = null;
		List<ProductVO> list = null;
		String sql = null;
		
		try {
			conn = pool.getConnection();
			sql = "select b.title, b.price, a.imgUrl FROM tblImageHash a, tblproduct b"
				  + " where title = ? and price = ?"
				  + " and a.imgUrl = b.contentUrl"
				  + " group by contentUrl";
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, title);
			pstmt.setString(2, price);
			
			rs = pstmt.executeQuery();
			list = SQLHelper.putResult(rs, ProductVO.class);
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return list;
	}
	
	public boolean insertDetail(List<ProductVO> p, int qty) {
		
		boolean success = false;
		
		try {
			conn = pool.getConnection();
			
			for(ProductVO list : p) {
			String query = "insert into CartNPay(id, title, price, qty) "
					+ "values(?,?,?,?)";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, list.getId());
			pstmt.setString(2, list.getTitle());
			pstmt.setString(3, list.getPrice());
			pstmt.setInt(4, qty);			
			}
			
			
			if(pstmt.executeUpdate() > 0) {
				conn.commit();
				System.out.println("데이터 입력 완료.");
				success = true;
			};
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt);
		}
		
		return success;
	}
	
	public List<ProductVO> findThumbnail(String shopName) {
		
		ResultSet rs = null;
		List<ProductVO> list = null;
		String sql = null;
		
		try {
			conn = pool.getConnection();
			sql = "SELECT COUNT(DISTINCT b.title) AS cnt, b.title, "
				+ "b.price, b.genderType, b.shopType, b.shopName, "
				+ "b.pageType, c.Contenturl, a.imgId "					
				+ "FROM tblImageHash a, tblproduct b, tblproduct c "
				+ "WHERE b.title IS NOT NULL "
				+ "and b.pageType = 'item' "
				+ "AND c.pageType = 'shop' "
				+ "AND a.imgUrl = b.contentUrl"
				+ "AND b.shopName = c.shopName"
				+ "GROUP BY b.shopname "
				+ "order by cnt DESC";
			
			pstmt = conn.prepareStatement(sql);			
			rs = pstmt.executeQuery();
			
			list = SQLHelper.putResult(rs, ProductVO.class);
			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		
		return list;
	}
}

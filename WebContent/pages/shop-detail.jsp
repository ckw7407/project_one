<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib  prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page import="java.util.List, vo.ProductVO, dao.ContentDAO" %>
<%@ page import="java.net.URLDecoder" %>
<%
	
	int id = Integer.parseInt(request.getParameter("id"));

	String pageType = "item";

	ContentDAO contentDAO = ContentDAO.getInstance();
	
	String shopName = contentDAO.findShopName(id);
	List<ProductVO> list = contentDAO.searchAsShopName(pageType, shopName);
	List<ProductVO> thumb = contentDAO.findThumbnail(shopName);

	String thumbNailImage = null;
	if(!thumb.isEmpty()) {
		ProductVO thumbNailVO = thumb.get(0);
		thumbNailImage = "/images/shop/" + thumbNailVO.getGendertype() + "/" +  thumbNailVO.getShoptype() + "/" + thumbNailVO.getContenturl();
	}
	
	String mainUrl = "https://drive.google.com/uc?export=view&id=";
%>
<c:set var="list" value="<%= list %>" />
<c:set var="thumbNailImage" value="<%=thumbNailImage %>" />
<c:set var="shopName" value="<%=shopName %>" />
<c:if test="${ list.size() == 0 or shopName == null }">
	<script>
		alert("DB에서 상품을 찾지 못했습니다.");
		history.go(-1);
	</script>
</c:if>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>쇼핑몰 클론 프로젝트</title>
    <link rel="stylesheet" href="../css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap" rel="stylesheet">
    <script src="https://kit.fontawesome.com/a99df0f94f.js" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/underscore@1.11.0/underscore-min.js"></script>
    <link rel="stylesheet" href="../css/shop-detail.css">
    <link rel="stylesheet" href="../libs/themes/wrunner-default-theme.css">
    <script src="../libs/wrunner-jquery.js"></script>
    <style>
		.card-container {
		    display: flex;
		    flex-direction: row;
		    justify-content: left;
		    align-items: center;
		    background-color: #fff;
		}    
    </style>
	 <%
	 	for(int i = 0; i < list.size(); i++) {
	 		ProductVO vo = list.get(i);
	 %>
	    <style>
            .card {
                display: flex;
                flex-wrap: wrap;
                flex: auto auto auto;
                flex-direction: column;
                border: 1px solid #F2F5F9;
            }
            
            .card p[d-<%=vo.getId()%>]::before {
                content: "";
                width: 100%;
                height: 78%;
                background: url(<%=mainUrl+vo.getImgid()%>) left top;
                background-size: cover;
                background-repeat: no-repeat;
                position: absolute;
                border-radius: 0;
                left: 0;
                top: 0;
                z-index: 0;
            }

            .card p[d-<%=vo.getId()%>]:hover::before {
                filter: brightness(1.1);
                border-radius: 0;
                transition: all .2s linear;
            }
	    </style>
	 <%
	 	}
	 %>
</head>
<body>
    <!-- 컨테이너의 시작 -->
    <div class="container">
        <!-- 헤더의 시작 -->
        <jsp:include page="/header.jsp">
        	<jsp:param value="true" name="isHide"/>
        </jsp:include>

        <!-- 본문의 시작 -->
        <section>
            <!-- 본문이 들어가는 래퍼 -->
            <div class="contents-wrapper">
                <div class="card-container">
                    <div class="shop-detail">
                        <div class="detail-trailer-list">
                            <div>
                                <p>비슷한 샵</p>
                            </div>
                            <ul>
                                <li>
                                    <a href="#"><img src="https://dummyimage.com/64x64/000/fff"></a>
                                </li>
                                <li>
                                    <a href="#"><img src="https://dummyimage.com/64x64/000/fff"></a>
                                </li>
                                <li>
                                    <a href="#"><img src="https://dummyimage.com/64x64/000/fff"></a>
                                </li>
                                <li>
                                    <a href="#"><img src="https://dummyimage.com/64x64/000/fff"></a>
                                </li>
                            </ul>
                        </div>
                        <div class="centered">
                            <a href="#"><img src="${thumbNailImage }"></a>
                        </div>
                        <div>
                            <h2><%= shopName %></h2>
                        </div>
                    </div>
                    <div class="header-filter-box">
                        <!-- 검색 필터 래퍼 -->
                        <div class="header-filter-box-wrapper">
                            <!-- 검색 필터 하단 : 대/중/소 분류 -->
                            <div class="header-filter-box-footer">
                                <!-- 검색 필터 왼쪽 : 카테고리 선택 -->
                                <div class="header-filter-box-footer-left">
                                    <div class="header-filter-box-footer-left-button">
                                        <span>상품 카테고리<em>전체(0)</em></span>
                                        <i class="fa fa-caret-down"></i>
                                    </div>
                                    <div class="header-filter-box-left-dropdown-menu" data-attr="상품 카테고리">
                                        <div class="header-filter-box-left-dropdown-menu-content">
                                            <dl>
                                                <dt>샵 카테고리</dt>
                                                <dd><label><input class="header-filter-box-left-shop-categories" type="checkbox" name="categories"><span>ALL</span></label></dd>
                                                <dd><label><input class="header-filter-box-left-shop-categories" type="checkbox" name="categories"><span>OUTER</span></label></dd>
                                                <dd><label><input class="header-filter-box-left-shop-categories" type="checkbox" name="categories"><span>TOP</span></label></dd>
                                                <dd><label><input class="header-filter-box-left-shop-categories" type="checkbox" name="categories"><span>BOTTOM</span></label></dd>
                                                <dd><label><input class="header-filter-box-left-shop-categories" type="checkbox" name="categories"><span>BAG</span></label></dd>
                                                <dd><label><input class="header-filter-box-left-shop-categories" type="checkbox" name="categories"><span>SHOES</span></label></dd>
                                                <dd><label><input class="header-filter-box-left-shop-categories" type="checkbox" name="categories"><span>ACC·ETC</span></label></dd>
                                            </dl>
                                        </div>                                
                                    </div>
                                </div>
                                <!-- 검색 필터 중앙 : 검색 -->
                                <div class="header-filter-box-footer-center">
                                    <label for="name-search"></label><input class="input-non-border-box" type="text" placeholder="이름으로 검색">
                                </div>
                                <!-- 검색 필터 오른쪽 : 분류 -->
                                <div class="header-filter-box-footer-right">
                                    <ul>
                                        <li></li>
                                        <li>
                                            <div id="slide-bar">

                                            </div>
                                        </li>
                                        <li></li>
                                        <li></li>
                                    </ul>                         
                                </div>
                            </div>
                        </div>
                    </div>
                    <%
                    	for(ProductVO card : list) {
                    %>
                    	<c:set var="card" value="<%= card %>" />
	                    <div class="card">
	                        <p d-<%= card.getId() %>></p>
		                    <i class="shop-hot-icon"></i>
		                    <div class="item-button-container"> 
		                        <h2>${card.getTitle()}</h2>
		                        <p>${card.getPrice()}</p>
		                        <p>${card.getShopname()}</p>
		                        <button class="like-button"></button>
		                    </div> 	                        
	                    </div>                     
                    <%
                    	}
                    %>
                </div> 
            </div>
        </section>
    </div>
    <!-- 라이트 박스-->
    <div id="light-box-container">
    </div>
    <jsp:include page="/pages/login.jsp"></jsp:include> 
    <!-- index.js는 메인 용이므로 알맞은 스크립트를 사용해야 합니다-->
    <script type="module" src="../js/index.js"></script>
    <script>
        (function() {

            const mySlider = $("#slide-bar").wRunner({
                // step size
                step: 100,

                // or 'range'
                type: "range",

                // min/max values
                limits: {
                    minLimit: 0, 
                    maxLimit: 128000
                },

                // default value
                singleValue: 1000,
                rangeValue: { 
                    minValue: 20, 
                    maxValue: 126000
                },

                // root element
                sliderRoots: document.querySelector(".header-filter-box-footer-right"),

                // the number of divisions
                divisionsCount: 10,

                // shows labels
                valueNoteDisplay: true,

                // theme name
                theme: "default",

                // or 'vertical'
                direction: 'horizontal'

            });        

        })();

    </script>
    <script src="/js/itemPage.js"></script>
</body>
</html>    

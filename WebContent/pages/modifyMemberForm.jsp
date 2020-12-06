<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="vo.*"%>
<%
	request.setCharacterEncoding("UTF-8");
%>
<c:set var="contextPath" value="${ pageContext.request.contextPath }" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>쇼핑몰 클론 프로젝트</title>

<link
	href="https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap"
	rel="stylesheet">
<script src="https://kit.fontawesome.com/a99df0f94f.js"
	crossorigin="anonymous"></script>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script
	src="https://cdn.jsdelivr.net/npm/underscore@1.11.0/underscore-min.js"></script>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
	integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
	crossorigin="anonymous">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
	integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp"
	crossorigin="anonymous">
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
	integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
	crossorigin="anonymous"></script>
<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>	
<link rel="stylesheet" href="../css/style.css">
<style>

/* https://github.com/twbs/bootstrap-sass/blob/v3.4.1/assets/stylesheets/bootstrap/_variables.scss#L66 */
.container {
	margin: 0;
	padding: 0;
}

body {
	font-size: 16px;
	line-height: normal;
	padding: 0;
	margin: 0;
}

p {
	font-size: 16px;
	line-height: normal;
	padding-bottom: 0;
}

img, a {
	padding: 0;
	margin: 0;
}

.contents-wrapper {
	margin-top: 8em;
}

.header-filter-box {
	display: none;
}
</style>
</head>
<body>
	<!-- 컨테이너의 시작 -->
	<div class="container">
		<jsp:include page="header.jsp"></jsp:include>

		<!-- 본문의 시작 -->
		<section>
			<!-- 본문이 들어가는 래퍼 -->
			<div class="contents-wrapper">
				<div class="member-form-container col-md-10 col-md-offset-2">

					<div class="jumbotron col-md-8">
						<p class="lead">회원 정보 수정</p>
					</div>

					<form class="form-horizontal col-sm-8"
						action="/members/modifyMember.do" onclick="return false;">
						<input type="hidden" name="id" value="${member.getId()}">
						<div class="form-group">
							<label class="control-label col-sm-2" for="email">이메일:</label>
							<div class="col-sm-6">
								<input type="email" class="form-control" id="email" autocomplete="email"
									placeholder="Enter email" value="${ member.getEmail() }">
							</div>
							<div class="col-sm-2">
								<button id="email-check-btn" class="btn btn-default" data-usable="usable" onclick="emailCheck()">이메일 중복 확인</button>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="pwd">비밀번호:</label>
							<div class="col-sm-6">
								<input type="password" name="pw" class="form-control" id="pw"
									placeholder="비밀 번호 입력" autocomplete="current-password">
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="pwd">비밀번호 확인:</label>
							<div class="col-sm-6">
								<input type="password" name="pw" class="form-control" id="pw"
									placeholder="비밀 번호 다시 입력" autocomplete="new-password">
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="name">이름:</label>
							<div class="col-sm-6">
								<input type="text" class="form-control" id="name" name="name"
									placeholder="이름 입력" value="${ member.getName() }">
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="tel">휴대폰 번호</label>
							<div class="col-sm-6">
								<input type="text" class="form-control" id="tel" name="tel" autocomplete="tel"
									placeholder="휴대폰 번호 입력" value="${ member.getTel() }">
							</div>
						</div>

						<div class="form-group">
							<label class="control-label col-sm-2" for="email">우편 번호:</label>
							<div class="col-sm-6">
								<input type="text" class="form-control col-sm-2" id="zipcode" autocomplete="postal-code"
									value="${ member.getZipCode() }">
								<input type="text" class="form-control" id="address2" name="address" autocomplete="street-address"
									value="${member.getAddress() }">									
							</div>
							<div class="col-sm-2">
								<button class="btn btn-default" onclick="searchAddress(this)">주소 검색 하기</button>
							</div>
						</div>

						<div class="form-group">
							<div class="panel">
								<label class="control-label col-sm-2" for="tel">가입일</label>
								<div class="col-sm-6">
									<p>${ member.getJoinDate() }</p>
								</div>								
							</div>
						</div>

						<div class="form-group">
							<div class="col-sm-offset-2 col-sm-2">
								<button class="btn btn-danger">회원 탈퇴</button>
							</div>
							<div class="col-sm-offset-1 col-sm-2">
								<input type="submit" class="btn btn-default" value="수정 완료" onsubmit="submit()">
							</div>
						
						</div>

					</form>
				</div>
			</div>
		</section>
	</div>
	<!-- 라이트 박스-->
	<div id="light-box-container"></div>
	<!-- index.js는 메인 용이므로 알맞은 스크립트를 사용해야 합니다-->
	<script type="module" src="../js/MorePage.js"></script>
	<script src="../js/modifyMemberForm.js"></script>
</body>
</html>
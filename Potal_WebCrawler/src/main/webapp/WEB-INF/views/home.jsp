<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<html>
	<head>
		<title>Home</title>
	</head>
	
	<!-- 아직 기본적인 형태만 갖춘 상태 -->
	
	<body>
		<form action="googleSearch" method="post">
			<div>
				검색 키워드(두 단어 이상 입력, 공백으로 구분) : <input type=text name="searchKeywords" value="${searchKeywords}"/>
			</div>
			<div>
				나이 : <input type=text name="age" value="${age}"/>
			</div>
			<div>
				성별 : <input type=text name="sex" value="${sex}"/>
			</div>
			<div>
				<input type="submit" value="검색" />
			</div>
			<div>
				구글 검색 기능 점수 : <input type=text name="googleScore" value="${googleScore}" />
			</div>
			<div>
				네이버 검색 기능 점수 : <input type=text name="naverScore" value="${naverScore}" />
			</div>
			<div>
				다음 검색 기능 점수 : <input type=text name="daumScore" value="${daumScore}" />
			</div>
		</form>
	</body>
</html>
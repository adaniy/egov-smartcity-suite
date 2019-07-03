<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/taglib/cdn.tld" prefix="cdn"%>

<div class="panel-heading">
			<div class="panel-title text-center no-float">
				<c:if test="${not empty error}">
					<strong class="error-msg">${error}</strong>
				</c:if>
				<c:if test="${not empty message}">
					<strong>${message}</strong>
				</c:if>
			</div>
</div>
		
<%@ include file="edcrapplication-view.jsp" %>

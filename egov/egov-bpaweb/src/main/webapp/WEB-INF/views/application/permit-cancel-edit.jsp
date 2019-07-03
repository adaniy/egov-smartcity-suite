<%--
  ~    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
  ~    accountability and the service delivery of the government  organizations.
  ~
  ~     Copyright (C) 2017  eGovernments Foundation
  ~
  ~     The updated version of eGov suite of products as by eGovernments Foundation
  ~     is available at http://www.egovernments.org
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program. If not, see http://www.gnu.org/licenses/ or
  ~     http://www.gnu.org/licenses/gpl.html .
  ~
  ~     In addition to the terms of the GPL license to be adhered to in using this
  ~     program, the following additional terms are to be complied with:
  ~
  ~         1) All versions of this program, verbatim or modified must carry this
  ~            Legal Notice.
  ~            Further, all user interfaces, including but not limited to citizen facing interfaces,
  ~            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
  ~            derived works should carry eGovernments Foundation logo on the top right corner.
  ~
  ~            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
  ~            For any further queries on attribution, including queries on brand guidelines,
  ~            please contact contact@egovernments.org
  ~
  ~         2) Any misrepresentation of the origin of the material is prohibited. It
  ~            is required that all modified versions of this material be marked in
  ~            reasonable ways as different from the original version.
  ~
  ~         3) This license does not grant any rights to any user of the program
  ~            with regards to rights under trademark law for use of the trade names
  ~            or trademarks of eGovernments Foundation.
  ~
  ~   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
  ~
  --%>

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/taglib/cdn.tld" prefix="cdn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div class="row">
	<div class="col-md-12">
		<form:form role="form" action="../approve" method="post"
			modelAttribute="permitCancel" id="permitCancelEditForm"
			cssClass="form-horizontal form-groups-bordered"
			enctype="multipart/form-data">
			<div class="panel panel-primary" data-collapsed="0">
				<div class="panel-body">
					<div class="row add-border">
						<div class="col-sm-3 add-margin">
							<spring:message code="lbl.plan.permission.no" />
						</div>
						<div class="col-sm-3 add-margin view-content">
							<a target="popup"
								onclick="window.open('/bpa/application/details-view/by-permit-number/${permitCancel.application.planPermissionNumber}','popup','width=1100,height=700'); return false;">${permitCancel.application.planPermissionNumber}</a>
						</div>
						<div class="col-sm-3 add-margin">
							<spring:message code="lbl.build.plan.permission.date" />
						</div>
						<div class="col-sm-3 add-margin view-content">
							<fmt:formatDate
								value="${permitCancel.application.planPermissionDate}"
								pattern="dd/MM/yyyy" var="planPermissionDate" />
							<c:out value="${planPermissionDate}" default="N/A"></c:out>
						</div>
					</div>
					<div class="row add-border">
						<label class="col-sm-3"><spring:message
								code="lbl.permit.cancel.doc.upload" /></label>
						<div class="col-sm-3 add-margin view-content">

							<c:set value="false" var="isDocFound"></c:set>
							<c:forEach var="doc"
								items="${permitCancel.cancelSupportDocs}" varStatus="loop">
								<c:if test="${doc.fileStoreId ne null}">
									<c:set value="true" var="isDocFound"></c:set>
									<a target="_blank"
										href="/bpa/application/downloadfile/${doc.fileStoreId}"
										data-gallery>${loop.index +1} - ${doc.fileName} </a>
									<c:if test="${!loop.last}">,</c:if>&nbsp;
							        </c:if>
							</c:forEach>
							<c:if test="${!isDocFound}">
							        No Attachment Found
							    </c:if>
						</div>
						<div class="col-sm-3 add-margin">
							<spring:message code="lbl.appln.date" />
						</div>
						<div class="col-sm-3 add-margin view-content">
							<fmt:formatDate value="${permitCancel.applicationDate}"
								pattern="dd/MM/yyyy" var="applicationDate" />
							<c:out value="${applicationDate}" default="N/A"></c:out>
						</div>
					</div>
					<div class="row add-border">
						<div class="col-sm-3 add-margin">
							<spring:message code="lbl.permit.cancel.init.reason" />
						</div>
						<div class="col-sm-9 add-margin view-content">
							<c:out value="${permitCancel.initiatorRemarks}" default="N/A"></c:out>
						</div>
					</div>
					<div class="form-group">
						<label class="col-sm-3 control-label text-right"><spring:message
								code="lbl.approver.comments" /><span class="mandatory"></span></label>
						<div class="col-sm-7 add-margin">
						<input type="hidden" name="permitCancel" value="${permitCancel.id}">
							<form:hidden path="id" value="${permitCancel.id}" />
							<form:textarea path="approverRemarks" id="approverRemarks"
								class="form-control patternvalidation"
								data-pattern="alphanumericspecialcharacters" required="required"
								maxlength="1012" cols="5" rows="4" />
							<form:errors path="approverRemarks"
								cssClass="add-margin error-msg" />
						</div>
					</div>
				</div>
			</div>
			<div class="text-center">
				<button type='submit' class='btn btn-primary'
					id="approvePermitCancel">
					<spring:message code='lbl.cancel.permit.btn' />
				</button>
				<a href='javascript:void(0)' class='btn btn-default'
					onclick='self.close()'><spring:message code='lbl.close' /></a>
			</div>
		</form:form>
	</div>
</div>

<script
	src="<cdn:url value='/resources/js/app/permit-cancel-helper.js?rnd=${app_release_no}'/> "></script>
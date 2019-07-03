/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2017>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

$(document).ready(function($) {
	
	$("#planPermissionNumber").blur(function(e){ 
		checkPermitIsUsedWithAnyOccupancyCertificateAppln();
	});
	function checkPermitIsUsedWithAnyOccupancyCertificateAppln() {
	    var isExist = false;
	    $.ajax({
	        async: false,
	        url: '/bpa/application/findby-permit-number',
	        type: "GET",
	        data : {
	        	permitNumber : $('#planPermissionNumber').val()
	        },
	        contentType: 'application/json; charset=utf-8',
	        success: function (response) {
	        	if(response)
	        		response = JSON.parse(response);
	            if(response && response.ocExists === 'true') {
	                bootbox.alert("For entered plan permission number occupancy certificate is already issued, so permit cancellation is not allowed.");
	                $('#planPermissionNumber').val('');
	                isExist = true;
	            } else {
	            	$('#application').val(response.id);
	                isExist = false;
	            }
	        },
	        error: function (response) {
	            console.log("Error occurred, when fetching application details!!!!!!!");
	        }
	    });
	    return isExist;
	}
	
	$("#permitCancelSubmit").click(function(e){ 
		if ($('#permitCancelSubmitForm').valid() && validateUploadFilesMandatory()) {
            bootbox
                .confirm({
                    message: 'Do you want to submit application for permit cancellation, are you sure ?',
                    buttons: {
                        'cancel': {
                            label: 'No',
                            className: 'btn-danger'
                        },
                        'confirm': {
                            label: 'Yes',
                            className: 'btn-primary'
                        }
                    },
                    callback: function (result) {
                        if (result) {
                            $('#permitCancelSubmitForm').trigger('submit');
                        } else {
                            e.stopPropagation();
                            e.preventDefault();
                        }
                    }
                });
        } else {
            e.preventDefault();
        }
		return false;
	});
	
	
	$("#approvePermitCancel").click(function(e){ 
		if ($('#permitCancelEditForm').valid()) {
            bootbox
                .confirm({
                    message: 'Do you want to approve permit cancellation request, are you sure ?',
                    buttons: {
                        'cancel': {
                            label: 'No',
                            className: 'btn-danger'
                        },
                        'confirm': {
                            label: 'Yes',
                            className: 'btn-primary'
                        }
                    },
                    callback: function (result) {
                        if (result) {
                            $('#permitCancelEditForm').trigger('submit');
                        } else {
                            e.stopPropagation();
                            e.preventDefault();
                        }
                    }
                });
        } else {
            e.preventDefault();
        }
		return false;
	});
	
});

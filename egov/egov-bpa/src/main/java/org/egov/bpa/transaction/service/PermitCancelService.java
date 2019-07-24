/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2018>  eGovernments Foundation
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

package org.egov.bpa.transaction.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_PERMIT_CANCELLED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_PERMIT_CANCEL_INIT;
import static org.egov.bpa.utils.BpaConstants.EGMODULE_NAME;
import static org.egov.bpa.utils.BpaConstants.NO;
import static org.egov.bpa.utils.BpaConstants.SENDEMAILFORBPA;
import static org.egov.bpa.utils.BpaConstants.SENDSMSFORBPA;
import static org.egov.bpa.utils.BpaConstants.YES;
import static org.egov.infra.utils.DateUtils.toDefaultDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.bpa.config.properties.BpaApplicationSettings;
import org.egov.bpa.master.entity.PermitCancel;
import org.egov.bpa.service.es.BpaIndexService;
import org.egov.bpa.transaction.entity.ApplicationStakeHolder;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.repository.PermitCancelRepository;
import org.egov.bpa.utils.BpaUtils;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@Transactional(readOnly = true)
public class PermitCancelService {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    @Qualifier("parentMessageSource")
    private MessageSource bpaMessageSource;
    @Autowired
    private AppConfigValueService appConfigValuesService;
    @Autowired
    private PermitCancelRepository permitCancelRepository;
    @Autowired
    private ApplicationBpaService applicationBpaService;
    @Autowired
    private BpaIndexService bpaIndexService;
    @Autowired
    private BpaApplicationSettings bpaApplicationSettings;
    @Autowired
    private BpaUtils bpaUtils;

    @Transactional
    public PermitCancel save(PermitCancel permitCancel) {
        if (permitCancel.getApplicationDate() == null)
            permitCancel.setApplicationDate(new Date());
        if (permitCancel.getFiles() != null && permitCancel.getFiles().length > 0) {
            Set<FileStoreMapper> revokeDocs = new HashSet<>();
            revokeDocs.addAll(permitCancel.getCancelSupportDocs());
            revokeDocs.addAll(applicationBpaService.addToFileStore(permitCancel.getFiles()));
            permitCancel.setCancelSupportDocs(revokeDocs);
        }
        permitCancel.getApplication()
                .setStatus(applicationBpaService.getStatusByCodeAndModuleType(APPLICATION_STATUS_PERMIT_CANCEL_INIT));
        permitCancel.getApplication().setPermitCancel(permitCancel);
        PermitCancel revokeResponse = permitCancelRepository.saveAndFlush(permitCancel);
        bpaIndexService.updateIndexes(revokeResponse.getApplication());
        return revokeResponse;
    }

    @Transactional
    public PermitCancel update(PermitCancel permitCancel) {
        permitCancel.setCancellationDate(new Date());
        permitCancel.getApplication()
                .setStatus(
                        applicationBpaService.getStatusByCodeAndModuleType(APPLICATION_STATUS_PERMIT_CANCELLED));
        PermitCancel revokeResponse = permitCancelRepository.save(permitCancel);
        bpaIndexService.updateIndexes(revokeResponse.getApplication());
        return revokeResponse;
    }

    public PermitCancel findByApplicationPlanPermissionNumber(final String permitNumber) {
        return permitCancelRepository.findByApplicationPlanPermissionNumber(permitNumber);
    }

    public void sendSmsAndEmail(final PermitCancel permitCancel) {
        String mobileNo;
        String email;
        String applicantName;
        if (isSmsEnabled() || isEmailEnabled()) {
            ApplicationStakeHolder applnStakeHolder = permitCancel.getApplication().getStakeHolder().get(0);
            if (applnStakeHolder.getApplication() != null && applnStakeHolder.getApplication().getOwner() != null) {
                applicantName = applnStakeHolder.getApplication().getOwner().getName();
                email = applnStakeHolder.getApplication().getOwner().getEmailId();
                mobileNo = applnStakeHolder.getApplication().getOwner().getUser().getMobileNumber();
                buildSmsAndEmail(permitCancel, mobileNo, email, applicantName);
            }
            if (applnStakeHolder.getStakeHolder() != null && applnStakeHolder.getStakeHolder().getIsActive()) {
                applicantName = applnStakeHolder.getStakeHolder().getName();
                email = applnStakeHolder.getStakeHolder().getEmailId();
                mobileNo = applnStakeHolder.getStakeHolder().getMobileNumber();
                buildSmsAndEmail(permitCancel, mobileNo, email, applicantName);
            }
        }

    }

    private void buildSmsAndEmail(PermitCancel permitCancel, String mobileNo, String email, String applicantName) {
        String smsMsg = "";
        String body = "";
        String subject = "";
        BpaApplication application = permitCancel.getApplication();
        if (APPLICATION_STATUS_PERMIT_CANCEL_INIT.equals(application.getStatus().getCode())) {
            smsMsg = bpaMessageSource.getMessage("msg.permit.cancel.initiate.sms",
                    new String[] { application.getPlanPermissionNumber() },
                    null);
            body = bpaMessageSource.getMessage("msg.permit.cancel.initiate.email.body",
                    new String[] { applicantName, application.getPlanPermissionNumber(),
                            toDefaultDateFormat(application.getPlanPermissionDate()),
                            ApplicationThreadLocals.getMunicipalityName() },
                    null);
            subject = bpaMessageSource.getMessage("msg.permit.cancel.initiate.email.subject",
                    new String[] { application.getPlanPermissionNumber() }, null);

        } else if (APPLICATION_STATUS_PERMIT_CANCELLED
                .equals(application.getStatus().getCode())) {
            smsMsg = bpaMessageSource.getMessage("msg.permit.cancel.approve.sms",
                    new String[] { application.getPlanPermissionNumber(),
                            toDefaultDateFormat(application.getPlanPermissionDate()),
                            toDefaultDateFormat(permitCancel.getCancellationDate()) },
                    null);
            body = bpaMessageSource.getMessage("msg.permit.cancel.approve.email.body",
                    new String[] { applicantName, application.getPlanPermissionNumber(),
                            toDefaultDateFormat(application.getPlanPermissionDate()),
                            toDefaultDateFormat(permitCancel.getCancellationDate()),
                            ApplicationThreadLocals.getMunicipalityName() },
                    null);
            subject = bpaMessageSource.getMessage("msg.permit.cancel.approve.email.subject",
                    new String[] { application.getPlanPermissionNumber() }, null);
        }
        if (isNotBlank(mobileNo) && isNotBlank(smsMsg))
            notificationService.sendSMS(mobileNo, smsMsg);
        if (isNotBlank(email) && isNotBlank(body))
            notificationService.sendEmail(email, subject, body);
    }

    public Boolean isSmsEnabled() {
        return getAppConfigValueByPassingModuleAndType(EGMODULE_NAME, SENDSMSFORBPA);
    }

    public Boolean getAppConfigValueByPassingModuleAndType(String moduleName, String sendsmsoremail) {
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(moduleName,
                sendsmsoremail);
        return YES.equalsIgnoreCase(
                appConfigValue != null && !appConfigValue.isEmpty() ? appConfigValue.get(0).getValue() : NO);
    }

    public Boolean isEmailEnabled() {
        return getAppConfigValueByPassingModuleAndType(EGMODULE_NAME, SENDEMAILFORBPA);
    }

    public void validateDocs(final PermitCancel permitCancel, final BindingResult errors) {
        List<String> pcDocAllowedExtenstions = new ArrayList<String>(
                Arrays.asList(bpaApplicationSettings.getValue("bpa.permit.cancellation.docs.allowed.extenstions").split(",")));

        List<String> pcDocMimeTypes = new ArrayList<String>(
                Arrays.asList(bpaApplicationSettings.getValue("bpa.permit.cancellation.docs.allowed.mime.types").split(",")));

        bpaUtils.validateFiles(errors, pcDocAllowedExtenstions, pcDocMimeTypes, permitCancel.getFiles(),
                "files");

    }

}

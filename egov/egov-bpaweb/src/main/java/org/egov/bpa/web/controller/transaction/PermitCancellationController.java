package org.egov.bpa.web.controller.transaction;

import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_PERMIT_CANCELLED;

import javax.validation.Valid;

import org.egov.bpa.config.properties.BpaApplicationSettings;
import org.egov.bpa.master.entity.PermitCancel;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.service.ApplicationBpaService;
import org.egov.bpa.transaction.service.PermitCancelService;
import org.egov.bpa.utils.BpaUtils;
import org.egov.infra.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/application")
public class PermitCancellationController {

    private static final String PERMIT_CANCEL = "permitCancel";
    private static final String MESSAGE = "message";
    public static final String COMMON_ERROR = "common-error";
    private static final String APPLICATION_SUCCESS = "application-success";

    @Autowired
    private PermitCancelService permitCancelService;
    @Autowired
    private ApplicationBpaService applicationBpaService;
    @Autowired
    protected ResourceBundleMessageSource messageSource;
    @Autowired
    private BpaUtils bpaUtils;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private BpaApplicationSettings bpaApplicationSettings;

    @GetMapping("/citizen/cancel-permit/new")
    public String showCancelPermitForm(final Model model) {
        model.addAttribute("currentUser", securityUtils.getCurrentUser());
        model.addAttribute(PERMIT_CANCEL, new PermitCancel());
        model.addAttribute("pcDocAllowedExtenstions",
                bpaApplicationSettings.getValue("bpa.permit.cancellation.docs.allowed.extenstions"));
        model.addAttribute("pcDocMaxSize", bpaApplicationSettings.getValue("bpa.permit.cancellation.docs.max.size"));
        return "permit-cancel-submit";
    }

    @PostMapping("/citizen/cancel-permit/create")
    public String submitCancelPermit(@Valid @ModelAttribute PermitCancel permitCancel, final Model model) {
        permitCancelService.save(permitCancel);
        bpaUtils.updatePortalUserinbox(permitCancel.getApplication(), null);
        permitCancelService.sendSmsAndEmail(permitCancel);
        model.addAttribute(MESSAGE, messageSource.getMessage("msg.permit.cancel.init",
                new String[] { permitCancel.getApplication().getPlanPermissionNumber() }, LocaleContextHolder.getLocale()));
        return APPLICATION_SUCCESS;
    }

    @GetMapping("/permit/cancellation/edit/{planPermissionNumber}")
    public String editPermitCancellation(final Model model, @PathVariable final String planPermissionNumber) {
        BpaApplication application = applicationBpaService.findByPermitNumber(planPermissionNumber);
        if (application != null && APPLICATION_STATUS_PERMIT_CANCELLED.equals(application.getStatus().getCode())) {
            model.addAttribute(MESSAGE, "Permit cancellation process is completed for the selected application.");
            return COMMON_ERROR;
        }
        model.addAttribute(PERMIT_CANCEL, permitCancelService.findByApplicationPlanPermissionNumber(planPermissionNumber));
        return "permit-cancel-edit";
    }

    @PostMapping("/permit/cancellation/approve")
    public String approvePermitCancellation(@Valid @ModelAttribute PermitCancel permitCancel, final Model model) {
        permitCancelService.update(permitCancel);
        bpaUtils.updatePortalUserinbox(permitCancel.getApplication(), null);
        permitCancelService.sendSmsAndEmail(permitCancel);
        model.addAttribute(MESSAGE, messageSource.getMessage("msg.permit.cancel.approve",
                new String[] { permitCancel.getApplication().getPlanPermissionNumber() }, LocaleContextHolder.getLocale()));
        return APPLICATION_SUCCESS;
    }

}

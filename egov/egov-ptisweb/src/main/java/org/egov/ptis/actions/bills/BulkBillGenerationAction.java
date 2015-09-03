/*******************************************************************************
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
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
 * 	1) All versions of this program, verbatim or modified must carry this
 * 	   Legal Notice.
 *
 * 	2) Any misrepresentation of the origin of the material is prohibited. It
 * 	   is required that all modified versions of this material be marked in
 * 	   reasonable ways as different from the original version.
 *
 * 	3) This license does not grant any rights to any user of the program
 * 	   with regards to rights under trademark law for use of the trade names
 * 	   or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 ******************************************************************************/
package org.egov.ptis.actions.bills;

import static org.egov.ptis.constants.PropertyTaxConstants.ADMIN_HIERARCHY_TYPE;
import static org.egov.ptis.constants.PropertyTaxConstants.WARD_BNDRY_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.egov.commons.Installment;
import org.egov.commons.dao.InstallmentDao;
import org.egov.exceptions.EGOVRuntimeException;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.Module;
import org.egov.infra.admin.master.service.BoundaryService;
import org.egov.infra.admin.master.service.ModuleService;
import org.egov.infra.web.struts.actions.BaseFormAction;
import org.egov.infra.web.struts.annotation.ValidationErrorPage;
import org.egov.ptis.actions.common.CommonServices;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.domain.entity.demand.BulkBillGeneration;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("serial")
@ParentPackage("egov")
@Results({ @Result(name = BulkBillGenerationAction.NEW, location = "bulkBillGeneration-new.jsp"),
    @Result(name = BulkBillGenerationAction.RESULT_ACK, location = "bulkBillGeneration-ack.jsp")
})
public class BulkBillGenerationAction extends BaseFormAction {

    /**
     *
     */
    private static final long serialVersionUID = -4113611719476196791L;
    Logger LOGGER = Logger.getLogger(getClass());
    public static final String RESULT_ACK = "ack";
    private Long zoneId;
    private Long wardId;
    private Map<Long, String> ZoneBndryMap;
    private String ackMessage;
    @Autowired
    private InstallmentDao installmentDao;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private BoundaryService boundaryService;
    private List<Boundary> wardList = new ArrayList<Boundary>();

    @Override
    public Object getModel() {
        return null;
    }

    @SkipValidation
    @Action(value = "/bills/bulkBillGeneration-newForm")
    public String newForm() {
        return NEW;
    }

    @Override
    public void prepare() {
        try {
            LOGGER.debug("Entered into prepare method");
            LOGGER.debug("Zone id : " + zoneId + ", " + "Ward id : " + wardId);
            final List<Boundary> zoneList = getPersistenceService().findAllBy(
                    "from Boundary BI where BI.boundaryType.name=? and BI.boundaryType.hierarchyType.name=? "
                            + "and BI.isHistory='N' order by BI.id", "Zone", ADMIN_HIERARCHY_TYPE);
            setZoneBndryMap(CommonServices.getFormattedBndryMap(zoneList));
            prepareWardDropDownData(zoneId != null, wardId != null);
            LOGGER.debug("Exit from prepare method");
        } catch (final Exception e) {
            throw new EGOVRuntimeException("Bill Generation Exception : " + e);
        }
    }

    @SuppressWarnings("unchecked")
    @SkipValidation
    private void prepareWardDropDownData(final boolean zoneExists, final boolean wardExists) {
        LOGGER.debug("Entered into prepareWardDropDownData method");
        LOGGER.debug("Zone exists ? : " + zoneExists + ", " + "Ward exists ? : " + wardExists);
        if (zoneExists && wardExists) {
            List<Boundary> wardNewList = new ArrayList<Boundary>();
            wardNewList = getPersistenceService()
                    .findAllBy(
                            "from Boundary BI where BI.boundaryType.name=? and BI.parent.id = ? and BI.isHistory='N' order by BI.id ",
                            WARD_BNDRY_TYPE, getZoneId());
            addDropdownData("wardList", wardNewList);
        } else
            addDropdownData("wardList", Collections.EMPTY_LIST);
        LOGGER.debug("Exit from prepareWardDropDownData method");
    }

    @Override
    public void validate() {
        LOGGER.debug("Entered into validate method");
        if (zoneId == null || zoneId == -1)
            addActionError(getText("mandatory.zone"));
        LOGGER.debug("Exit from validate method");
    }

    @ValidationErrorPage(value = "new")
    @Action(value = "/bills/bulkBillGeneration-generateBills")
    public String generateBills() {
        LOGGER.debug("generateBills method started for zone " + zoneId + " and ward number :" + wardId);
        BulkBillGeneration bulkBill = null;
        String wardMessage = "";
        final Module module = moduleService.getModuleByName(PropertyTaxConstants.PTMODULENAME);
        final Installment currentInstall = installmentDao.getInsatllmentByModuleForGivenDate(module, new Date());

        final StringBuilder queryStr = new StringBuilder();
        queryStr.append("select bbg from BulkBillGeneration bbg ").append(
                " where bbg.zone.id=:zoneid and bbg.installment.id=:installment ");
        if (wardId != null && wardId != -1)
            queryStr.append("and bbg.ward.id=:wardid ");
        final Query query = getPersistenceService().getSession().createQuery(queryStr.toString());
        query.setLong("zoneid", zoneId);
        query.setLong("installment", currentInstall.getId());
        if (wardId != null && wardId != -1)
            query.setLong("wardid", wardId);
        final List<BulkBillGeneration> bbgList = query.list();
        if (bbgList != null && !bbgList.isEmpty())
            bulkBill = bbgList.get(0);

        if (bulkBill == null) {
            bulkBill = new BulkBillGeneration();
            bulkBill.setZone(boundaryService.getBoundaryById(zoneId));
            bulkBill.setWard(boundaryService.getBoundaryById(wardId));
            bulkBill.setInstallment(currentInstall);
            persistenceService.setType(BulkBillGeneration.class);
            getPersistenceService().persist(bulkBill);
            if (wardId != null && wardId != -1)
                wardMessage = ", Ward " + bulkBill.getWard().getName();
            setAckMessage("Bill generation scheduled for zone " + bulkBill.getZone().getName() + wardMessage
                    + " and for Installment " + currentInstall.getDescription()
                    + ", you can check the bill generation status using ");
        } else {
            if (wardId != null && wardId != -1)
                wardMessage = ", Ward " + bulkBill.getWard().getName();
            setAckMessage("Bill generation already scheduled for zone " + bulkBill.getZone().getName() + wardMessage
                    + " and for Installment " + currentInstall.getDescription()
                    + ", you can check the bill generation status after some time using ");
        }
        LOGGER.debug("generateBills method started for zone " + zoneId + " and ward number :" + wardId);
        return RESULT_ACK;
    }

    public List<Boundary> getWardList() {
        return wardList;
    }

    public void setWardList(final List<Boundary> wardList) {
        this.wardList = wardList;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(final Long wardId) {
        this.wardId = wardId;
    }

    public String getAckMessage() {
        return ackMessage;
    }

    public void setAckMessage(final String ackMessage) {
        this.ackMessage = ackMessage;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final Long zoneId) {
        this.zoneId = zoneId;
    }

    public Map<Long, String> getZoneBndryMap() {
        return ZoneBndryMap;
    }

    public void setZoneBndryMap(final Map<Long, String> zoneBndryMap) {
        ZoneBndryMap = zoneBndryMap;
    }
}

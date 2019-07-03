/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2018>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.bpa.transaction.service.oc;

import static org.egov.bpa.utils.BpaConstants.SCALING_FACTOR;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.bpa.transaction.entity.ApplicationFloorDetail;
import org.egov.bpa.transaction.entity.BuildingDetail;
import org.egov.bpa.transaction.entity.oc.OCBuilding;
import org.egov.bpa.transaction.entity.oc.OCFloor;
import org.egov.bpa.transaction.entity.oc.OccupancyCertificate;
import org.egov.bpa.utils.BpaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class OccupancyCertificateValidationService {

    private static final String OC_COMPARISON_VALIDATION = "OcComparisonValidation";

    public static final String CITIZEN_OCCUPANCY_CERTIFICATE_NEW = "citizen-occupancy-certificate-new";

    @Autowired
    private BpaUtils bpaUtils;
    @Autowired
    @Qualifier("parentMessageSource")
    private MessageSource messageSource;

    /**
     *
     * @param model
     * @param occupancyCertificate
     * @return
     */
    public String validateOcWithBpaApplication(final Model model, final OccupancyCertificate occupancyCertificate) {

        // 1.Validate plot area
        double ocPlotArea = occupancyCertificate.getExtentInSqmts().setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        double permitPlotArea = occupancyCertificate.getParent().getSiteDetail().get(0).getExtentinsqmts()
                .setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (ocPlotArea > permitPlotArea) {
            model.addAttribute(OC_COMPARISON_VALIDATION,
                    messageSource.getMessage("msg.oc.comp.plot.area.more",
                            new String[] { String.valueOf(ocPlotArea), String.valueOf(permitPlotArea) },
                            LocaleContextHolder.getLocale()));
            return CITIZEN_OCCUPANCY_CERTIFICATE_NEW;
        }

        // 2.check number of blocks,block number same or not
        List<OCBuilding> ocBuildings = occupancyCertificate.getBuildings();
        List<BuildingDetail> permitBuildings = occupancyCertificate.getParent().getBuildingDetail();
        int ocExistingBldgSize = occupancyCertificate.getExistingBuildings().size();
        int permitExistingBldgSize = occupancyCertificate.getParent().getExistingBuildingDetails().size();
        int totalOcBldgs = ocBuildings.size() + ocExistingBldgSize;
        int totalPermitBldgs = permitBuildings.size() + permitExistingBldgSize;
        // Validate block's count is same
        if (totalOcBldgs > totalPermitBldgs) {
            model.addAttribute(OC_COMPARISON_VALIDATION,
                    messageSource.getMessage("msg.oc.comp.blks.count.not.match",
                            new String[] { String.valueOf(totalOcBldgs), String.valueOf(totalPermitBldgs) },
                            LocaleContextHolder.getLocale()));
            return CITIZEN_OCCUPANCY_CERTIFICATE_NEW;
        }

        /*
         * // Validate block's number is same for (OCBuilding oc : ocBuildings) { boolean buildingExist = false; Integer
         * permitBldgNo = 0; for (BuildingDetail bpa : permitBuildings) { permitBldgNo = bpa.getNumber(); if
         * (oc.getBuildingNumber().equals(bpa.getNumber())) buildingExist = true; } if (!buildingExist) {
         * model.addAttribute(OC_COMPARISON_VALIDATION, messageSource.getMessage("msg.oc.comp.blk.no.not.match", new String[] {
         * String.valueOf(oc.getBuildingNumber()), String.valueOf(permitBldgNo) }, LocaleContextHolder.getLocale())); return
         * CITIZEN_OCCUPANCY_CERTIFICATE_NEW; } }
         */

        // 3.Building height is same or not
        for (OCBuilding oc : ocBuildings) {
            boolean isHeightSame = false;
            BigDecimal permitBldgHgt = BigDecimal.ZERO;
            for (BuildingDetail bpa : permitBuildings) {
                permitBldgHgt = bpa.getHeightFromGroundWithOutStairRoom().setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP);
                if (oc.getHeightFromGroundWithOutStairRoom().setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP)
                        .doubleValue() <= permitBldgHgt.doubleValue())
                    isHeightSame = true;
            }
            if (!isHeightSame) {
                model.addAttribute(OC_COMPARISON_VALIDATION,
                        messageSource.getMessage("msg.oc.comp.blk.hgt.not.match",
                                new String[] {
                                        String.valueOf(oc.getHeightFromGroundWithOutStairRoom().setScale(SCALING_FACTOR,
                                                BigDecimal.ROUND_HALF_UP)),
                                        String.valueOf(permitBldgHgt) },
                                LocaleContextHolder.getLocale()));
                return CITIZEN_OCCUPANCY_CERTIFICATE_NEW;
            }
        }

        // 4.check the floor count same or not
        for (OCBuilding oc : ocBuildings) {
            int ocFloorsCount = oc.getFloorDetailsForUpdate().size();
            for (BuildingDetail bpa : permitBuildings) {
                int permitFloorsCount = bpa.getApplicationFloorDetails().size();
                if (oc.getBuildingNumber().equals(bpa.getNumber()) && ocFloorsCount > permitFloorsCount) {
                    model.addAttribute(OC_COMPARISON_VALIDATION,
                            messageSource.getMessage("msg.oc.comp.blk.flr.count.not.match",
                                    new String[] { String.valueOf(oc.getBuildingNumber()), String.valueOf(ocFloorsCount),
                                            String.valueOf(bpa.getNumber()), String.valueOf(permitFloorsCount) },
                                    LocaleContextHolder.getLocale()));
                    return CITIZEN_OCCUPANCY_CERTIFICATE_NEW;
                }
            }
        }

        BigDecimal hundred = new BigDecimal(100);
        BigDecimal percent = new BigDecimal(bpaUtils.getAppConfigForOcAllowDeviation());

        // 5.check floor wise occupancy
        for (OCBuilding oc : ocBuildings) {
            Map<Integer, List<String>> ocFloorDtls = oc.getFloorDetailsForUpdate().stream()
                    .collect(Collectors.groupingBy(OCFloor::getFloorNumber,
                            Collectors.mapping(ocFloor -> ocFloor.getOccupancy().getDescription(), Collectors.toList())));
            for (BuildingDetail bpa : permitBuildings) {
                Map<Integer, List<String>> permitFloorDtls = bpa.getApplicationFloorDetails().stream()
                        .collect(Collectors.groupingBy(ApplicationFloorDetail::getFloorNumber,
                                Collectors.mapping(bpaFloor -> bpaFloor.getOccupancy().getDescription(), Collectors.toList())));
                if (oc.getBuildingNumber().equals(bpa.getNumber()))
                    for (Map.Entry<Integer, List<String>> ocFloor : ocFloorDtls.entrySet())
                        for (Map.Entry<Integer, List<String>> bpaFloor : permitFloorDtls.entrySet())
                            if (ocFloor.getKey().equals(bpaFloor.getKey()))
                                for (String ocOccupancyType : ocFloor.getValue())
                                    if (!bpaFloor.getValue().contains(ocOccupancyType)) {
                                        model.addAttribute(OC_COMPARISON_VALIDATION,
                                                messageSource.getMessage("msg.oc.comp.blk.flr.occupancy.not.match", new String[] {
                                                        String.valueOf(oc.getBuildingNumber()), String.valueOf(ocFloor.getKey()),
                                                        ocOccupancyType, String.valueOf(bpa.getNumber()),
                                                        String.valueOf(bpaFloor.getKey()),
                                                        bpaFloor.getValue().stream().map(String::new)
                                                                .collect(Collectors.joining(",")) },
                                                        LocaleContextHolder.getLocale()));
                                        return CITIZEN_OCCUPANCY_CERTIFICATE_NEW;
                                    }
            }
        }

        // 6.check block wise floor area
        BigDecimal limitSqurMtrs = new BigDecimal(40);
        for (OCBuilding oc : ocBuildings)
            for (BuildingDetail bpa : permitBuildings)
                if (oc.getBuildingNumber().equals(bpa.getNumber())) {
                    BigDecimal totalOcFloor = getOcTotalFloorArea(oc.getFloorDetailsForUpdate()).setScale(SCALING_FACTOR,
                            BigDecimal.ROUND_HALF_UP);
                    BigDecimal totalBpaFloor = getBpaTotalFloorArea(bpa.getApplicationFloorDetails()).setScale(SCALING_FACTOR,
                            BigDecimal.ROUND_HALF_UP);
                    BigDecimal allowDeviation = totalBpaFloor.multiply(percent).divide(hundred);
                    BigDecimal totalBpaWithAllowDeviation = totalBpaFloor.add(allowDeviation);
                    if (totalBpaWithAllowDeviation.compareTo(totalOcFloor) < 0) {
                        model.addAttribute(OC_COMPARISON_VALIDATION,
                                messageSource.getMessage("msg.oc.comp.blk.area.not.match1",
                                        new String[] { String.valueOf(oc.getBuildingNumber()), String.valueOf(totalOcFloor),
                                                String.valueOf(bpa.getNumber()), String.valueOf(totalBpaFloor) },
                                        LocaleContextHolder.getLocale()));
                        return CITIZEN_OCCUPANCY_CERTIFICATE_NEW;
                    }
                    if (totalOcFloor.subtract(totalBpaFloor).compareTo(limitSqurMtrs) > 0) {
                        model.addAttribute(OC_COMPARISON_VALIDATION,
                                messageSource.getMessage("msg.oc.comp.blk.area.not.match2",
                                        new String[] { String.valueOf(oc.getBuildingNumber()), String.valueOf(totalOcFloor),
                                                String.valueOf(bpa.getNumber()), String.valueOf(totalBpaFloor) },
                                        LocaleContextHolder.getLocale()));
                        return CITIZEN_OCCUPANCY_CERTIFICATE_NEW;
                    }
                }
        return "";
    }

    private BigDecimal getOcTotalFloorArea(List<OCFloor> floorList) {
        BigDecimal totalFloorArea = BigDecimal.ZERO;
        for (OCFloor floorDetail : floorList)
            totalFloorArea = totalFloorArea.add(floorDetail.getFloorArea());
        return totalFloorArea;
    }

    private BigDecimal getBpaTotalFloorArea(List<ApplicationFloorDetail> floorList) {
        BigDecimal totalFloorArea = BigDecimal.ZERO;
        for (ApplicationFloorDetail floorDetail : floorList)
            totalFloorArea = totalFloorArea.add(floorDetail.getFloorArea());
        return totalFloorArea;
    }

}

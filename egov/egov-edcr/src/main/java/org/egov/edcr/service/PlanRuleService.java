package org.egov.edcr.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.repository.PlanRuleRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlanRuleService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlanRuleRepository planRuleRepository;

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public List<PlanRule> findRulesByPlanDetail(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        PlanRule planRule = new PlanRule();
        StringBuffer planRules = new StringBuffer();
        planRules.append("23"); // Default rule to be add.// overheadline and other
        BigDecimal plotArea;
        if (planDetail != null && planDetail.getPlanInformation() != null
                && planDetail.getPlanInformation().getPlotArea() != null) {
            plotArea = planDetail.getPlanInformation().getPlotArea();

            if (plotArea.compareTo(BigDecimal.valueOf(125)) <= 0) // Plot area less than 125
            {

                planRules.append(",26");// construction abuting to public road
                planRules.append(",30");
                planRules.append(",61");
                planRules.append(",62");
                planRules.append(",63");
                planRules.append(",64");

            } else {
                planRules.append(",24"); // interior or exterior
                planRules.append(",25");// distance from road
                planRules.append(",26");
                planRules.append(",30");
                planRules.append(",31");// coverage and FAR

                if (planDetail.getBuilding().getMaxFloor() != null
                        && planDetail.getBuilding().getMaxFloor().compareTo(BigDecimal.valueOf(3)) > 0)
                    // if floor greater than 3 floor validate.
                    planRules.append(",32"); // Height of building
                else {
                    // Append rule from 35 to 50.
                }

                planRules.append(",33");// access to plot
                planRules.append(",34");// Parking

            }

        }
        planRule.setRules(planRules.toString());
        List<PlanRule> rules = new ArrayList<>();
        rules.add(planRule);
        return rules;
        // BASED ON PLAN DETAIL, GET DIFFERENT RULES.
        /*
         * final Criteria criteria = buildSearchCriteria(planDetail); return criteria.list();
         */
    }

    public List<PlanRule> findAll() {
        return planRuleRepository.findAll();
    }

    public PlanRule findId(Long id) {
        return planRuleRepository.findOne(id);
    }

}

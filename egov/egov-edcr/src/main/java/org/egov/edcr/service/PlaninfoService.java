package org.egov.edcr.service;

import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.repository.PlanInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlaninfoService {

    @Autowired
    private PlanInfoRepository planInfoRepository;

    public PlanInformation save(PlanInformation planInformation) {
        return planInfoRepository.save(planInformation);
    }

}

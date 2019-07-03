package org.egov.edcr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.edcr.entity.EdcrPdfDetail;
import org.egov.edcr.repository.EdcrPdfDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EdcrPdfDetailService {

    @Autowired
    private EdcrPdfDetailRepository edcrPdfDetailRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public void save(EdcrPdfDetail edcrPdfDetail) {
        edcrPdfDetailRepository.save(edcrPdfDetail);
    }

    public void saveAll(List<EdcrPdfDetail> edcrPdfDetails) {
        edcrPdfDetailRepository.save(edcrPdfDetails);
    }

    public List<EdcrPdfDetail> findByDcrApplicationId(Long applicationDetailId) {
        return edcrPdfDetailRepository.findByEdcrApplicationDetailId(applicationDetailId);
    }

}

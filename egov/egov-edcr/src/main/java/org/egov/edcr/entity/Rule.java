package org.egov.edcr.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.validator.constraints.SafeHtml;

@Entity
@Table(name = "EDCR_RULE")
@SequenceGenerator(name = Rule.SEQ_EDCR_RULE, sequenceName = Rule.SEQ_EDCR_RULE, allocationSize = 1)
public class Rule extends AbstractAuditable {

    private static final long serialVersionUID = -167432164622772634L;

    public static final String SEQ_EDCR_RULE = "SEQ_EDCR_RULE";

    @Id
    @GeneratedValue(generator = SEQ_EDCR_RULE, strategy = GenerationType.SEQUENCE)
    private Long id;
    @SafeHtml
    @NotNull
    private String name;
    @SafeHtml
    @NotNull
    private String clause;

    @NotNull
    private Boolean active;

    @OneToMany(mappedBy = "rule", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubRule> subRules;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClause() {
        return clause;
    }

    public void setClause(String clause) {
        this.clause = clause;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<SubRule> getSubRules() {
        return subRules;
    }

    public void setSubRules(List<SubRule> subRules) {
        this.subRules = subRules;
    }
}

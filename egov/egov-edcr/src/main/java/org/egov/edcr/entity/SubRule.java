package org.egov.edcr.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.SafeHtml;

@Entity
@Table(name = "EDCR_SUBRULE")
@SequenceGenerator(name = SubRule.SEQ_EDCR_SUBRULE, sequenceName = SubRule.SEQ_EDCR_SUBRULE, allocationSize = 1)
public class SubRule extends AbstractAuditable {

    private static final long serialVersionUID = -1893529596612908357L;

    public static final String SEQ_EDCR_SUBRULE = "SEQ_EDCR_SUBRULE";

    @Id
    @GeneratedValue(generator = SEQ_EDCR_SUBRULE, strategy = GenerationType.SEQUENCE)
    private Long id;
    @SafeHtml
    @NotNull
    private String name;
    @SafeHtml
    @NotNull
    private String clause;

    private Long orderBy;

    @NotNull
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "rule")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Rule rule;

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

    public Long getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Long orderBy) {
        this.orderBy = orderBy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}

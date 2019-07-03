package org.egov.edcr.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ar.com.fdvs.dj.domain.constants.HorizontalAlign;

public class ScrutinyDetail implements Serializable {
    private static final long serialVersionUID = -788830650510383907L;
    private String key;
    private String heading;
    private String remarks;
    private String subHeading;
    /**
     * Do not add heading dynamically. It should be a static text in your process API.
     */
    private Map<Integer, ColumnHeadingDetail> columnHeading = new TreeMap<>();
    private List<Map<String, String>> detail = new ArrayList<>();

    public class ColumnHeadingDetail implements Serializable {
        private static final long serialVersionUID = 2446433602892212662L;
        public String name;
        public HorizontalAlign align;

    }

    public String getKey() {
        return key;
    }

    public List<Map<String, String>> getDetail() {
        return detail;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDetail(List<Map<String, String>> detail) {
        this.detail = detail;
    }

    public void addDetail(Map<String, String> det) {
        detail.add(det);
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSubHeading() {
        return subHeading;
    }

    public void setSubHeading(String subHeading) {
        this.subHeading = subHeading;
    }

    public Map<Integer, ColumnHeadingDetail> getColumnHeading() {
        return columnHeading;
    }

    public void setColumnHeading(Map<Integer, ColumnHeadingDetail> columnHeading) {
        this.columnHeading = columnHeading;
    }

    public void addColumnHeading(Integer orderNo, String heading, HorizontalAlign align) {
        ColumnHeadingDetail colHeadingDtl = new ColumnHeadingDetail();
        if (align != null)
            colHeadingDtl.align = align;
        colHeadingDtl.name = heading;
        columnHeading.put(orderNo, colHeadingDtl);
    }

    public void addColumnHeading(Integer orderNo, String heading) {
        ColumnHeadingDetail colHeadingDtl = new ColumnHeadingDetail();
        colHeadingDtl.align = HorizontalAlign.LEFT;
        colHeadingDtl.name = heading;
        columnHeading.put(orderNo, colHeadingDtl);
    }

}
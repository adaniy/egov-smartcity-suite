package org.egov.edcr.entity.utility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class BlockDistances implements Serializable {
    private static final long serialVersionUID = 6L;
    private String blockNumber;

    private List<BigDecimal> distances;

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public List<BigDecimal> getDistances() {
        return distances;
    }

    public void setDistances(List<BigDecimal> distances) {
        this.distances = distances;
    }
}

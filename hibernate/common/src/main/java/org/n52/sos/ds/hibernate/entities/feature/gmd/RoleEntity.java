package org.n52.sos.ds.hibernate.entities.feature.gmd;

import org.n52.sos.ds.hibernate.entities.feature.NilReasonEntity;

/**
 * Hibernate entity for role.
 * 
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.4.0
 *
 */
public class RoleEntity extends NilReasonEntity {

    private String codeList;
    private String codeListValue;

    /**
     * @return the codeList
     */
    public String getCodeList() {
        return codeList;
    }

    /**
     * @param codeList
     *            the codeList to set
     */
    public void setCodeList(String codeList) {
        this.codeList = codeList;
    }

    /**
     * @return the codeListValue
     */
    public String getCodeListValue() {
        return codeListValue;
    }

    /**
     * @param codeListValue
     *            the codeListValue to set
     */
    public void setCodeListValue(String codeListValue) {
        this.codeListValue = codeListValue;
    }
}
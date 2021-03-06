/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.model.catalog;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.meveo.model.AuditableEntity;

/**
 * Price plan matrix entity used when rating transactions.
 * 
 * @author Ignas Lelys
 * @created Nov 28, 2010
 * 
 */
@Entity
@Table(name = "CAT_PRICE_PLAN_MATRIX")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "CAT_PRICE_PLAN_MATRIX_SEQ")
public class PricePlanMatrix extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "EVENT_CODE", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    @NotEmpty
    private String eventCode;

    @Column(name = "START_SUBSCRIPTION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startSubscriptionDate;

    @Column(name = "END_SUBSCRIPTION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endSubscriptionDate;

    @Column(name = "START_RATING_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startRatingDate;

    @Column(name = "END_RATING_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endRatingDate;

    @Column(name = "MIN_SUBSCR_AGE")
    private Long minSubscriptionAgeInMonth;

    @Column(name = "MAX_SUBSCR_AGE")
    private Long maxSubscriptionAgeInMonth;

    @Column(name = "CRITERIA_1")
    private String criteria1Value;

    @Column(name = "CRITERIA_2")
    private String criteria2Value;

    @Column(name = "CRITERIA_3")
    private String criteria3Value;

    @Column(name = "AMOUNT_WITHOUT_TAX", precision = 23, scale = 12)
    @Digits(integer = 23, fraction = 12)
    private BigDecimal amountWithoutTax;

    /** Should be called extraAmount because thats what this field is. */
    @Column(name = "AMOUNT_WITHOUT_TAX2", precision = 23, scale = 12)
    @Digits(integer = 23, fraction = 12)
    private BigDecimal amountWithoutTax2;

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public Date getStartSubscriptionDate() {
        return startSubscriptionDate;
    }

    public void setStartSubscriptionDate(Date startSubscriptionDate) {
        this.startSubscriptionDate = startSubscriptionDate;
    }

    public Date getEndSubscriptionDate() {
        return endSubscriptionDate;
    }

    public void setEndSubscriptionDate(Date endSubscriptionDate) {
        this.endSubscriptionDate = endSubscriptionDate;
    }

    public Date getStartRatingDate() {
        return startRatingDate;
    }

    public void setStartRatingDate(Date startRatingDate) {
        this.startRatingDate = startRatingDate;
    }

    public Date getEndRatingDate() {
        return endRatingDate;
    }

    public void setEndRatingDate(Date endRatingDate) {
        this.endRatingDate = endRatingDate;
    }

    public Long getMinSubscriptionAgeInMonth() {
        return minSubscriptionAgeInMonth;
    }

    public void setMinSubscriptionAgeInMonth(Long minSubscriptionAgeInMonth) {
        this.minSubscriptionAgeInMonth = minSubscriptionAgeInMonth;
    }

    public Long getMaxSubscriptionAgeInMonth() {
        return maxSubscriptionAgeInMonth;
    }

    public void setMaxSubscriptionAgeInMonth(Long maxSubscriptionAgeInMonth) {
        this.maxSubscriptionAgeInMonth = maxSubscriptionAgeInMonth;
    }

    public String getCriteria1Value() {
        return criteria1Value;
    }

    public void setCriteria1Value(String criteria1Value) {
        this.criteria1Value = criteria1Value;
    }

    public String getCriteria2Value() {
        return criteria2Value;
    }

    public void setCriteria2Value(String criteria2Value) {
        this.criteria2Value = criteria2Value;
    }

    public String getCriteria3Value() {
        return criteria3Value;
    }

    public void setCriteria3Value(String criteria3Value) {
        this.criteria3Value = criteria3Value;
    }

    public BigDecimal getAmountWithoutTax() {
        return amountWithoutTax;
    }

    public void setAmountWithoutTax(BigDecimal amountWithoutTax) {
        this.amountWithoutTax = amountWithoutTax;
    }

    public BigDecimal getAmountWithoutTax2() {
        return amountWithoutTax2;
    }

    public void setAmountWithoutTax2(BigDecimal amountWithoutTax2) {
        this.amountWithoutTax2 = amountWithoutTax2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((criteria1Value == null) ? 0 : criteria1Value.hashCode());
        result = prime * result + ((criteria2Value == null) ? 0 : criteria2Value.hashCode());
        result = prime * result + ((criteria3Value == null) ? 0 : criteria3Value.hashCode());
        result = prime * result + ((endRatingDate == null) ? 0 : endRatingDate.hashCode());
        result = prime * result + ((endSubscriptionDate == null) ? 0 : endSubscriptionDate.hashCode());
        result = prime * result + ((eventCode == null) ? 0 : eventCode.hashCode());
        result = prime * result + ((maxSubscriptionAgeInMonth == null) ? 0 : maxSubscriptionAgeInMonth.hashCode());
        result = prime * result + ((minSubscriptionAgeInMonth == null) ? 0 : minSubscriptionAgeInMonth.hashCode());
        result = prime * result + ((startRatingDate == null) ? 0 : startRatingDate.hashCode());
        result = prime * result + ((startSubscriptionDate == null) ? 0 : startSubscriptionDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj==null || (getClass() != obj.getClass())){
            return false;
        }
        PricePlanMatrix other = (PricePlanMatrix) obj;
        if (criteria1Value == null) {
            if (other.criteria1Value != null)
                return false;
        } else if (!criteria1Value.equals(other.criteria1Value))
            return false;
        if (criteria2Value == null) {
            if (other.criteria2Value != null)
                return false;
        } else if (!criteria2Value.equals(other.criteria2Value))
            return false;
        if (criteria3Value == null) {
            if (other.criteria3Value != null)
                return false;
        } else if (!criteria3Value.equals(other.criteria3Value))
            return false;
        if (endRatingDate == null) {
            if (other.endRatingDate != null)
                return false;
        } else if (!endRatingDate.equals(other.endRatingDate))
            return false;
        if (endSubscriptionDate == null) {
            if (other.endSubscriptionDate != null)
                return false;
        } else if (!endSubscriptionDate.equals(other.endSubscriptionDate))
            return false;
        if (eventCode == null) {
            if (other.eventCode != null)
                return false;
        } else if (!eventCode.equals(other.eventCode))
            return false;
        if (maxSubscriptionAgeInMonth == null) {
            if (other.maxSubscriptionAgeInMonth != null)
                return false;
        } else if (!maxSubscriptionAgeInMonth.equals(other.maxSubscriptionAgeInMonth))
            return false;
        if (minSubscriptionAgeInMonth == null) {
            if (other.minSubscriptionAgeInMonth != null)
                return false;
        } else if (!minSubscriptionAgeInMonth.equals(other.minSubscriptionAgeInMonth))
            return false;
        if (startRatingDate == null) {
            if (other.startRatingDate != null)
                return false;
        } else if (!startRatingDate.equals(other.startRatingDate))
            return false;
        if (startSubscriptionDate == null) {
            if (other.startSubscriptionDate != null)
                return false;
        } else if (!startSubscriptionDate.equals(other.startSubscriptionDate))
            return false;
        return true;
    }

}

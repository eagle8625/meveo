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
package org.meveo.model.payments;

/**
 * @author Tyshan(tyshan@manaty.net)
 * @created Nov 16, 2010 11:07:11 AM
 */
public enum MatchingTypeEnum {

    A(1, "matchingTypeEnum.a"),
    M(2, "matchingTypeEnum.m"),
    A_TIP(3, "matchingTypeEnum.A_TIP"),
    A_DERICT_DEBIT(4, "matchingTypeEnum.A_DERICT_DEBIT");

    private Integer id;
    private String label;

    MatchingTypeEnum(Integer id, String label) {
        this.label = label;
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public Integer getId() {
        return this.id;
    }

    public static MatchingTypeEnum getValue(Integer id) {
        if (id != null) {
            for (MatchingTypeEnum status : values()) {
                if (id.equals(status.getId())) {
                    return status;
                }
            }
        }
        return null;
    }

}

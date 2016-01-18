/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.persistence.api;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.base.Preconditions;

@Entity
@DiscriminatorValue("threshold")
public class Threshold extends AbstractReductionFunction {

    @Column(name="threshold", nullable=false)    
    private float m_threshold;

    public void setThreshold(float threshold) {
        Preconditions.checkArgument(threshold > 0, "threshold must be strictly positive");
        Preconditions.checkArgument(threshold <= 1, "threshold must be less or equal to 1");
        m_threshold = threshold;
    }

    public float getThreshold() {
        return m_threshold;
    }

    @Override
    public Optional<OnmsSeverity> reduce(Collection<OnmsSeverity> sources) {
        // TODO: Implement proper logic. This is wrong.
        return Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Threshold other = (Threshold) obj;

        return Objects.equals(m_threshold, other.m_threshold) && super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), m_threshold);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this).add("id", getId())
                .add("threshold", m_threshold)
                .toString();
    }
}
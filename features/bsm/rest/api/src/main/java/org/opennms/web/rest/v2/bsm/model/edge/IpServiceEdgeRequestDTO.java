/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.model.edge;

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;

import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;

public class IpServiceEdgeRequestDTO {

    private Integer value;

    private MapFunctionDTO mapFunction;

    @XmlElement(name="value",required = true)
    public Integer getValue() {
        return value;
    }

    @XmlElement(name="mapFunction", required= true)
    public MapFunctionDTO getMapFunction() {
        return mapFunction;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setMapFunction(MapFunctionDTO mapFunction) {
        this.mapFunction = mapFunction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (getClass() != obj.getClass()) { return false; }
        IpServiceEdgeRequestDTO other = (IpServiceEdgeRequestDTO) obj;
        return Objects.equals(value, other.value)
                && Objects.equals(mapFunction, other.mapFunction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, mapFunction);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("value", value)
                .add("mapFunction", mapFunction)
                .toString();
    }
}

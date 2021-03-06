/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.service;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("service-configuration.xsd")
public class Attribute implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "name")
    private String m_name;

    @XmlElement(name = "value")
    private Value m_value;

    public Attribute() {
    }

    public Attribute(final String name, final String type, final String content) {
        setName(name);
        setValue(new Value(type, content));
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Value getValue() {
        return m_value;
    }

    public void setValue(final Value value) {
        m_value = ConfigUtils.assertNotNull(value, "value");
    }

    public int hashCode() {
        return Objects.hash(m_name, m_value);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Attribute) {
            final Attribute that = (Attribute) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_value, that.m_value);
        }
        return false;
    }
}

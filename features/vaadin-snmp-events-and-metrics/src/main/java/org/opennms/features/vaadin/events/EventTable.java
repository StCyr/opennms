/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.events;

import java.util.List;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;

/**
 * The Class Event Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class EventTable extends Table {

    /** The Table Container for Events. */
    private final BeanContainer<String, org.opennms.netmgt.xml.eventconf.Event> container =
            new BeanContainer<String, org.opennms.netmgt.xml.eventconf.Event>(org.opennms.netmgt.xml.eventconf.Event.class);

    /**
     * Instantiates a new event table.
     *
     * @param events the OpenNMS events
     */
    public EventTable(final List<org.opennms.netmgt.xml.eventconf.Event> events) {
        container.setBeanIdProperty("uei");
        container.addAll(events);
        setContainerDataSource(container);
        setImmediate(true);
        setSelectable(true);
        addStyleName("light");
        setVisibleColumns(new Object[] { "eventLabel", "uei" });
        setColumnHeaders(new String[] { "Event Label", "Event UEI" });
        setWidth("100%");
        setHeight("250px");
    }

    /**
     * Gets the event.
     *
     * @param eventId the event ID (the Item ID associated with the container, in this case, the Event's UEI)
     * @return the event
     */
    @SuppressWarnings("unchecked")
    public org.opennms.netmgt.xml.eventconf.Event getEvent(Object eventId) {
        return ((BeanItem<org.opennms.netmgt.xml.eventconf.Event>)getItem(eventId)).getBean();
    }

    /**
     * Gets the event container.
     *
     * @return the event container
     */
    @SuppressWarnings("unchecked")
    public BeanContainer<String, org.opennms.netmgt.xml.eventconf.Event> getContainer() {
        return (BeanContainer<String, org.opennms.netmgt.xml.eventconf.Event>) getContainerDataSource();
    }

}

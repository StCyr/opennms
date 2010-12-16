//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.protocols.wmi.wbem.jinterop;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethod;

/**
 * <p>OnmsWbemMethodImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemMethodImpl implements OnmsWbemMethod {
        private IJIDispatch wbemMethodDispatch;

    /**
     * <p>Constructor for OnmsWbemMethodImpl.</p>
     *
     * @param wbemMethodDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemMethodImpl(IJIDispatch wbemMethodDispatch) {
        this.wbemMethodDispatch = wbemMethodDispatch;
    }

    /**
     * <p>getWmiName</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiName()throws WmiException {
        try {
            return wbemMethodDispatch.get("Name").getObjectAsString2();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemMethod Name attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiOrigin</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiOrigin() throws WmiException {
        try {
            return wbemMethodDispatch.get("Origin").getObjectAsString2();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemMethod Origin attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiOutParameters</p>
     */
    public void getWmiOutParameters() {
        return; // TODO IMPLEEMNT THIS
    }

    /**
     * <p>getWmiInParameters</p>
     */
    public void getWmiInParameters() {
        return; // TODO IMPLEEMNT THIS
    }
}

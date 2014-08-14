//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.springframework.security;

import java.util.HashMap;
import java.util.Map;

/**
 * An uninstantiatable class that provides a servlet container-independent
 * interface to the authentication system and a list of useful constants.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public final class Authentication extends Object {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_READONLY = "ROLE_READONLY";
    public static final String ROLE_DASHBOARD = "ROLE_DASHBOARD";
    public static final String ROLE_RTC = "ROLE_RTC";
    public static final String ROLE_PROVISION = "ROLE_PROVISION";
    public static final String ROLE_REMOTING = "ROLE_REMOTING";
    public static final String ROLE_REST = "ROLE_REST";
    
    private static Map<String, String> s_oldToNewMap = new HashMap<String, String>();
    
    static {
    	s_oldToNewMap.put("OpenNMS RTC Daemon", ROLE_RTC);
    	s_oldToNewMap.put("OpenNMS Administrator", ROLE_ADMIN);
        s_oldToNewMap.put("OpenNMS Read-Only User", ROLE_READONLY);
        s_oldToNewMap.put("OpenNMS Dashboard User", ROLE_DASHBOARD);
        s_oldToNewMap.put("OpenNMS Provision User", ROLE_PROVISION);
        s_oldToNewMap.put("OpenNMS Remote Poller User", ROLE_REMOTING);
        s_oldToNewMap.put("OpenNMS REST User", ROLE_REST);
        
    	// There is no entry for ROLE_USER, because all authenticated people are users
    }

    /** Private, empty constructor so this class cannot be instantiated. */
    private Authentication() {
    }
    
    /**
     * <p>getSpringSecuirtyRoleFromOldRoleName</p>
     *
     * @param oldRole a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getSpringSecuirtyRoleFromOldRoleName(String oldRole) {
    	return s_oldToNewMap.get(oldRole);
    }

}
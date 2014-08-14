/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;
import org.opennms.netmgt.provision.support.AsyncBasicDetector;
import org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator;

/**
 * <p>Abstract AsyncMultilineDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncMultilineDetector extends AsyncBasicDetector<LineOrientedRequest, MultilineOrientedResponse> {

    /**
     * <p>Constructor for AsyncMultilineDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public AsyncMultilineDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /**
     * <p>Constructor for AsyncMultilineDetector.</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param serviceName a {@link java.lang.String} object.
     */
    public AsyncMultilineDetector(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
    }

    /** {@inheritDoc} */
    @Override
    abstract protected void onInit();
    
    /**
     * <p>expectCodeRange</p>
     *
     * @param beginRange a int.
     * @param endRange a int.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected ResponseValidator<MultilineOrientedResponse> expectCodeRange(final int beginRange, final int endRange){
        return new ResponseValidator<MultilineOrientedResponse>() {
            
            public boolean validate(final MultilineOrientedResponse response) {
                return response.expectedCodeRange(beginRange, endRange);
            }
            
        };
    }
    
    /** {@inheritDoc} */
    public ResponseValidator<MultilineOrientedResponse> startsWith(final String pattern){
        return new ResponseValidator<MultilineOrientedResponse>(){

            public boolean validate(final MultilineOrientedResponse response) {
                return response.startsWith(pattern);
            }
            
        };
    }
    
    /**
     * <p>request</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     */
    public LineOrientedRequest request(final String command) {
        return new LineOrientedRequest(command);
    }

}
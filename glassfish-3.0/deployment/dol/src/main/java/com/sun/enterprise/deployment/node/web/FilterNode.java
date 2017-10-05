/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.deployment.node.web;

import com.sun.enterprise.deployment.ServletFilterDescriptor;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.Vector;

/**
 * This class is responsible for handling filter xml node
 *
 * @author  Jerome Dochez
 * @version 
 */
public class FilterNode extends DisplayableComponentNode {

    private ServletFilterDescriptor descriptor;

    // constructor. register sub nodes.
    public FilterNode() {
        super();        
        registerElementHandler(new XMLElement(WebTagNames.INIT_PARAM), 
                                                            InitParamNode.class, "addInitializationParameter");            
    }
    
   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {

        if (descriptor==null) {
            descriptor = (ServletFilterDescriptor) super.getDescriptor();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */    
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebTagNames.NAME, "setDisplayName");
        table.put(WebTagNames.FILTER_NAME, "setName");
        table.put(WebTagNames.FILTER_CLASS, "setClassName");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     * 
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.ASYNC_SUPPORTED.equals(element.getQName())) {
            descriptor.setAsyncSupported(Boolean.valueOf(value));
        } else {
            super.setElementValue(element, value);
        }
    }
    
    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree 
     * @param node name for the root element of this xml fragment      
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, ServletFilterDescriptor descriptor) {       
        Node myNode = appendChild(parent, nodeName);
        writeDisplayableComponentInfo(myNode, descriptor);
        appendTextChild(myNode, WebTagNames.FILTER_NAME, descriptor.getName());         
        appendTextChild(myNode, WebTagNames.FILTER_CLASS, descriptor.getClassName());     
        appendTextChild(myNode, WebTagNames.ASYNC_SUPPORTED, String.valueOf(descriptor.isAsyncSupported()));     
        Vector initParams = descriptor.getInitializationParameters();
        if (!initParams.isEmpty()) {
            WebCommonNode.addInitParam(myNode, WebTagNames.INIT_PARAM, initParams.elements());
        }
        
        return myNode;
    }       
}
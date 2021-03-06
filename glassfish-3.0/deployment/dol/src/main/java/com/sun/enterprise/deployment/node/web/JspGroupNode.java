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

import com.sun.enterprise.deployment.JspGroupDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.LocalizedInfoNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

import java.util.Enumeration;
import java.util.Map;

/**
 * This node is responsible for handling jsp-group xml tag
 * @version 
 */
public class JspGroupNode  extends DeploymentDescriptorNode {
    private JspGroupDescriptor descriptor;

    public JspGroupNode() {
        super();
        registerElementHandler(new XMLElement(WebTagNames.NAME), LocalizedInfoNode.class);   
    }
    
    /**
     * all sub-implementation of this class can use a dispatch table to map 
     * xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a 
     * value
     */    
    protected Map getDispatchTable() {    
        Map table = super.getDispatchTable();
        table.put(WebTagNames.URL_PATTERN, "addUrlPattern");
        table.put(TagNames.NAME, "setDisplayName");
        table.put(WebTagNames.EL_IGNORED, "setElIgnored");
        table.put(WebTagNames.PAGE_ENCODING, "setPageEncoding");
        table.put(WebTagNames.SCRIPTING_INVALID, "setScriptingInvalid");
        table.put(WebTagNames.INCLUDE_PRELUDE, "addIncludePrelude");
        table.put(WebTagNames.INCLUDE_CODA, "addIncludeCoda");
        table.put(WebTagNames.IS_XML, "setIsXml");
        table.put(WebTagNames.DEFERRED_SYNTAX_ALLOWED_AS_LITERAL, 
            "setDeferredSyntaxAllowedAsLiteral");
        table.put(WebTagNames.TRIM_DIRECTIVE_WHITESPACES, 
            "setTrimDirectiveWhitespaces");
        table.put(WebTagNames.DEFAULT_CONTENT_TYPE, "setDefaultContentType");
        table.put(WebTagNames.BUFFER, "setBuffer");
        table.put(WebTagNames.ERROR_ON_UNDECLARED_NAMESPACE,
            "setErrorOnUndeclaredNamespace");
        return table;
    }    

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
        if (descriptor == null) {
            descriptor = (JspGroupDescriptor)super.getDescriptor();
        }
        return descriptor;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree 
     * @param node name for the root element of this xml fragment      
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, JspGroupDescriptor descriptor) {  
        Node myNode = appendChild(parent, nodeName);

        LocalizedInfoNode localizedNode = new LocalizedInfoNode();
        writeLocalizedDescriptions(myNode, descriptor);        
        localizedNode.writeLocalizedMap(myNode, TagNames.NAME, descriptor.getLocalizedDisplayNames());
        
        // url-pattern*
        for (String urlPattern : descriptor.getUrlPatterns()) {
            appendTextChild(myNode, WebTagNames.URL_PATTERN, urlPattern);
        }
        appendTextChild(myNode, WebTagNames.EL_IGNORED,
            descriptor.getElIgnored());
        appendTextChild(myNode, WebTagNames.PAGE_ENCODING,
            descriptor.getPageEncoding());
        appendTextChild(myNode, WebTagNames.SCRIPTING_INVALID,
            descriptor.getScriptingInvalid());

        appendTextChild(myNode, WebTagNames.IS_XML, descriptor.getIsXml());

        // include-prelude*
        for (String includePrelude : descriptor.getIncludePreludes()) {
            appendTextChild(myNode, WebTagNames.INCLUDE_PRELUDE,
                includePrelude);
        }
        // include-coda*
        for (String includeCoda : descriptor.getIncludeCodas()) {
            appendTextChild(myNode, WebTagNames.INCLUDE_CODA, includeCoda);
        }
        appendTextChild(myNode, WebTagNames.DEFERRED_SYNTAX_ALLOWED_AS_LITERAL,
            descriptor.getDeferredSyntaxAllowedAsLiteral());
        appendTextChild(myNode, WebTagNames.TRIM_DIRECTIVE_WHITESPACES,
            descriptor.getTrimDirectiveWhitespaces());
        appendTextChild(myNode, WebTagNames.DEFAULT_CONTENT_TYPE,
            descriptor.getDefaultContentType());
        appendTextChild(myNode, WebTagNames.BUFFER, descriptor.getBuffer());
        appendTextChild(myNode, WebTagNames.ERROR_ON_UNDECLARED_NAMESPACE,
            descriptor.getErrorOnUndeclaredNamespace());

        return myNode;
    }    
}

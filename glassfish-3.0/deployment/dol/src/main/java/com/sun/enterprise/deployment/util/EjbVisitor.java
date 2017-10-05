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

/*
 * EjbVisitor.java
 *
 * Created on January 31, 2002, 11:28 AM
 */

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.*;

import java.util.Iterator;

/**
 * This class is responsible for visiting DOL ejb related  descritpors
 * 
 * @author  Jerome Dochez
 * @version 
 */
public interface EjbVisitor extends ComponentVisitor {
    
    /**
     * visits an ejb descriptor
     * @param ejb descriptor
     */
    public void accept(EjbDescriptor ejb);
    
    /**
     * visits a method permission and permitted methods for the 
     * last J2ee component visited
     * @param method permission 
     * @param the methods associated with the above permission
     */
    public void accept(MethodPermission pm, Iterator methods);

    /**
     * visits a method transaction 
     * @param ejb descritptor this method applies to
     * @param method descriptor the method
     * @param container transaction
     */
    public void accept(MethodDescriptor method, ContainerTransaction ct);
    
    /**
     * visits a CMP field definition (for CMP entity beans)
     * @param field descriptor for the CMP field
     */
    public void accept(FieldDescriptor fd);
    
    /**
     * visits a query method 
     * @param method descriptor for the method
     * @param query descriptor 
     */
    public void accept(MethodDescriptor method, QueryDescriptor qd);    

}

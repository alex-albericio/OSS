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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.io.Serializable;

import javax.validation.constraints.Pattern;

/**
 * Used to define the authentication policy requirements associated with the
 * response processing performed by an authentication provider (i.e. when a
 * client provider's ClientAuthModule.validateResponse() method is called or
 * when a server provider's erverAuthModule.secureResponse() method is called)
 */

/* @XmlType(name = "") */

@Configured
public interface ResponsePolicy extends ConfigBeanProxy, Injectable  {

    /**
     * Specifies the type of required authentication, either "sender" 
     * (user name and password) or "content" (digital signature).
     *
     * Defines a requirement for message layer sender authentication (e.g.
     * username password) or content authentication (e.g. digital signature)
     *
     * @return possible object is {@link String }
     */ 
    @Attribute
    @Pattern(regexp="(sender|content|username-password)")
    public String getAuthSource();

    /**
     * Sets the value of the authSource property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAuthSource(String value) throws PropertyVetoException;

    /**
     * Specifies whether recipient authentication occurs before or after content 
     * authentication. Allowed values are 'before-content' and 'after-content'.
     *
     * defines a requirement for message layer authentication of the reciever of
     * a message to its sender (e.g. by XML encryption).
     *  before-content
     *      indicates that recipient authentication (e.g. encryption)
     *      is to occur before any content authentication (e.g.
     *      encrypt then sign) with respect to the target of the
     *      containing auth-policy.
     *  after-content
     *      indicates that recipient authentication (e.g. encryption)
     *      is to occur after any content authentication (e.g. sign
     *      then encrypt) with respect to the target of the
     *      containing auth-policy.
     *
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Pattern(regexp="(before-content|after-content)")
    public String getAuthRecipient();

    /**
     * Sets the value of the authRecipient property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAuthRecipient(String value) throws PropertyVetoException;



}
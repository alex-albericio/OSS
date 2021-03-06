/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Assert;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.JavaConfig;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.beans.PropertyVetoException;

/**
 * User: Jerome Dochez
 * Date: Apr 7, 2008
 * Time: 11:13:22 AM
 */
public class JavaConfigSubTypesTest extends ConfigPersistence {


    @Test
    public void testSubTypesOfDomain() {
        JavaConfig config = super.getHabitat().getComponent(JavaConfig.class);
        try {
            Class<?>[] subTypes = ConfigSupport.getSubElementsTypes((ConfigBean) ConfigBean.unwrap(config));
            boolean found=false;
            for (Class subType : subTypes) {
                Logger.getAnonymousLogger().fine("Found class " + subType);
                if (subType.getName().equals(List.class.getName())) {
                    found=true;
                }
            }
            Assert.assertTrue(found);;
        } catch(ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    Habitat habitat = Utils.getNewHabitat(this);

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Override
    public Habitat getHabitat() {
        return habitat;
    }

    @Test
    public void doTest() throws TransactionFailure {


        JavaConfig javaConfig = habitat.getComponent(JavaConfig.class);

        ConfigSupport.apply(new SingleConfigCode<JavaConfig>() {
            public Object run(JavaConfig param) throws PropertyVetoException, TransactionFailure {
                List<String> jvmOptions = param.getJvmOptions();
                jvmOptions.add("-XFooBar=true");
                return jvmOptions;
            }
        }, javaConfig);

    }

    public boolean assertResult(String s) {
        return s.indexOf("-XFooBar")!=-1;
    }
    
}

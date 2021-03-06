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

package javax.resource.spi;

/**
 * This interface serves as a marker. An instance of an ActivationSpec must be a
 * JavaBean and must be serializable. This holds the activation configuration
 * information for a message endpoint.
 * 
 * @version 1.0
 * @author Ram Jeyaraman
 */
public interface ActivationSpec extends ResourceAdapterAssociation {

	/**
	 * This method may be called by a deployment tool to validate the overall
	 * activation configuration information provided by the endpoint deployer.
	 * This helps to catch activation configuration errors earlier on without
	 * having to wait until endpoint activation time for configuration
	 * validation. The implementation of this self-validation check behavior is
	 * optional.
	 * 
	 * Note: Since Java EE Connector 1.6 specification, resource adapter
	 * implementations are recommended to use the annotations or the XML 
     * validation deployment descriptor facilities defined by the Bean Validation
     * specification to express their validation requirements of its configuration 
     * properties to the application server.
     *
	 * @throws <code>InvalidPropertyException</code> indicates invalid
	 *         configuration property settings.
	 */
	void validate() throws InvalidPropertyException;
	
}

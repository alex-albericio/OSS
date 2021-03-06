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

/** SampleResultSetScenario.java
**/

package sample;

import javax.naming.*;
import javax.resource.cci.*;
import java.util.Iterator;
import java.sql.SQLException;
import javax.resource.ResourceException;

public class SampleResultSetScenario {

  public void method() {
    
    try {
      Context nc = new InitialContext();

      // JNDI lookup for ConnectionFactory
      ConnectionFactory cf = (ConnectionFactory)nc.lookup(
				 "java:comp/env/eis/ConnectionFactory");
      Connection cx = cf.getConnection();

      // Create an Interaction. 
      Interaction ix = cx.createInteraction();

      // Create an InteractionSpec and set properties.
      InteractionSpecImpl ixSpec = new InteractionSpecImpl();
      ixSpec.setFunctionName("<NAME OF FUNCTION>");
      ixSpec.setInteractionVerb(InteractionSpec.SYNC_SEND_RECEIVE);

      RecordFactory rf = cf.getRecordFactory();
      
      // Create an input MappedRecord. The component code adds values
      // based on the meta information it has accessed from the metadata
      // repository. The name of the Record acts as a pointer to the 
      // meta information (stored in the metadata repository) for a 
      // specific record type.
      MappedRecord input = rf.createMappedRecord("Name of Record");
      input.put("<key: element1>", new String("<VALUE1>"));
      input.put("<key: element2>", new String("<VALUE2>")); 

      // Execute the Interaction
      ResultSet rs = (javax.resource.cci.ResultSet)ix.execute(ixSpec, input);

      // Iterate over the ResultSet. The example here positions the
      // cursor on the first row and then iterates forward through 
      // the contents of the ResultSet. The getXXX methods are used
      // to retrieve column values:      
      rs.beforeFirst();
      while (rs.next()) {
	// Look at the current row of the resultSet
      }
    }
    catch (NamingException ne) {
      return;
    }
    catch (ResourceException e) {
      return;
    }    
    catch (SQLException e) {
      return;
    }    
  }



  public static void main(String[] args) {
  }

}

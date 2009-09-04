/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.transfer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.AbstractPort;

import org.cagrid.transfer.CaGridTransferActivity;
import org.cagrid.transfer.CaGridTransferConfigurationBean;
import org.junit.Test;

/**
 * Tests the StringConstantActivity
 * @author Stuart Owen
 *
 */
public class CaGridTransferActivityTest {

	/**
	 * Simple invocation test. Also tests Activity.configure sets up the correct output port.
	 * @throws Exception
	 */
	@Test
	public void testInvoke() throws Exception {
		CaGridTransferConfigurationBean bean = new CaGridTransferConfigurationBean();
		
		CaGridTransferActivity activity = new CaGridTransferActivity();
		activity.configure(bean);
		
		assertEquals("there should be 4 inputs",4,activity.getInputPorts().size());
		assertEquals("there should be 1 output",1,activity.getOutputPorts().size());
		assertEquals("the output port name should be transferredFileName","transferredFileName",((AbstractPort)activity.getOutputPorts().toArray()[0]).getName());
		
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("transferredFileName", String.class);

		
	}
}

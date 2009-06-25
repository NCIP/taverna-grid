/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
 * Copyright (C) 2009 The University of Chicago
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
package net.sf.taverna.t2.activities.cagrid.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

public class CaGridQueryTest {

	private static String indexURL;
	@BeforeClass
	public static void setup() {
		//TODO should read from an external resource
		indexURL="http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
	}
	
	
	@Ignore("This test takes ages as it loads all caGrid services so we have disabled it.")
	@Test
	public void testDoQuery() {
		CaGridQuery q = new CaGridQuery(indexURL,null);
		q.doQuery();
		CaGridActivityItem i = (CaGridActivityItem)q.toArray()[0];
		assertEquals("The type should be caGrid Services","caGrid Services",i.getType());
		assertEquals("The style should be document","document",i.getStyle());
		assertNotNull("The operation should be set",i.getOperation());
		assertTrue("The operation should be have some content",i.getOperation().length()>2);
	}
	
}

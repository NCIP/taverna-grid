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
package org.cagrid.cds;


/**
 * Configuration bean for setting up a CaGridTransferActivity.<br>
 * The only thing to be configured is the function: upload/download.
 * 
 * @author Wei Tan
 * @see CDSActivity
 */
public class CDSConfigurationBean {
	
	private String caGridName = "NCI Production CaGrid 1.3";
	//which party to delegate
	private String party = "/O=caBIG/OU=caGrid/OU=Services/CN=cagrid-fqp.nci.nih.gov";
	//Specifies how many hours the delegation service can delegated this
    // credential to other parties.
	private int delegationLifetime = 4; 
	  // Specifies the path length of the credential being delegate the
    // minumum is 1.
	private int delegationPathLength = 1;
	 // Specifies the how long (in hours) credentials issued to allowed parties will
    // be valid for.
	private int issuedCredentialLifetime = 1;
	// Specifies the path length of the credentials issued to allowed
    // parties. A path length of 0 means that
    // the requesting party cannot further delegate the credential.
	private int issuedCredentialPathLength = 0;
	/**
	 * @return the caGrid name of the CDSActivity
	 */
	public String getCaGridName() {
		return caGridName;
	}

	/**
	 * @param caGridName: set the caGrid name of the CDSActivity
	 */
	public void setCaGridName(String caGridName) {
		this.caGridName = caGridName;
	}

	public void setParty(String party) {
		this.party = party;
	}

	public String getParty() {
		return party;
	}

	public void setDelegationLifetime(int delegationLifetime) {
		this.delegationLifetime = delegationLifetime;
	}

	public int getDelegationLifetime() {
		return delegationLifetime;
	}

	public void setDelegationPathLength(int delegationPathLength) {
		this.delegationPathLength = delegationPathLength;
	}

	public int getDelegationPathLength() {
		return delegationPathLength;
	}

	public void setIssuedCredentialLifetime(int issuedCredentialLifetime) {
		this.issuedCredentialLifetime = issuedCredentialLifetime;
	}

	public int getIssuedCredentialLifetime() {
		return issuedCredentialLifetime;
	}

	public void setIssuedCredentialPathLength(int issuedCredentialPathLength) {
		this.issuedCredentialPathLength = issuedCredentialPathLength;
	}

	public int getIssuedCredentialPathLength() {
		return issuedCredentialPathLength;
	}
	

}

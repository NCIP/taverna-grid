package net.sf.taverna.t2.workbench.cagrid;

public class WFProperties {
	/*TRANSFER_NONE = 0; if there is no cagrid-transfer plug-in
	 *  TRANSFER_UPLOAD_ONLY = 1; if there is only upload transfer
	 *  TRANSFER_DOWNLOAD_ONLY = 2; if there is download only transfer
	 *  TRANSFER_BOTH = 3; if it has both upload and download
	 */
	public static final int TRANSFER_NONE = 0;
	public static final int TRANSFER_UPLOAD_ONLY = 1;
	public static final int TRANSFER_DOWNLOAD_ONLY = 2;
	public static final int TRANSFER_BOTH = 3;
	
	public boolean needSecurity;
	public int needTransfer;
	
	WFProperties(){
		needSecurity = false;
		needTransfer = TRANSFER_NONE;
	}

}

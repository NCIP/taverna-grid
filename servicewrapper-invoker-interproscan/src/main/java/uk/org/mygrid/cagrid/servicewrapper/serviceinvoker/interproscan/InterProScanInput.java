package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan;

import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.InputParams;

public class InterProScanInput {

	private InputParams params;

	private Data[] content;

	public InputParams getParams() {
		return params;
	}

	public void setParams(InputParams params) {
		this.params = params;
	}

	public Data[] getContent() {
		return content;
	}

	public void setContent(Data[] content) {
		this.content = content;
	}

}

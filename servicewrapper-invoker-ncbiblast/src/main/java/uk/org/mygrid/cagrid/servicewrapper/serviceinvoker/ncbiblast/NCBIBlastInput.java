package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast;

import uk.ac.ebi.www.wsncbiblast.Data;
import uk.ac.ebi.www.wsncbiblast.InputParams;

public class NCBIBlastInput {
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
	
	@Override
	public String toString() {
		StringBuffer string = new StringBuffer("NCBIBlastInput params=");
		string.append(getParams());
		string.append(" content="); 
		string.append(" ");
		for (Data data : getContent()) {
			string.append(data);
			string.append('\n');
		}
		return string.toString();
	}
}

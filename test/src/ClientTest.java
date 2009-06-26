import static org.junit.Assert.*;

import org.junit.Test;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;


public class ClientTest {

	public static void main(String[] args) {
		new InterProScanClient("http://localhost:8080/");
	}
	
}

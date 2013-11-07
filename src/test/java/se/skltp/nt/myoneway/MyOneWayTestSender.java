package se.skltp.nt.myoneway;

import static org.soitoolkit.commons.mule.mime.MimeUtil.sendFileAsMultipartHttpPost;
import static se.skltp.nt.NtMuleServer.getAddress;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyOneWayTestSender {

	private static final Logger log = LoggerFactory.getLogger(MyOneWayTestSender.class);

	public static void main(String[] args) {
		String url       = getAddress("MYONEWAY_INBOUND_URL");
    	String inputFile = "src/test/resources/testfiles/myOneWay/input.txt";
		int timeout      = 5000;

		log.info("Post message to: {}, {} chars", url, inputFile.length());
    	sendFileAsMultipartHttpPost(url, new File(inputFile), "payload", false, timeout);
	}
}
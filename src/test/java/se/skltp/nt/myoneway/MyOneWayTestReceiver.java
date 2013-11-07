package se.skltp.nt.myoneway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyOneWayTestReceiver {

	private static final Logger log = LoggerFactory.getLogger(MyOneWayTestReceiver.class);

	public void process(Object message) {
		log.info("MyOneWayTestReceiver received the message: {}", message);
	}
}

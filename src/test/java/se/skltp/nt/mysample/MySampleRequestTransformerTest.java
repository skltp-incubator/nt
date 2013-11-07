package se.skltp.nt.mysample;

import static org.junit.Assert.assertEquals;


import org.junit.Test;
import org.soitoolkit.commons.mule.util.MiscUtil;

public class MySampleRequestTransformerTest {

	@Test
	public void testTransformer_ok() throws Exception {

		// Specify input and expected result 
		String input          = MiscUtil.readFileAsString("src/test/resources/testfiles/mySample/request-input.xml");

		String expectedResult = MiscUtil.readFileAsString("src/test/resources/testfiles/mySample/request-input.xml"); // No transformation is done by default so use input also as expected...


		// Create the transformer under test and let it perform the transformation

		MySampleRequestTransformer transformer = new MySampleRequestTransformer();
		String result = (String)transformer.pojoTransform(input, "UTF-8");


		// Compare the result to the expected value
		assertEquals(expectedResult, result);
	}
}
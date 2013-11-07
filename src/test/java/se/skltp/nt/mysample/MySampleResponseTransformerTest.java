package se.skltp.nt.mysample;

import static org.junit.Assert.assertEquals;
import static org.soitoolkit.commons.mule.smooks.SmooksUtil.runSmooksTransformer;

import java.io.IOException;

import org.junit.Test;
import org.mule.api.transformer.TransformerException;
import org.soitoolkit.commons.mule.util.MiscUtil;

public class MySampleResponseTransformerTest {

	@Test
	public void testTransformer_ok() throws Exception {

		// Specify input and expected result 

		String input          = MiscUtil.readFileAsString("src/test/resources/testfiles/mySample/response-expected-result.xml"); // No transformation is done by default so use expected also as input...

		String expectedResult = MiscUtil.readFileAsString("src/test/resources/testfiles/mySample/response-expected-result.xml");
		
		// Create the transformer under test and let it perform the transformation

		MySampleResponseTransformer transformer = new MySampleResponseTransformer();
		String result = (String)transformer.pojoTransform(input, "UTF-8");


		// Compare the result to the expected value
		assertEquals(expectedResult, result);
	}


}
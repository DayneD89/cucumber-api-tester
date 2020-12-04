package uk.ddou.cucumber;

import org.testng.asserts.SoftAssert;

public class TestContext {

	private SoftAssert sa;

	public SoftAssert softAssert(){
		if (sa == null)
			sa = new SoftAssert();
		return sa;
	}

}

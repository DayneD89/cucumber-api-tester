package uk.ddou.cucumber;

import com.fasterxml.jackson.core.JsonParseException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class RestServiceCucumberTest extends CommonRestAssured{


	@Given("^a base uri \"(.*)\"$")
	public void setBaseURI(String uri) {
		super.setBaseURI(uri);
	}

	@Given("^a base path \"(.*)\"$")
	public void setBasePath(String basepath) {
		super.setBasePath(basepath);
	}

	@Given("^a port (\\d+)$")
	public void setPort(int port){
		super.setPort(port);
	}




	@Given("^(form parameters|query parameters|path parameters|parameters)$")
	public void withParams(String type, Map<String, String> map){
		map.forEach((key,val)->{
			Boolean list = false;
			List<String> vals = new ArrayList<String>();
			if (val.contains("::")){
				list = true;
				vals = Arrays.asList(val.split("::"));
			}

			if (!list)
				super.setParam(type, key, val);
			else
				super.setParamList(key, vals);
			});
	}

	@Given("^headers$")
	public void setHeader(Map<String, String> map){
		map.forEach((key,val)->{
			super.setHeader(key, val);
		});
	}

	@Given("^a JSON body \"(.*)\"$")
	public void requestBody(String data) throws JsonParseException, IOException{
		String jsonData = data.replace("\\","");
		super.setBody("application/json",jsonData);
	}


	@When("^the system requests (GET|PUT|POST|PATCH|DELETE|HEAD) \"(.*)\"$")
	public void apiGetRequest(String apiMethod, String path){
		callAPI(apiMethod, path);
	}
	@When("^the system requests (GET|PUT|POST|PATCH|DELETE|HEAD)$")
	public void apiGetRequest(String apiMethod){
		callAPI(apiMethod, "");
	}

	@Then("^the response code is (\\d+)$")
	public void verify_status_code(int code) throws NumberFormatException, IOException{
		checkStatus(code);
	}
	@Then("^the response body (is|is not|isnt|isn't) a valid (JSON|CSV|XML|PSV)$")
	public void verify_body_type(String op, String type) throws IOException{
		assertType(type.toUpperCase(),op.equalsIgnoreCase("is"));
	}

	@Then("^the response time is less than (\\d+) milliseconds$")
	public void verifyResponseTime(long duration){
		checkResponseTime(duration);
	}

	@Then("^the response header contains$")
	public void verifyHeader(Map<String, String> map){
		map.forEach((key,val)->{
			checkHeader(key, val);
			});
	}


	@And("^the response body contains$")
	public void responseBodyValid(DataTable table) {
		List<List<String>> temp = table.asLists();
		List<ResponseValidator> list = new ArrayList<>();
		for(int x=0;x<temp.size();x++) {
			list.add(
					new ResponseValidator(
							temp.get(x).get(0),
							temp.get(x).get(1),
							temp.get(x).get(2),
							temp.get(x).get(3))
			);
		}
		responseContains(list);
	}
 }
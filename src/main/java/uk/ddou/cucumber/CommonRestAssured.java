package uk.ddou.cucumber;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;


public class CommonRestAssured {

	TestContext context;
	public CommonRestAssured()
	{
		request = given();//.contentType(ContentType.JSON);
		context = new TestContext();
	}

	private Response response = null;
	private JsonPath respJson = null;
	private String respString = null;
	private JSONObject jsonObject = null;

	private ValidatableResponse respValidator = null;

	public RequestSpecification request = null;

	public void setBaseURI(String uri){
		request.baseUri(uri);
	}

	public void setBasePath(String basepath){
		request.basePath(basepath);
	}

	public void setPort(int port){
		request.port(port);
	}

	public void setHeader(String key, String val){
		request.header(key, val);
	}

	public void setParam(String type, String key, String val){
		switch(type){
			case "parameters": 			request.param(key, val);break;
			case "form parameters": 	request.formParam(key, val);break;
			case "query parameters": 	request.queryParam(key, val);break;
			case "path parameters": 	request.pathParam(key, val);break;
		}
	}

	public void setParamList(String key, List<String> val){
		request.param(key, val);
	}

	public void setBody(String type, String content){
		request.contentType(type)
				.body(content);
	}


	public void callAPI(String method, String path){
		switch(method){
		case "GET":		response = request.get(path);break;
		case "PUT":		response = request.put(path);break;
		case "POST":	response = request.post(path);break;
		case "PATCH":	response = request.patch(path);break;
		case "DELETE":	response = request.delete(path);break;
		case "HEAD":	response = request.head(path);break;
		}

		respValidator = response.then();
		//System.out.println("response.then():"+response.then());
		respString = response.asString();
		try{
			jsonObject = new JSONObject(respString);
		}catch(Exception e){}
		respJson = new JsonPath(respString);
	}

	public void checkStatus(int statusCode){
		respValidator.assertThat().statusCode(statusCode);
	}

	public void checkResponseTime(long duration){
		respValidator.assertThat().time(lessThan(duration));
	}

	public void checkHeader(String key, String val){
		assertThat(response.header(key),is(val));
	}

	public void assertType(String type,boolean matcher) {
		type=type.toUpperCase();
		String message = matcher ? "Not Valid "+type+" response" : "Response is a "+type;
		switch(type) {
			case "JSON":
				assertThat(message,matcher == isJSON());
				break;
			case "XML":
				assertThat(message,matcher == isXML());
				break;
			case "CSV":
				assertThat(message,matcher == is_SV(','));
				break;
			case "PSV":
				assertThat(message,matcher == is_SV('|'));
				break;
		}
	}
	public boolean isJSON(){
		boolean validJSON;
		try {
			new JSONObject(respString);
			validJSON = true;
		} catch(JSONException ex) {
			try {
				new JSONArray(respString);
				validJSON = true;
			} catch (JSONException ex1) {
				validJSON = false;
			}
		}
		return validJSON;
	}
	public boolean isXML(){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler());
			Document doc = builder.parse(new InputSource(new StringReader(respString)));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	public boolean is_SV(char delim){
		String d = (delim=='p'||delim=='P'||delim=='|') ? "|" : ",";
		try {
			String[] lines = respString.split("\n");
			int num = 0;
			for(int i=0;i<lines.length;i++) {
				int tNum = lines[i].split(d).length;
				if(tNum==1) throw new Exception();
				if(i==0) num = tNum;
				else if(tNum-num>1 || tNum-num<-1) throw new Exception();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}



	private JSONObject GetRespJSON() {
		int type = (respString==null || respString.trim().length()==0) ? 0 : isJSON() ? 1 : isXML() ? 2 : is_SV('C') ? 3 : is_SV('P') ? 4 : 5;
		switch (type) {
			case 0 :
				return new JSONObject("{}");
			case 1 :
				return new JSONObject(respString);
			case 2:
				JSONObject json = XML.toJSONObject(respString);
				while(json.length()==1)
					json = json.optJSONObject(
							(respString.startsWith("<?") ? respString.substring(respString.indexOf('>')+1) : respString)
							.split("<")[1].split(">")[0]);
				return json;
			case 3:
				return GetRespJSON_CV('C');
			case 4:
				return GetRespJSON_CV('P');
			case 5:
			default:
				assertThat("Could not read response as JSON (Supported conversions include XML,CSV and PSV)",false);
				return null;
		}
	}
	private JSONObject GetRespJSON_CV(char c) {
		String delim = (c=='p'||c=='P') ? "|" : ",";
		String[] lines = respString.split("\n");
		String[] keys = lines[0].split(",");
		JSONObject csvJson = new JSONObject();
		for(int i=1;i<lines.length;i++) {
			for(int j=0;j<keys.length;j++) {
				try {
					csvJson.append(keys[j],Integer.parseInt(lines[i].split(delim)[j]));
				} catch (Exception e) {
					try {
						csvJson.append(keys[j],Double.parseDouble(lines[i].split(delim)[j]));
					} catch (Exception e2) {
						if (lines[i].split(delim)[j].equalsIgnoreCase("true") || lines[i].split(delim)[j].equalsIgnoreCase("false")) {
							csvJson.append(keys[j], lines[i].split(delim)[j].equalsIgnoreCase("true"));
						} else {
							csvJson.append(keys[j], lines[i].split(delim)[j]);
						}
					}
				}
			}
		}
		return new JSONObject(csvJson.toString());
	}
	public void responseContains(List<ResponseValidator> table){
		JSONObject respJSON = GetRespJSON();
		//System.out.println("--------------\n"+respJSON.toString(2)+"\n--------------------------------\n\n");
		table.forEach((data) -> {
			data.validate(respJSON);
		});
	}
}

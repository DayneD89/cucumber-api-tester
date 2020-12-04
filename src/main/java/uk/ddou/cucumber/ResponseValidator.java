package uk.ddou.cucumber;

import cucumber.api.java.eo.Do;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * POJO used to define contents of cucumber datatable when invoking response
 * body validation of rest api calls
 */
public class ResponseValidator {


	public ResponseValidator(String el, String ma, String va, String ty) {
		this.matcher = ma.toLowerCase();
		if (matcher.startsWith("!")) {
			negative = true;
			matcher = matcher.substring(1);
		}
		if (matcher.startsWith("~")) {
			like = true;
			matcher = matcher.substring(1);
		}
		switch (ty.toLowerCase()) {
			case "num" :
			case "int" :
			case "integer" :
				type = "int";
				try {value = Integer.parseInt(va);}
				catch (Exception e) {strOverride = "INVALID FEATURE: CANNOT PASS "+va+" AS INT.";type = "unknown";}
				break;
			case "double" :
			case "float" :
				type = "double";
				try {value = Double.parseDouble(va);}
				catch (Exception e) {strOverride = "INVALID FEATURE: CANNOT PASS "+va+" AS DOUBLE.";type = "unknown";}
				break;
			case "bool" :
			case "boolean" :
				value = va.toLowerCase().charAt(0)==('t');
				type = "bool";
				break;
			case "":
				type = "unknown";
				value = va;
				break;
			case "text":
			case "string":
				type = "text";
				value = va;
		}
		this.element = el;
	}

	private String element;
	private String matcher;
	private String type;
	private boolean array = true;
	private boolean negative = false;
	private boolean like = false;
	private Object value;
	private Object result = null;
	private String strOverride="";

	@Override
	public String toString() {
		return strOverride.length()==0
				? "ResponseValidator [element=" + element + ", matcher=" + matcher + ", value=" + value + "]"
				: "ResponseValidator [element=" + element + ", matcher=" + matcher + ", value=" + value + ", message=" +strOverride+"]";
	}

	private JSONObject getInnerItem(JSONObject json) {
		Pattern pattern = Pattern.compile(".*\\[(.+)\\]");
		String item="";
		for (int i=0;i<element.split("\\.").length;i++) {
			item = element.split("\\.")[i];
			System.out.println(item);
			java.util.regex.Matcher matcher = pattern.matcher(item);
			if(matcher.matches()) {
				String found = matcher.group(1);
				int arrnum = Integer.parseInt(found);
				item = item.substring(0, item.length() - (2+found.length()));
				JSONArray arr = json.getJSONArray(item);
				json = arr.getJSONObject(arrnum);
			} else {
				if(i==element.split("\\.").length-1) break;
				json = json.getJSONObject(item);
			}
		}
		element=item;
		return json;
	}

	void validate(JSONObject json) {
		json = getInnerItem(json);
		try {
			if(json.getJSONArray(element)!=null) {
				result = json.getJSONArray(element);
			}
		} catch (Exception e) {array=false;}

		Matcher<Object> m = GetMatcher();
		if(result==null)
			switch(type) {
				case "integer" :
				case "num" :
				case "number" :
					type = "int";
				case "int" :
					result = json.getInt(element);
					break;
				case "float" :
					type = "double";
				case "double" :
					result = json.getDouble(element);
					break;
				case "boolean" :
					type = "bool";
				case "bool" :
					result = json.getBoolean(element);
					break;
				case "string" :
					type = "text";
				case "text" :
					result = json.getString(element);
					break;
				case "" :
					type = "unknown";
				case "unknown" :
					result = json.get(element).toString();break;
			}
		//System.out.println(this);
		//System.out.println("MATCHER:"+m.getClass().getSimpleName());
		//System.out.println("\tRESULT:\n"+result.getClass().getSimpleName()+"\n"+result.toString());
		//System.out.println("VALUE:\n"+value.getClass().getSimpleName()+"\n"+value.toString());
		assertThat(result,(negative && strOverride.equals("")) ? not(m) : m);
	}

	private Matcher GetMatcher() {
		switch(matcher) {
			case "equals":
			case "equal":
			case "is":
				return (array) ? allOf(iterableWithSize(1),contains(value))
						: ((like)
							? anyOf(equalToIgnoringCase(value.toString()),equalToCompressingWhiteSpace(value.toString()))
							: equalTo(value));
			case "hasitem":
				return hasItem(value);
			case "hasitems":
				return hasItems(value.toString().split(","));
			case "contains":
				return  (like) ? containsInAnyOrder(value) : contains(value);
			case "hassize":
				return hasSize((Integer)value);
			case "startswith":
				if(array) {
					Matcher tm = (((JSONArray)result).length()>1)
							? is(value)
							: startsWith(value.toString());
					result = ((JSONArray)result).get(0);
					return tm;
				} else {
					type="unknown";
					return startsWith(value.toString());
				}
			case "endswith":
				if(array) {
					Matcher tm = (((JSONArray)result).length()>1)
							? is(value)
							: endsWith(value.toString());
					result = ((JSONArray)result).get(0);
					return tm;
				} else {
					type="unknown";
					return endsWith(value.toString());
				}
			default:
				strOverride="Invalid matcher";
				return not(true);
		}
	}
}

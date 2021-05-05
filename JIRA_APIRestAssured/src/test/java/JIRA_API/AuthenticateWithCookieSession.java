package JIRA_API;

import static io.restassured.RestAssured.*;

import java.io.File;
import org.testng.Assert;
import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
public class AuthenticateWithCookieSession {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		updateCommentInJIRA();
	}

	public static void updateCommentInJIRA() {
		
		RestAssured.baseURI="http://localhost:8080";
		String filePath = "E:\\Workspace\\API_RestAssured\\JIRA_APIRestAssured\\src\\test\\resources\\file.txt";
		//get the session id to authenticate
		SessionFilter session = new SessionFilter();
		
		String resonse = given().header("Content-Type","application/json").body(loginPayload()).log().all()
		.filter(session).when().post("/rest/auth/1/session")
		.then().log().all().extract().response().asString();
		
		//Add comment in jira
		String addCommentResponse = given().pathParam("key", "10001").contentType(ContentType.JSON).body(addCommentPayload()).log().all()
		.filter(session).when().post("/rest/api/2/issue/{key}/comment")
		.then().extract().response().asString();
		
		//Print the id of comment from the response
		JsonPath path = new JsonPath(addCommentResponse);
		String commentId = path.getString("id");
		System.out.println(path.getString("id"));
		
		//add attachment
		given().header("X-Atlassian-Token","no-check").pathParam("key", "10001").header("Content-Type","multipart/form-data")
		.multiPart("file", new File(filePath)).filter(session)
		.when().post("rest/api/2/issue/{key}/attachments")
		.then().log().all().statusCode(200);
		
		//get issue
		String getIssueReponse = given().filter(session).pathParam("key", "10001").queryParam("fields", "comment")
		.when().get("/rest/api/2/issue/{key}")
		.then().log().all().extract().response().asString();
		
		System.out.println(getIssueReponse);
		
		JsonPath js1 =new JsonPath(getIssueReponse);
		int commentsCount=js1.getInt("fields.comment.comments.size()");
		for(int i=0;i<commentsCount;i++)
		{
			String commentIdIssue =js1.get("fields.comment.comments["+i+"].id").toString();
			if (commentIdIssue.equalsIgnoreCase(commentId)){
				String message= js1.get("fields.comment.comments["+i+"].body").toString();
				System.out.println(message);
				Assert.assertEquals(message, "Comments are added by sudha");
			}
		}
	}
	
	public static String addCommentPayload() {
		String payload = "{\r\n"
				+ "    \"body\": \"Comments are added by sudha\",\r\n"
				+ "    \"visibility\": {\r\n"
				+ "        \"type\": \"role\",\r\n"
				+ "        \"value\": \"Administrators\"\r\n"
				+ "    }\r\n"
				+ "}";
				return payload;
	}
	
	public static String loginPayload() {
		String payload = "{ \r\n"
				+ "    \"username\": \"sam3.sudha\", \r\n"
				+ "    \"password\": \"Bluesky@2020\" }";
		return payload;
	}
}

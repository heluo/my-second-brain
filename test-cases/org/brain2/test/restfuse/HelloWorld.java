package org.brain2.test.restfuse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.runner.RunWith;

import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.HttpJUnitRunner;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.PollState;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.HttpTest;

@RunWith(HttpJUnitRunner.class)
public class HelloWorld {
	
	@Context
	private Response response; // will be injected after every request
	
	@Context
	private PollState state;
	
	@Rule
	public Destination destination = new Destination("http://mapi.vnexpress.net");
	
	
	/*@Rule
	public Destination destination = new Destination("http://mapi.vnexpress.net");
	

	@HttpTest(method = Method.GET, path = "/articles?method=get")
	public void testBasic() {
		com.eclipsesource.restfuse.Assert.assertOk(response);
		int responseCode = response.getStatus();
		System.out.println(responseCode);
		System.out.println(response.getBody(String.class));
	}

	@HttpTest(method = Method.GET, path = "/articles?method=get")
	@Poll(times = 2, interval = 200)
	public void testPoll() {
		Response currentRes = state.getResponse(state.getTimes());
		org.junit.Assert.assertEquals(currentRes, response);
	}

	@HttpTest(method = Method.GET, path = "/articles?method=get", headers = { @Header(name = "User-Agent", value = "Virus de") })
	public void testHeader() {
		com.eclipsesource.restfuse.Assert.assertOk(response);
	}*/
	
	@HttpTest(method = Method.POST, path = "/mongo/post.php", content = "a:1" )	
	public void testPost() {
		com.eclipsesource.restfuse.Assert.assertOk(response);
		int responseCode = response.getStatus();
		System.out.println(responseCode);
		System.out.println(response.getBody(String.class));
	}
	
	@HttpTest(method = Method.GET, path = "/mongo/post.php")
	public void testBasic() {
		com.eclipsesource.restfuse.Assert.assertOk(response);
		int responseCode = response.getStatus();
		System.out.println(responseCode);
		System.out.println(response.getType().getMimeType());
		System.out.println(response.getBody(String.class));
	}

	@HttpTest(method = Method.GET, path = "/articles?method=get")
	public void testArticlesAPI() throws JSONException {
		com.eclipsesource.restfuse.Assert.assertOk(response);
		int responseCode = response.getStatus();
		System.out.println(responseCode);
		String json = response.getBody(String.class);
		JSONObject jsonObject = new JSONObject(json);
		JSONArray jsonArray = jsonObject.getJSONObject("body").getJSONArray("articles");
		for (int i = 0; i < jsonArray.length(); i++) {
			System.out.println(jsonArray.get(i));
		}
	}

}

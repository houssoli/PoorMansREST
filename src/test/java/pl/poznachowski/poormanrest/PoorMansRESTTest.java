package pl.poznachowski.poormanrest;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static junitparams.JUnitParamsRunner.$;

import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.munit.runner.functional.FunctionalMunitSuite;
import org.mule.transport.http.HttpResponse;

import com.google.common.collect.ImmutableMap;

@RunWith(JUnitParamsRunner.class)
public class PoorMansRESTTest extends FunctionalMunitSuite {

	private static final String SOME_PAYLOAD = "Payload";
	private static final String POOR_MAN_REST_FLOW = "PoorMansRESTFlow";
	private static final String ACC_ID = "123";
	private static final String USER_ID = "456";

	private static final String GOOD_PATH = "/client/" + ACC_ID + "/" + USER_ID + "/get";
	private static final String HTTP_PATH = "http.request.path";
	private static final String HTTP_METHOD = "http.method";
	private static final String POST = "POST";
	private static final String GET = "GET";

	@Test
	@Parameters(method = "badURLs")
	public void shouldReturn400StatusFor_GoodMethod_BadURL(Map<String, Object> inboundProperties) throws Exception {

		MuleEvent testEvent = MuleEventEnricher.enrich(testEvent(SOME_PAYLOAD)).withInboundProperties(inboundProperties).get();
		MuleEvent resultEvent = runFlow(POOR_MAN_REST_FLOW, testEvent);

		assertThat(resultEvent, is(notNullValue()));
		assertThat(resultEvent.getMessage(), is(notNullValue()));
		assertThat(resultEvent.getMessage().getPayload(), is(notNullValue()));
		assertThat(resultEvent.getMessage().getPayload(), is(instanceOf(HttpResponse.class)));

		HttpResponse httpResponse = (HttpResponse) resultEvent.getMessage().getPayload();
		assertThat(httpResponse.getStatusCode(), is(400));
		assertTrue(httpResponse.getBodyAsString().contains("Unknown resource"));
	}

	@SuppressWarnings("unused")
	private Object[] badURLs() {
		return $($(ImmutableMap.of(HTTP_METHOD, POST, HTTP_PATH, "/client_s/123/456/get")),
				$(ImmutableMap.of(HTTP_METHOD, POST, HTTP_PATH, "/client/123/456")),
				$(ImmutableMap.of(HTTP_METHOD, GET, HTTP_PATH, "/client/123/456/")),
				$(ImmutableMap.of(HTTP_METHOD, GET, HTTP_PATH, "/123/456/client/get")),
				$(ImmutableMap.of(HTTP_METHOD, GET, HTTP_PATH, "/client/123/ /get")),
				$(ImmutableMap.of(HTTP_METHOD, GET, HTTP_PATH, "/client//456/get")));
	}

	@Test
	@Parameters(method = "badMethods")
	public void shouldReturn400StatusFor_GoodURL_BadMethod(Map<String, Object> inboundProperties) throws Exception {

		MuleEvent testEvent = MuleEventEnricher.enrich(testEvent(SOME_PAYLOAD)).withInboundProperties(inboundProperties).get();
		MuleEvent resultEvent = runFlow(POOR_MAN_REST_FLOW, testEvent);

		assertThat(resultEvent, is(notNullValue()));
		assertThat(resultEvent.getMessage(), is(notNullValue()));
		assertThat(resultEvent.getMessage().getPayload(), is(notNullValue()));
		assertThat(resultEvent.getMessage().getPayload(), is(instanceOf(HttpResponse.class)));

		HttpResponse httpResponse = (HttpResponse) resultEvent.getMessage().getPayload();
		assertThat(httpResponse.getStatusCode(), is(400));
		assertTrue(httpResponse.getBodyAsString().contains("Unknown HTTP method"));
	}

	@Test
	@Parameters({ "GET", "POST" })
	public void shouldProcessAndReturnProperPayload(String httpMethod) throws Exception {
		MuleEvent testEvent = MuleEventEnricher.enrich(testEvent(SOME_PAYLOAD))
				.withInboundProperties(ImmutableMap.of(HTTP_METHOD, (Object) httpMethod, HTTP_PATH, GOOD_PATH)).get();
		MuleEvent resultEvent = runFlow(POOR_MAN_REST_FLOW, testEvent);

		assertThat(resultEvent, is(notNullValue()));
		assertThat(resultEvent.getMessage(), is(notNullValue()));
		
		MuleMessage message = resultEvent.getMessage();
		assertThat(message.getPayload(), is(notNullValue()));
		assertThat(message.getPayload(), is(instanceOf(HttpResponse.class)));

		HttpResponse httpResponse = (HttpResponse) resultEvent.getMessage().getPayload();
		assertThat(httpResponse.getStatusCode(), is(200));
		assertTrue(httpResponse.getBodyAsString().contains("Processing " + httpMethod));
		
		assertThat((String)message.getInvocationProperty("accountID"), is(ACC_ID));
		assertThat((String)message.getInvocationProperty("userID"), is(USER_ID));
	}

	@SuppressWarnings("unused")
	private Object[] badMethods() {
		return $($(ImmutableMap.of(HTTP_METHOD, "PUT", HTTP_PATH, GOOD_PATH)),
				$(ImmutableMap.of(HTTP_METHOD, "DELETE", HTTP_PATH, GOOD_PATH)),
				$(ImmutableMap.of(HTTP_METHOD, "PATCH", HTTP_PATH, GOOD_PATH)));
	}
}

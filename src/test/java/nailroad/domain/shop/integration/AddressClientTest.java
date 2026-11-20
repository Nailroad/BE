package nailroad.domain.shop.integration;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Optional;
import nailroad.domain.shop.integration.AddressClient.Coordinates;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.twice;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AddressClientTest {

	private static final String BASE_URL = "https://business.juso.go.kr";
	private static final String ADDRESS = "서울특별시 마포구 양화로 123";
	private static final String BUILDING_RESPONSE = """
		{"results":{"common":{"errorCode":"0"},"juso":[{
		  "roadAddrPart1":"서울특별시 마포구 양화로 123",
		  "admCd":"1144012000","rnMgtSn":"114403113005","udrtYn":"0","buldMnnm":"123","buldSlno":"0"
		}]}}
		""";
	private static final String COORDINATE_RESPONSE = """
		{"results":{"common":{"errorCode":"0"},"juso":[{"entX":"1000000","entY":"2000000"}]}}
		""";

	private final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	private final AddressClient client = new AddressClient("search-key", "coordinate-key", builder.build());

	// 좌표 요청이 타임아웃되면 해당 요청만 재시도하고 응답을 위도·경도로 변환하는지 확인한다.
	@Test
	void resolvesCoordinatesAfterRetryingTimeout() {
		server.expect(requestTo(startsWith(BASE_URL + "/addrlink/addrLinkApi.do?")))
			.andExpect(queryParam("confmKey", "search-key"))
			.andExpect(request -> assertTrue(request.getURI().getQuery().contains("keyword=" + ADDRESS)))
			.andRespond(withSuccess(BUILDING_RESPONSE, MediaType.APPLICATION_JSON));
		server.expect(requestTo(startsWith(BASE_URL + "/addrlink/addrCoordApi.do?")))
			.andExpect(queryParam("confmKey", "coordinate-key"))
			.andExpect(queryParam("admCd", "1144012000"))
			.andExpect(queryParam("rnMgtSn", "114403113005"))
			.andExpect(queryParam("udrtYn", "0"))
			.andExpect(queryParam("buldMnnm", "123"))
			.andExpect(queryParam("buldSlno", "0"))
			.andRespond(withException(new SocketTimeoutException("coordinate lookup timed out")));
		server.expect(requestTo(startsWith(BASE_URL + "/addrlink/addrCoordApi.do?")))
			.andRespond(withSuccess(COORDINATE_RESPONSE, MediaType.APPLICATION_JSON));

		// EPSG:5179의 투영 원점은 위도 38도, 경도 127.5도입니다.
		assertEquals(Optional.of(new Coordinates(new BigDecimal("38.0000000"), new BigDecimal("127.5000000"))),
			client.lookup(ADDRESS));
		server.verify();
	}

	// 검색 결과가 입력 주소와 다르면 잘못된 좌표를 사용하지 않는지 확인한다.
	@Test
	void doesNotLookUpCoordinatesForANonMatchingAddress() {
		server.expect(requestTo(startsWith(BASE_URL + "/addrlink/addrLinkApi.do?")))
			.andRespond(withSuccess(BUILDING_RESPONSE, MediaType.APPLICATION_JSON));

		assertTrue(client.lookup("서울특별시 마포구 양화로 160").isEmpty());
		server.verify();
	}

	// 서버 오류가 계속되면 한 번만 재시도하고 좌표 없음으로 처리하는지 확인한다.
	@Test
	void leavesCoordinatesUnresolvedAfterTwoServerErrors() {
		server.expect(twice(), requestTo(startsWith(BASE_URL + "/addrlink/addrLinkApi.do?")))
			.andRespond(withServerError());

		assertTrue(client.lookup(ADDRESS).isEmpty());
		server.verify();
	}
}

package nailroad.domain.shop.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.locationtech.proj4j.Proj4jException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class AddressClient {

	private final String searchKey;
	private final String coordinateKey;
	private final RestClient restClient;
	private final CoordinateTransform toWgs84;

	@Autowired
	public AddressClient(
		@Value("${app.juso.search-key:}") String searchKey,
		@Value("${app.juso.coordinate-key:}") String coordinateKey
	) {
		this(searchKey, coordinateKey, createRestClient());
	}

	AddressClient(String searchKey, String coordinateKey, RestClient restClient) {
		this.searchKey = searchKey;
		this.coordinateKey = coordinateKey;
		this.restClient = restClient;
		CRSFactory factory = new CRSFactory();
		this.toWgs84 = new CoordinateTransformFactory().createTransform(
			factory.createFromName("EPSG:5179"), factory.createFromName("EPSG:4326"));
	}

	private boolean isConfigured() {
		return !searchKey.isBlank() && !coordinateKey.isBlank();
	}

	public Optional<Coordinates> lookup(String address) {
		if (!isConfigured()) {
			return Optional.empty();
		}

		try {
			return findBuilding(address)
				.flatMap(this::findPoint)
				.flatMap(this::convert);
		} catch (RestClientException | IllegalArgumentException | Proj4jException exception) {
			// 예외 원문에는 승인키가 포함된 요청 URL이 있을 수 있습니다.
			log.warn("행안부 좌표를 조회하지 못했습니다. ({})", exception.getClass().getSimpleName());
			return Optional.empty();
		}
	}

	private Optional<Building> findBuilding(String address) {
		AddressResponse response = requestWithRetry(() -> restClient.get()
			.uri(builder -> builder.path("/addrlink/addrLinkApi.do")
				.queryParam("confmKey", "{key}")
				.queryParam("keyword", "{address}")
				.queryParam("currentPage", 1)
				.queryParam("countPerPage", 10)
				.queryParam("resultType", "json")
				.build(searchKey, address))
			.retrieve()
			.body(AddressResponse.class));
		if (response == null || response.results() == null
			|| !isSuccessful(response.results().common()) || response.results().juso() == null) {
			return Optional.empty();
		}
		List<Building> matches = response.results().juso().stream()
			.filter(building -> building != null && building.matches(address))
			.toList();
		return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
	}

	private Optional<ProjectedPoint> findPoint(Building building) {
		CoordinateResponse response = requestWithRetry(() -> restClient.get()
			.uri(builder -> builder.path("/addrlink/addrCoordApi.do")
				.queryParam("confmKey", "{key}")
				.queryParam("admCd", "{admCd}")
				.queryParam("rnMgtSn", "{rnMgtSn}")
				.queryParam("udrtYn", "{udrtYn}")
				.queryParam("buldMnnm", "{buldMnnm}")
				.queryParam("buldSlno", "{buldSlno}")
				.queryParam("resultType", "json")
				.build(coordinateKey, building.admCd(), building.rnMgtSn(), building.udrtYn(),
					building.buldMnnm(), building.buldSlno()))
			.retrieve()
			.body(CoordinateResponse.class));
		if (response == null || response.results() == null || !isSuccessful(response.results().common())
			|| response.results().juso() == null || response.results().juso().size() != 1) {
			return Optional.empty();
		}
		return Optional.ofNullable(response.results().juso().getFirst());
	}

	private <T> T requestWithRetry(Supplier<T> request) {
		try {
			return request.get();
		} catch (ResourceAccessException | HttpServerErrorException exception) {
			if (Thread.currentThread().isInterrupted()) {
				throw exception;
			}
			return request.get();
		}
	}

	private Optional<Coordinates> convert(ProjectedPoint point) {
		if (point.entX() == null || point.entY() == null) {
			return Optional.empty();
		}
		double x = Double.parseDouble(point.entX());
		double y = Double.parseDouble(point.entY());
		if (!Double.isFinite(x) || !Double.isFinite(y) || x <= 0 || y <= 0) {
			return Optional.empty();
		}
		ProjCoordinate position = toWgs84.transform(new ProjCoordinate(x, y), new ProjCoordinate());
		if (!Double.isFinite(position.x) || Math.abs(position.x) > 180
			|| !Double.isFinite(position.y) || Math.abs(position.y) > 90) {
			return Optional.empty();
		}
		return Optional.of(new Coordinates(decimal(position.y), decimal(position.x)));
	}

	private static boolean isSuccessful(Outcome outcome) {
		return outcome != null && "0".equals(outcome.errorCode());
	}

	private static BigDecimal decimal(double value) {
		return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
	}

	private static RestClient createRestClient() {
		Duration timeout = Duration.ofSeconds(2);
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(timeout);
		return RestClient.builder()
			.baseUrl("https://business.juso.go.kr")
			.requestFactory(requestFactory)
			.build();
	}

	public record Coordinates(BigDecimal latitude, BigDecimal longitude) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record AddressResponse(AddressResults results) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record AddressResults(Outcome common, List<Building> juso) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record Building(
		String roadAddr, String roadAddrPart1, String jibunAddr,
		String admCd, String rnMgtSn, String udrtYn, String buldMnnm, String buldSlno
	) {
		boolean matches(String address) {
			return (sameAddress(address, roadAddrPart1) || sameAddress(address, roadAddr)
				|| sameAddress(address, jibunAddr))
				&& admCd != null && rnMgtSn != null && udrtYn != null && buldMnnm != null && buldSlno != null;
		}

		private static boolean sameAddress(String address, String candidate) {
			return candidate != null && candidate.strip().replaceAll("\\s+", " ")
				.equals(address.strip().replaceAll("\\s+", " "));
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record CoordinateResponse(CoordinateResults results) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record CoordinateResults(Outcome common, List<ProjectedPoint> juso) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ProjectedPoint(String entX, String entY) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record Outcome(String errorCode) {
	}
}

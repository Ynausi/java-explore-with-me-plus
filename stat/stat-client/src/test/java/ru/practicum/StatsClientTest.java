package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.ViewStatsResponseDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(StatsClient.class)
@TestPropertySource(properties = "client.url=http://localhost:9090")
public class StatsClientTest {

    @Autowired
    private StatsClient statClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void hit_shouldSendPostRequestCorrectly() throws Exception {
        HitRequestDto requestDto = new HitRequestDto();
        requestDto.setApp("service");
        requestDto.setUri("/events/1");
        requestDto.setIp("192.168.0.1");
        requestDto.setTimestamp("2026-05-19 14:00:00");

        String jsonBody = objectMapper.writeValueAsString(requestDto);

        mockServer.expect(requestTo("http://localhost:9090/hit"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonBody))
                .andRespond(withSuccess());

        statClient.hit(requestDto);

        mockServer.verify();
    }

    @Test
    void getStats_withUris_shouldSendGetRequestWithQueryParams() throws Exception {
        String start = "2026-05-19 00:00:00";
        String end = "2026-05-19 23:59:59";
        List<String> uris = List.of("/events/1", "/events/2");
        boolean unique = true;

        List<ViewStatsResponseDto> expectedResponse = List.of(
                new ViewStatsResponseDto("ewm-main-service", "/events/1", 5),
                new ViewStatsResponseDto("ewm-main-service", "/events/2", 10)
        );
        String jsonResponse = objectMapper.writeValueAsString(expectedResponse);

        mockServer.expect(requestTo("http://localhost:9090/stats?start=2026-05-19%2000:00:00&end=2026-05-19%2023:59:59&unique=true&uris=/events/1&uris=/events/2"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<ViewStatsResponseDto> actualResponse = statClient.getStats(start, end, uris, unique);

        mockServer.verify();
        assertNotNull(actualResponse);
        assertEquals(2, actualResponse.size());
        assertEquals("/events/1", actualResponse.getFirst().getUri());
        assertEquals(5, actualResponse.getFirst().getHits());
    }

    @Test
    void getStats_whenUrisIsNull_shouldSendGetRequestWithoutUrisParam() throws Exception {
        String start = "2026-05-19 00:00:00";
        String end = "2026-05-19 23:59:59";
        boolean unique = false;

        List<ViewStatsResponseDto> expectedResponse = List.of();
        String jsonResponse = objectMapper.writeValueAsString(expectedResponse);

        mockServer.expect(requestTo("http://localhost:9090/stats?start=2026-05-19%2000:00:00&end=2026-05-19%2023:59:59&unique=false"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<ViewStatsResponseDto> actualResponse = statClient.getStats(start, end, null, unique);

        mockServer.verify();
        assertNotNull(actualResponse);
        assertEquals(0, actualResponse.size());
    }
}
package ru.vtb.tester3000.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.vtb.tester3000.config.TesterProperties;

@Component
public class ProductAuthorizationClient {

    private final RestClient restClient;
    private final TesterProperties properties;

    public ProductAuthorizationClient(RestClient.Builder restClientBuilder, TesterProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    public record SendResult(boolean success, int statusCode, String body) {
    }

    public SendResult send(JsonNode authMessage) {
        try {
            var response = restClient.post()
                    .uri(properties.getM210().requestUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-mdm-id", properties.getM210().getMdmId())
                    .body(authMessage)
                    .retrieve()
                    .toEntity(String.class);
            String body = response.getBody() == null ? "" : response.getBody();
            return new SendResult(response.getStatusCode().is2xxSuccessful(), response.getStatusCode().value(), body);
        } catch (RestClientResponseException ex) {
            return new SendResult(false, ex.getStatusCode().value(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            return new SendResult(false, 0, ex.getMessage());
        }
    }
}

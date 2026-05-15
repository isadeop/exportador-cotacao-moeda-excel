package clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class AwesomeApiClient {

    private static final String BASE_URL = "https://economia.awesomeapi.com.br/json/daily/";

    public Optional<String> buscarHistorico(String moedas, int dias) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + moedas + "/" + dias))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(response.body());
            }
        } catch (Exception e) {
            System.err.println("Falha na conexão com a API: " + e.getMessage());
        }
        return Optional.empty();
    }
}
package sptech.school;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import io.github.cdimascio.dotenv.Dotenv;


public class JiraIntegration {
    public static void main(String[] args) throws IOException {
        String summary="teste";
        String description="descicao";
        Dotenv dotenv = Dotenv.load();
        String jiraUrl = dotenv.get("JIRA_URL");
        String email = dotenv.get("JIRA_EMAIL");
        String apiToken = dotenv.get("API_JIRA");

        String auth = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes());

        String json = "{"
                + "\"serviceDeskId\": \"2\","
                + "\"requestTypeId\": \"2\","
                + "\"requestFieldValues\": {"
                + "\"summary\": \"" + summary + "\","
                + "\"description\": \"" + description + "\""
                + "}"
                + "}";

        URL url = new URL(jiraUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        if (conn.getResponseCode() != 201) {
            System.out.println("Erro ao criar chamado: " + conn.getResponseCode());
        }
    }

    public static void abrirChamado(String summary, String description) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String jiraUrl = dotenv.get("JIRA_URL");
        String email = dotenv.get("JIRA_EMAIL");
        String apiToken = dotenv.get("API_JIRA");

        String auth = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes());

        String json = "{"
                + "\"serviceDeskId\": \"2\","
                + "\"requestTypeId\": \"2\","
                + "\"requestFieldValues\": {"
                + "\"summary\": \"" + summary + "\","
                + "\"description\": \"" + description + "\""
                + "}"
                + "}";

        URL url = new URL(jiraUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        if (conn.getResponseCode() != 201) {
            System.out.println("Erro ao criar chamado: " + conn.getResponseCode());
        }
    }
}
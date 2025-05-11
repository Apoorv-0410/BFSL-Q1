package com.bfhl.bfhlsolution;

import com.bfhl.bfhlsolution.model.WebhookResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SolutionRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Step 1: Generate webhook
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            Map<String, String> generateRequest = Map.of(
                "name", "Apoorv Betawadkar",
                "regNo", "0827CD221016",
                "email", "apoorvbetawadkar220949@acropolis.in"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(generateRequest, headers);

            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(generateUrl, entity, WebhookResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("Failed to generate webhook.");
                return;
            }

            String webhookUrl = response.getBody().getWebhook();
            String accessToken = response.getBody().getAccessToken();

            // Step 2: Final SQL query (for Q2)
            String finalQuery = """
                SELECT 
                    E1.EMP_ID,
                    E1.FIRST_NAME,
                    E1.LAST_NAME,
                    D.DEPARTMENT_NAME,
                    COUNT(E2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
                FROM 
                    EMPLOYEE E1
                JOIN 
                    DEPARTMENT D ON E1.DEPARTMENT = D.DEPARTMENT_ID
                LEFT JOIN 
                    EMPLOYEE E2 ON E1.DEPARTMENT = E2.DEPARTMENT
                               AND E2.DOB > E1.DOB
                GROUP BY 
                    E1.EMP_ID, E1.FIRST_NAME, E1.LAST_NAME, D.DEPARTMENT_NAME
                ORDER BY 
                    E1.EMP_ID DESC
                """;

            // Step 3: Submit final query
            HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.setContentType(MediaType.APPLICATION_JSON);
            postHeaders.set("Authorization", accessToken);

            Map<String, String> postBody = Map.of("finalQuery", finalQuery);
            HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(postBody, postHeaders);

            ResponseEntity<String> postResponse = restTemplate.postForEntity(webhookUrl, postEntity, String.class);

            if (postResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("SQL query submitted successfully!");
            } else {
                System.err.println("Failed to submit the SQL query.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

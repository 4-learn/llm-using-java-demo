import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenAIChat {

    public static void main(String[] args) {
        try {
            // 定義 OpenAI API URL
            String apiURL = "https://api.openai.com/v1/chat/completions";
            
            // 定義 API 密鑰（從環境變數中獲取）
            String apiKey = System.getenv("OPENAI_API_KEY");
            
            // 定義請求的 JSON 內容
            String requestBody = "{\n" +
                    "  \"model\": \"gpt-4o\",\n" +
                    "  \"messages\": [\n" +
                    "    {\"role\": \"system\", \"content\": \"You are a health management assistant. Your task is to analyze the user's health data and provide helpful advice.\"},\n" +
                    "    {\"role\": \"user\", \"content\": \"Here is my recent blood pressure reading: {\\\"systolic\\\": 135, \\\"diastolic\\\": 85, \\\"heart_rate\\\": 78, \\\"date\\\": \\\"2024-09-01\\\"}\"},\n" +
                    "    {\"role\": \"assistant\", \"content\": \"Based on your blood pressure reading, your systolic is 135 and your diastolic is 85, which falls into the prehypertension range. Your heart rate is 78, which is within normal limits. It's recommended to monitor your blood pressure regularly, maintain a healthy diet, and stay active to prevent any long-term health issues. Consider consulting a healthcare professional if this trend continues.\"}\n" +
                    "  ]\n" +
                    "}";

            // 創建 HttpClient
            HttpClient client = HttpClient.newHttpClient();
            
            // 創建 HttpRequest，包含標頭和 JSON 請求內容
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            
            // 發送請求並獲取響應
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 打印響應結果
            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

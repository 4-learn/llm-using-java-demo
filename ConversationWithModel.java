import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConversationWithModel {
    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";
    private static final String MODEL = "meta-llama/Llama-3.3-70B-Instruct";
    private static final String HF_TOKEN = System.getenv("HF_TOKEN");

    public static void main(String[] args) {
        if (HF_TOKEN == null || HF_TOKEN.isEmpty()) {
            System.err.println("請設定環境變數 HF_TOKEN");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        String userInput = "";
        StringBuilder conversationHistory = new StringBuilder();

        System.out.println("開始與模型對話（輸入 'exit' 結束）：");

        while (!userInput.equalsIgnoreCase("exit")) {
            System.out.print("你: ");
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                break;
            }

            String modelResponse = generateResponse(userInput, conversationHistory.toString());
            if (modelResponse != null) {
                conversationHistory.append("你: ").append(userInput).append("\n");
                conversationHistory.append("模型: ").append(modelResponse).append("\n");
                System.out.println("模型: " + modelResponse);
            }
        }
        scanner.close();
    }

    public static String generateResponse(String userMessage, String history) {
        try {
            String systemMessage = "你是一個友善的AI助手，會用繁體中文回答問題。";
            if (!history.isEmpty()) {
                systemMessage += "對話記錄：" + history;
            }

            int maxTokens = 150;
            String payload = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + esc(systemMessage) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + esc(userMessage) + "\"}"
                + "],"
                + "\"max_tokens\":" + maxTokens
                + "}";

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line).append('\n');
            }

            conn.disconnect();

            if (status >= 200 && status < 300) {
                return extractContentFromResponse(response.toString());
            } else {
                System.err.println("API 錯誤 (HTTP " + status + "): " + response.toString());
                return null;
            }

        } catch (Exception e) {
            System.err.println("請求失敗: " + e.getMessage());
            return null;
        }
    }

    private static String extractContentFromResponse(String jsonResponse) {
        try {
            int contentStart = jsonResponse.indexOf("\"content\":\"") + 11;
            int contentEnd = jsonResponse.indexOf("\"", contentStart);
            if (contentStart > 10 && contentEnd > contentStart) {
                return jsonResponse.substring(contentStart, contentEnd)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
            }
        } catch (Exception e) {
            System.err.println("解析回應失敗: " + e.getMessage());
        }
        return "解析回應時發生錯誤";
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
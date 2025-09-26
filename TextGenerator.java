import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Hugging Face API 簡潔模板
 * - 端點: https://router.huggingface.co/v1/chat/completions
 * - 移除多餘日誌，保持輸出簡潔
 * 
 * 使用：HF_TOKEN=你的token java TextGenerator
 */
public class TextGenerator {
    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";
    private static final String MODEL = "meta-llama/Llama-3.3-70B-Instruct";
    private static final String HF_TOKEN = System.getenv("HF_TOKEN");

    public static void main(String[] args) {
        // 檢查 Token
        if (HF_TOKEN == null || HF_TOKEN.isEmpty()) {
            System.err.println("請設定環境變數 HF_TOKEN");
            System.exit(1);
        }

        // 設定請求參數
        String userMessage = "Hello 我來自台灣，請聊聊你自己吧！.";
        int maxTokens = 100;

        String payload = "{"
            + "\"model\":\"" + MODEL + "\","
            + "\"messages\":[{\"role\":\"user\",\"content\":\"" + esc(userMessage) + "\"}],"
            + "\"max_tokens\":" + maxTokens
            + "}";

        try {
            // 建立連接
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // 發送請求
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            // 讀取回應
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line).append('\n');
            }

            // 輸出結果
            if (status >= 200 && status < 300) {
                System.out.println(response.toString());
            } else {
                System.err.println("API 錯誤 (HTTP " + status + "): " + response.toString());
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("請求失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
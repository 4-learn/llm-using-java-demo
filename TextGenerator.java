import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TextGenerator {

    private static final String API_URL = "https://api-inference.huggingface.co/models/gpt2";
    private static final String API_KEY = "";

    public static void main(String[] args) {
        try {
            // 設定 Hugging Face API 的 URL
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 構建輸入資料，生成 "Hello" 後的回應
            String input = "{\"inputs\":\"Hello\"}";

            // 發送請求
            OutputStream os = connection.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            os.close();

            // 接收回應
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine);
            }
            br.close();

            // 顯示生成結果
            System.out.println("Model response: " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

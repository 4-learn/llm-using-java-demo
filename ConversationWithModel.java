import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ConversationWithModel {

    private static final String API_URL = "https://api-inference.huggingface.co/models/gpt2";
    // 將您的 Hugging Face access token 直接放入這裡
    private static final String API_KEY = "";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String userInput = "";
        String conversationHistory = "";  // 用來存儲對話的上下文

        System.out.println("開始與模型對話（輸入 'exit' 結束）：");

        while (!userInput.equalsIgnoreCase("exit")) {
            System.out.print("你: ");
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                break;
            }

            // 將用戶的輸入加到對話歷史中
            conversationHistory += "你: " + userInput + "\n";

            // 發送請求給模型
            String modelResponse = generateResponse(conversationHistory);
            if (modelResponse != null) {
                conversationHistory += "模型: " + modelResponse + "\n";  // 更新對話歷史
                System.out.println("模型: " + modelResponse);
            }
        }
        scanner.close();
    }

    // 發送請求給 Hugging Face 模型，並返回生成的文本
    public static String generateResponse(String conversationHistory) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 將對話歷史中的換行符號轉換為 \n，並保證 JSON 格式正確
            conversationHistory = conversationHistory.replace("\n", "\\n");

            // 將雙引號轉義，以保證 JSON 格式
            conversationHistory = conversationHistory.replace("\"", "\\\"");

            // 限制對話歷史的長度，避免過長的對話上下文
            if (conversationHistory.length() > 1000) {
                conversationHistory = conversationHistory.substring(conversationHistory.length() - 1000);
            }

            // 構建 JSON 請求體
            String input = "{\"inputs\":\"" + conversationHistory + "\"}";

            OutputStream os = connection.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String responseLine;
                StringBuilder response = new StringBuilder();
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
                br.close();

                // 清理模型回應中的特殊字符
                String modelResponse = response.toString();
                modelResponse = modelResponse.replaceAll("[{}\\[\\]\"]", ""); // 去掉JSON結構中的特殊字符

                return modelResponse;
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String responseLine;
                StringBuilder response = new StringBuilder();
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
                br.close();

                // 檢查是否是模型正在加載
                if (response.toString().contains("Model is currently loading")) {
                    System.out.println("模型正在加載，請稍候...");

                    // 提取估計的加載時間
                    if (response.toString().contains("estimated_time")) {
                        String[] splitResponse = response.toString().split("estimated_time\":");
                        String estimatedTime = splitResponse[1].split("}")[0].trim();
                        double waitTime = Double.parseDouble(estimatedTime);

                        // 休眠指定的時間後重新發送請求
                        System.out.println("等待大約 " + (int)waitTime + " 秒...");
                        Thread.sleep((long)(waitTime * 1000));
                        return generateResponse(conversationHistory); // 再次重試
                    }
                }

                System.out.println("錯誤: " + response.toString());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

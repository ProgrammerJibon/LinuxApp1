package pro.jibon.apps.linuxapp1;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static pro.jibon.apps.linuxapp1.CustomTools.AlertBox;

public class Internet3 extends Task<JSONObject> implements Runnable{

    private final TaskListener taskListener;
    private final String boundary = "*****";
    private final String lineEnd = "\r\n";
    String twoHyphens = "--";
    private String url;
    private Integer code = 0;
    private Map<String, String> inputs = null;
    private Map<String, Image> files = null;

    private String allLines = "";
    Stage primaryStage;

    public Internet3(Stage primaryStage, String url, TaskListener listener) {
        this.primaryStage = primaryStage;
        this.url = url;
        this.taskListener = listener;
    }

    public Internet3(Stage primaryStage, String url, Map<String, String> inputs, TaskListener listener) {
        this.primaryStage = primaryStage;
        this.url = url;
        this.inputs = inputs;
        this.taskListener = listener;
    }

    public Internet3(Stage primaryStage, String url, Map<String, String> inputs, Map<String, Image> files, TaskListener listener) {
        this.primaryStage = primaryStage;
        this.url = url;
        this.inputs = inputs;
        this.files = files;
        this.taskListener = listener;
    }

    @Override
    protected JSONObject call() {
        try {
            URL newLink = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) newLink.openConnection();

            // Set up connection properties
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            // Get the output stream from the connection
            OutputStream outputStream = httpURLConnection.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // Add parameters
            if (inputs != null) {
                for (Map.Entry<String, String> entry : inputs.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (!key.equals("") && !value.equals("")) {
                        addFormField(key, value, dataOutputStream);
                    }
                }
            }

            // Add images
            if (files != null) {
                for (Map.Entry<String, Image> entry : files.entrySet()) {
                    String key = entry.getKey();
                    Image value = entry.getValue();
                    if (!key.equals("") && value != null) {
                        addFilePart(key, value, dataOutputStream);
                    }
                }
            }

            // Finalize the request
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();

            // Get the HTTP response code
            this.code = httpURLConnection.getResponseCode();

            // Get the response content
            List<String> cookieList = httpURLConnection.getHeaderFields().get("Set-Cookie");
            if (cookieList != null) {
                for (String cookieTemp : cookieList) {
                    // handle cookies if needed
                }
            }

            // Process the response and return a JSONObject
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            allLines = stringBuilder.toString();
            return new JSONObject(allLines);

        } catch (Exception e) {
            // Log the exception or handle it as needed
            AlertBox(primaryStage, e.getMessage());
            return null;
        }
    }


    private void addFormField(String fieldName, String fieldValue, OutputStream outputStream) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(twoHyphens).append(boundary).append(lineEnd);
            builder.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"").append(lineEnd);
            builder.append(lineEnd);
            builder.append(fieldValue).append(lineEnd);

            outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFilePart(String paramName, Image image, OutputStream outputStream) {
        try {
            String fileName = "image-" + Math.random() + ".png";
            String contentType = "image/png";

            // Create the file part header
            StringBuilder sb = new StringBuilder();
            sb.append(twoHyphens).append(boundary).append(lineEnd);
            sb.append("Content-Disposition: form-data; name=\"").append(paramName).append("\"; filename=\"").append(fileName).append("\"").append(lineEnd);
            sb.append("Content-Type: ").append(contentType).append(lineEnd);
            sb.append(lineEnd);
            outputStream.write(sb.toString().getBytes());

            // Convert the JavaFX Image to a BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            // Write the image data to the output stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageData = baos.toByteArray();
            outputStream.write(imageData);

            // Add the closing boundary
            outputStream.write(lineEnd.getBytes());
            outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void succeeded() {
        super.succeeded();
        taskListener.onFinished(code, getValue());
    }

    public interface TaskListener {
        void onFinished(Integer code, JSONObject result);
    }
}

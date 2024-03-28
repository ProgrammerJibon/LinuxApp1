package pro.jibon.apps.linuxapp1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class CurrencyDetectionApp extends Application {
    private ImageView imageView = new ImageView();
    private VideoCapture capture;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Load OpenCV
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

        // Set up the JavaFX stage
        primaryStage.setTitle("OpenCV Webcam Example");
        StackPane root = new StackPane();
        root.getChildren().add(imageView);
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();

        // Open the default camera (usually 0)
        capture = new VideoCapture(0);

        // Start the video capture thread
        startCapturing();
    }

    private void startCapturing() {
        new Thread(() -> {
            Mat frame = new Mat();
            MatOfByte buffer = new MatOfByte();

            while (!Thread.interrupted()) {
                if (capture.read(frame)) {
                    // Process the frame if needed
                    // e.g., detect faces
                    detectFaces(frame);

                    // Convert Mat to BufferedImage
                    Imgcodecs.imencode(".png", frame, buffer);
                    byte[] imageData = buffer.toArray();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);

                    try {
                        BufferedImage bufferedImage = javax.imageio.ImageIO.read(inputStream);
                        Image fxImage = javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null);

                        // Update JavaFX UI in the JavaFX Application Thread
                        javafx.application.Platform.runLater(() -> imageView.setImage(fxImage));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void detectFaces(Mat frame) {
        // Your face detection code here using CascadeClassifier
        // For example:
        CascadeClassifier faceCascade = new CascadeClassifier("path/to/haarcascade_frontalface_default.xml");
        MatOfRect faceDetections = new MatOfRect();
        faceCascade.detectMultiScale(frame, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 2);
        }
    }

    @Override
    public void stop() {
        // Release the camera when the application is closed
        capture.release();
    }
}
package pro.jibon.apps.linuxapp1;

import com.pi4j.io.gpio.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import static pro.jibon.apps.linuxapp1.CustomTools.AlertBox;
import static pro.jibon.apps.linuxapp1.CustomTools.isRasPi;

public class HelloApplication extends Application {
    @FXML
    ProgressBar progressBar;
    @FXML
    private Label welcomeText;
    @FXML
    private Button button1, button2, ledBtn;
    @FXML
    private TextField degree, xTimes, urlInput;
    @FXML
    private WebView webView;
    private GpioController gpio;
    private GpioPinDigitalOutput[] pins;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();


        /* Internet3 internet3 = new Internet3(stage, "http://127.0.0.1/json", (code, result) -> {
            // Handle the result here
            AlertBox(stage, "Response: " + result);
        });
        internet3.run();*/

        button1 = (Button) scene.lookup("#btn1");
        button2 = (Button) scene.lookup("#btn2");
        ledBtn = (Button) scene.lookup("#ledPower");
        degree = (TextField) scene.lookup("#degree");
        xTimes = (TextField) scene.lookup("#xTimes");
        urlInput = (TextField) scene.lookup("#urlInput");
        webView = (WebView) scene.lookup("#webView");
        progressBar = (ProgressBar) scene.lookup("#progressBar");
        welcomeText = (Label) scene.lookup("#welcomeText");

        if (CustomTools.isRasPi()) {
            gpio = GpioFactory.getInstance();
            pins = new GpioPinDigitalOutput[]{
                    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14, PinState.LOW),
                    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10, PinState.LOW),
                    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, PinState.LOW),
                    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, PinState.LOW)
            };
        }
        ledBtn.setOnAction(mouseEvent -> {
            if (Integer.parseInt(degree.getText()) < 1) {
                AlertBox(stage, "Invalid degree");
                degree.requestFocus();
            } else if (Integer.parseInt(xTimes.getText()) < 1) {
                AlertBox(stage, "Invalid degree x Times");
                xTimes.requestFocus();
            } else {
                runStepper(Integer.parseInt(degree.getText()) * Integer.parseInt(xTimes.getText()));
            }
        });


        button1.setText("Close");
        button1.setOnAction(mouseEvent -> {
            stage.close();
            if (isRasPi()) {
                try {
                    for (GpioPinDigitalOutput pin : pins) {
                        gpio.unprovisionPin(pin);
                    }
                    gpio.shutdown();
                } catch (Exception e) {

                }
            }
        });

        //webview test
        WebEngine webEngine = webView.getEngine();
        progressBar.progressProperty().bind(webView.getEngine().getLoadWorker().progressProperty());
        button2.setText("Open WebView");
        button2.setOnAction(actionEvent -> {
            webEngine.load(urlInput.getText());
        });
        button2.fire();
        webEngine.titleProperty().addListener((observableValue, s, t1) -> {
            welcomeText.setText(s);
        });
        webEngine.setJavaScriptEnabled(true);
        webView.setContextMenuEnabled(true);

        // primary settings
        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
        });
        /*stage.setAlwaysOnTop(true);*/
        stage.setResizable(false);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            stage.setFullScreen(true);
        });
    }

    public GpioController runStepper(int deg) {
        gpio.shutdown();
        gpio = GpioFactory.getInstance();

        // Define the stepping sequence for full-step mode
        int[][] sequence = {
                {1, 0, 0, 1},
                {1, 0, 0, 0},
                {1, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 1, 0},
                {0, 0, 1, 0},
                {0, 0, 1, 1},
                {0, 0, 0, 1}
        };

        // Number of steps per revolution for the 28BYJ-48 stepper motor in full-step mode
        int stepsPerRevolution = 512;

        // Calculate the number of steps based on the provided degrees
        int steps = (int) ((deg / 360.0) * stepsPerRevolution);

        // Rotate the stepper motor in full-step mode
        for (int i = 0; i < steps; i++) {
            for (int[] step : sequence) {
                for (int j = 0; j < pins.length; j++) {
                    pins[j].setState((step[j] == 1) ? PinState.HIGH : PinState.LOW);
                }
                try {
                    Thread.sleep(2); // Adjust the delay based on your motor's speed
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (GpioPinDigitalOutput pin : pins) {
            pin.setState(PinState.LOW);
        }
        return gpio;
    }

}


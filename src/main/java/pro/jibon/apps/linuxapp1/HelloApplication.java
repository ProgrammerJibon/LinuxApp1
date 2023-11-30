package pro.jibon.apps.linuxapp1;

import com.pi4j.io.gpio.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import static pro.jibon.apps.linuxapp1.CustomTools.AlertBox;
import static pro.jibon.apps.linuxapp1.CustomTools.isRasPi;

public class HelloApplication extends Application {
    @FXML
    private Button button1, ledBtn;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();


        Internet3 internet3 = new Internet3(stage, "http://127.0.0.1/json", (code, result) -> {
            // Handle the result here
            AlertBox(stage, "Response: " + result);
        });
        internet3.run();

        button1 = (Button) scene.lookup("#btn");
        ledBtn = (Button) scene.lookup("#ledPower");
        GpioController gpio = null;
        GpioPinDigitalOutput ledPin;
        int ledPinNumber = 2; //16
        if (isRasPi()){
            try{
                gpio = GpioFactory.getInstance();
                ledPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(ledPinNumber), PinState.LOW);
            }catch (Exception e){
                ledPin = null;
            }
        }else {
            ledPin = null;
        }

        GpioPinDigitalOutput finalLedPin = ledPin;
        ledBtn.setOnAction(mouseEvent->{
            ledBtn.setText("Wait.");
            if (finalLedPin != null){
                ledBtn.setText("Wait..");
                try{
                    if (finalLedPin.getState() == PinState.LOW){
                        ledBtn.setText("Turning LED ON");
                        finalLedPin.high();
                    }else{
                        ledBtn.setText("Turning LED OFF");
                        finalLedPin.low();
                    }
                }catch (Exception e){
                    //AlertBox(e.getMessage());
                }
            }else{
                ledBtn.setText("Unsupported");
            }
        });

        button1.setText("Close");
        button1.setOnAction(mouseEvent -> {
            stage.close();
        });
        stage.setOnCloseRequest(windowEvent -> {
            if (isRasPi()) {
                try {
                    GpioController gpio2 = GpioFactory.getInstance();
                    gpio2.shutdown();
                }catch (Exception e){

                }
            }
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

    public static void main(String[] args) {
        launch();
    }

}

package pro.jibon.apps.linuxapp1;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import static com.pi4j.system.SystemInfo.getOsName;

public class CustomTools {
    Stage primaryStage;
    public CustomTools(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public static void AlertBox(Stage primaryStage, String string){
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Alert");
        alert.setContentText(string);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(windowEvent -> {
            alert.close();
        });
        stage.initOwner(primaryStage);
        alert.show();
        return;
    }
    public void AlertBox(String string){
        AlertBox(primaryStage, string);
    }
    public static boolean isLinux() {
        return (getOsName().toLowerCase().contains("linux"));
    }
    public static boolean isRasPi() {
        return (isLinux() && Files.exists(Paths.get("/opt/pigpio"), LinkOption.NOFOLLOW_LINKS));
    }
}

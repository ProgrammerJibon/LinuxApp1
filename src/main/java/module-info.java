module pro.jibon.apps.linuxapp1 {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires pi4j.core;
    requires org.json;
    requires java.desktop;
    requires javafx.swing;


    opens pro.jibon.apps.linuxapp1 to javafx.fxml;
    exports pro.jibon.apps.linuxapp1;
}
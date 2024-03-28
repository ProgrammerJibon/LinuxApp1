module pro.jibon.apps.linuxapp {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires pi4j.core;
    requires org.json;
    requires java.desktop;
    requires javafx.swing;
    requires webcam.capture;
    requires opencv;
    requires javafx.web;


    opens pro.jibon.apps.linuxapp1 to javafx.fxml;
    exports pro.jibon.apps.linuxapp1;
}
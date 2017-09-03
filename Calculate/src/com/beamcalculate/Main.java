package com.beamcalculate;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class Main extends Application {

    private BorderPane borderPane = new BorderPane();
    private StringProperty windowTitle = new SimpleStringProperty();
    private StringProperty menuText = new SimpleStringProperty();
    private Map<Locale,StringProperty> languagesItems = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        borderPane.setTop(createMenuBar());
        loadView(Locale.getDefault());
        primaryStage.titleProperty().bind(windowTitle);
        primaryStage.setScene(new Scene(borderPane, 1020, 850));
        primaryStage.getIcons().add(new Image("image/main.png"));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu();
        menu.textProperty().bind(menuText);
        getSupportedLocales().forEach(locale -> {
            MenuItem item = new MenuItem();
            StringProperty languageItem = new SimpleStringProperty();
            languagesItems.put(locale,languageItem);
            item.textProperty().bind(languageItem);

            item.setOnAction(event -> {
                loadView(locale);
            });
            menu.getItems().add(item);
        });
        menuBar.getMenus().addAll(menu);
        return menuBar;
    }

    public static String getBundleText(String key){
        ResourceBundle bundle = ResourceBundle.getBundle("UIResources", Main.AppSettings.currentLocal);
        return bundle.getString(key);
    }

    public static List<Locale> getSupportedLocales() {
        return new ArrayList<>(Arrays.asList(Locale.US, Locale.FRANCE, Locale.CHINA));
    }

    private void loadView(Locale locale) {
        try {
            Pane pane = FXMLLoader.load(
                    getClass().getResource("/fxml/main.fxml"),
                    ResourceBundle.getBundle("UIResources", locale)
            );
            borderPane.setCenter(pane);
            AppSettings.currentLocal = locale;
            windowTitle.setValue(getBundleText("window.title.main"));
            menuText.setValue(getBundleText("menu.languages"));
            languagesItems.forEach((itemLocale, languageItem) -> {
                if(locale.equals(itemLocale)){
                    languageItem.setValue(getBundleText("menuItem." + itemLocale.getLanguage()));

                } else {
                    languageItem.setValue(
                            ResourceBundle.getBundle("UIResources", itemLocale).getString("menuItem." + itemLocale.getLanguage())
                                    + "("
                                    + getBundleText("menuItem." + itemLocale.getLanguage())
                                    + ")"
                    );
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static class AppSettings {
        public static Locale currentLocal;
    }


    public static void main(String[] args) {
        launch(args);
    }
}

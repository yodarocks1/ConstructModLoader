/*
 * Copyright (C) 2020 benne
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cml.gui.splash;

import cml.Media;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author benne
 */
public class SplashScreenLoader extends Preloader {

    /**
     * Splash image
     */
    public static final Image SPLASH = new Image(SplashScreenLoader.class.getClassLoader().getResourceAsStream("media/Splash.png"));
    /**
     * Splash image (easter egg)
     */
    public static final Image SPLASH_EGG = new Image(SplashScreenLoader.class.getClassLoader().getResourceAsStream("media/SplashEgg.png"));
    /**
     * CML Icon
     */
    public static final Image ICON = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Icon/CML.ico.png"));

    private static SplashScreenLoader splasher;

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {

        splasher = this;

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.getIcons().add(ICON);

        StackPane root = new StackPane();

        ImageView splash = new ImageView(SPLASH);
        splash.setFitHeight(400);
        splash.setFitWidth(400);

        root.getChildren().setAll(splash);

        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        splash.setOnMouseClicked((event) -> {
            if (event.isControlDown()) {
                splash.setImage(SPLASH_EGG);
            }
        });

        this.stage = stage;
        stage.show();
    }

    /**
     * Closes the splash screen
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public static void close() {
        splasher.stage.close();
    }

}

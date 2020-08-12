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
package cml.gui.popup;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author benne
 * @param <R> Result type
 * @param <D> Button data (used for result conversion)
 */
public class CmlPopup<R, D> implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(CmlPopup.class.getName());

    private Stage stage;
    private PopupData<R, D> contentManager;
    @FXML private Node drag;
    @FXML private ButtonBar buttonBar;
    @FXML private AnchorPane contentPane;
    @FXML private Text title;
    private final Object closeLock = new Object();
    private volatile boolean isClosed = false;

    @SuppressWarnings("LeakingThisInConstructor")
    public CmlPopup(Modality modality, PopupData<R, D> contentManager) {
        this(modality, null, contentManager);
    }

    public CmlPopup(Modality modality, Window owner, PopupData<R, D> contentManager) {
        this.contentManager = contentManager;
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Popup.fxml"));
                loader.setController(this);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                this.stage = new Stage();
                this.stage.setScene(scene);
                if (owner != null) {
                    this.stage.initOwner(owner);
                }
                this.stage.initStyle(StageStyle.TRANSPARENT);
                this.stage.initModality(modality);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not load popup", ex);
            }
        });
    }

    public final void setContentManager(PopupData<R, D> contentManager) {
        this.contentManager = contentManager;
        contentManager.doSetup(this);
        contentPane.getChildren().setAll(contentManager.toNode());
        buttonBar.getButtons().setAll(contentManager.getButtons());
        contentManager.getButtons().stream().filter((button) -> (contentManager.doClose(button))).forEachOrdered((button) -> {
            button.setOnAction((event) -> {
                contentManager.pressed(button);
                close();
            });
        });
    }
    
    public final PopupData<R, D> getContentManager() {
        return this.contentManager;
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public R getResult() {
        return contentManager.getResult();
    }

    private double dragXOffset = 0;
    private double dragYOffset = 0;
    private double stageOpacity = 1;

    public void makeDraggable() {
        drag.setOnMousePressed((event) -> {
            dragXOffset = stage.getX() - event.getScreenX();
            dragYOffset = stage.getY() - event.getScreenY();
            drag.setCursor(Cursor.MOVE);
            stageOpacity = stage.getOpacity();
            stage.setOpacity(stageOpacity * 0.75);
        });
        drag.setOnMouseReleased((event) -> {
            drag.setCursor(Cursor.DEFAULT);
            stage.setOpacity(stageOpacity);
        });
        drag.setOnMouseDragged((event) -> {
            stage.setX(event.getScreenX() + dragXOffset);
            stage.setY(event.getScreenY() + dragYOffset);
        });

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setContentManager(this.contentManager);
    }
    
    public Window asWindow() {
        return stage;
    }

    //Delegate methods
    public final void show() {
        isClosed = false;
        Platform.runLater(() -> {
            stage.show();
        });
    }

    public CompletableFuture<R> showAndGet() {
        show();
        return CompletableFuture.supplyAsync(() -> {
            synchronized (closeLock) {
                while (!isClosed) {
                    try {
                        closeLock.wait();
                    } catch (InterruptedException ex) {
                        
                    }
                }
                return contentManager.getResult();
            }
        });
    }
    
    public void showThenRun(BiConsumer<? super R, ? super Throwable> action) {
        CompletableFuture.supplyAsync(() -> {
            synchronized (closeLock) {
                while (!isClosed) {
                    try {
                        closeLock.wait();
                    } catch (InterruptedException ex) {
                        
                    }
                }
                return contentManager.getResult();
            }
        }).whenComplete(action);
        show();
    }

    public void close() {
        stage.close();
        synchronized (closeLock) {
            isClosed = true;
            closeLock.notifyAll();
        }
    }

}

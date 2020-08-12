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

import cml.Media;
import cml.lib.threadmanager.ThreadManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author benne
 */
public class CmlPopover implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(CmlPopover.class.getName());
    private static final double TRANSITION_DISTANCE = 300;
    private static final double VERTICAL_HEIGHT = 36;
    private static final double VERTICAL_OFFSET = 96;
    private static final boolean FROM_BOTTOM = false;

    private final PopoverType type;
    private final String leftT;
    private final String centerT;
    private final String rightT;

    private final CountDownLatch initializedLatch = new CountDownLatch(1);

    private Stage stage = null;
    private Scene scene = null;

    @FXML private ImageView icon;
    @FXML private Text left;
    @FXML private Text center;
    @FXML private Text right;

    private CmlPopover(Window owner, PopoverType type, PopoverColor color, String left, String center, String right) {
        this.type = type;
        this.leftT = (left == null) ? "" : left;
        this.centerT = (center == null) ? "" : center;
        this.rightT = (right == null) ? "" : right;
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Popover.fxml"));
                loader.setController(this);
                Parent root = loader.load();
                root.getStyleClass().add(color.styleClass);
                root.setOpacity(0.0);
                scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                stage = new Stage();
                stage.setScene(scene);
                stage.initOwner(owner);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.initModality(Modality.NONE);
                stage.show();
                initializedLatch.countDown();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not create/load Popover", ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.icon.setImage(type.image);
        this.left.setText(leftT);
        this.center.setText(centerT);
        this.right.setText(rightT);
    }

    private Stage getStage() {
        return stage;
    }

    private Scene getScene() {
        return scene;
    }

    //STATIC
    public static void popover(Window owner, PopoverType type, PopoverColor color, String text, int duration, TimeUnit unit) {
        popover(owner, type, color, text, "", "", duration, unit);
    }

    public static void popover(Window owner, PopoverType type, String text, int duration, TimeUnit unit) {
        popover(owner, type, text, "", "", duration, unit);
    }

    public static void popover(Window owner, PopoverType type, String left, String center, String right, int duration, TimeUnit unit) {
        popover(owner, type, type.defaultColor, left, center, right, duration, unit);
    }

    public static void popover(Window owner, PopoverType type, PopoverColor color, String left, String center, String right, int duration, TimeUnit unit) {
        popover(owner, type, color, left, center, right, 1, duration, unit);
    }

    public static void popover(Window owner, PopoverType type, PopoverColor color, String left, String center, String right, int slots, int duration, TimeUnit unit) {
        ThreadManager.MANAGER.executor.submit(() -> pop(owner, type, color, left, center, right, slots, duration, unit));
    }

    private static void pop(Window owner, PopoverType type, PopoverColor color, String left, String center, String right, int slots, int duration, TimeUnit unit) {
        final CmlPopover popover = new CmlPopover(owner, type, color, left, center, right);
        try {
            popover.initializedLatch.await();
        } catch (InterruptedException ex) {
        }
        final Parent popRoot = popover.getScene().getRoot();
        final Animation transitionIn = new Transition() {
            {
                setCycleDuration(Duration.seconds(slots * 1.5));
                setInterpolator(Interpolator.EASE_OUT);
            }

            @Override
            protected void interpolate(double frac) {
                popRoot.setTranslateX((frac - 1.0) * TRANSITION_DISTANCE);
                popRoot.setOpacity(frac);
                popRoot.setScaleX(frac);
                popRoot.setScaleY(Math.sqrt(frac));
            }
        };
        final Animation transitionOut = new Transition() {
            {
                setCycleDuration(Duration.seconds(slots * 1.5));
                setInterpolator(Interpolator.EASE_IN);
            }

            @Override
            protected void interpolate(double frac) {
                popRoot.setTranslateX(frac * TRANSITION_DISTANCE);
                popRoot.setOpacity(1.0 - frac);
                popRoot.setScaleX(1.0 - frac);
                popRoot.setScaleY(Math.sqrt(1.0 - frac));
            }
        };

        final CountDownLatch lock = new CountDownLatch(1);
        POP_QUEUE.acquireShared(slots);
        final int showSlot = lockShowSlot();
        
        transitionIn.setOnFinished((event) -> {
            lock.countDown();
        });
        transitionOut.setOnFinished((event) -> {
            popover.getStage().hide();
            POP_QUEUE.releaseShared(slots);
            releaseShowSlot(showSlot);
        });
        
        Platform.runLater(() -> {
            popover.getStage().setX((owner.getX() + (owner.getWidth() / 2)) - (popover.getStage().getWidth() / 2));
            if (!FROM_BOTTOM) {
                popover.getStage().setY(owner.getY() + VERTICAL_OFFSET + (VERTICAL_HEIGHT * showSlot));
            } else {
                popover.getStage().setY(owner.getY() + owner.getHeight() - VERTICAL_OFFSET - (VERTICAL_HEIGHT * showSlot));
            }
            transitionIn.play();
        });

        try {
            lock.await();
            Thread.sleep(unit.toMillis(duration));
        } catch (InterruptedException ex) {
        }

        Platform.runLater(() -> {
            transitionOut.play();
        });
    }

    private static final AtomicReferenceArray<Boolean> SHOWING = new AtomicReferenceArray(new Boolean[]{false, false, false, false, false});

    private static int lockShowSlot() {
        for (int i = 0; i < SHOWING.length(); i++) {
            if (!SHOWING.get(i)) {
                SHOWING.set(i, true);
                return i;
            }
        }
        return 5;
    }

    private static void releaseShowSlot(int slot) {
        SHOWING.set(slot, false);
    }

    private static final AbstractQueuedSynchronizer POP_QUEUE = new AbstractQueuedSynchronizer() {

        @Override
        protected int tryAcquireShared(int reserveSlots) {
            int val = 5 - getState() - reserveSlots;
            if (val >= 0) {
                setState(getState() + reserveSlots);
            }
            return val;
        }

        @Override
        protected boolean tryReleaseShared(int reservedSlots) {
            setState(getState() - reservedSlots);
            return true;
        }

    };

    public enum PopoverColor {
        GRAY("gray"), GREEN("green"), BLUE("blue"), YELLOW("yellow"), ORANGE("orange"), RED("red");
        public final String styleClass;

        private PopoverColor(String styleClass) {
            this.styleClass = styleClass;
        }
    }

    public enum PopoverType {
        CHECK(Media.CHECK, PopoverColor.GREEN), X(Media.X, PopoverColor.RED), PLUS(Media.PLUS, PopoverColor.BLUE), MINUS(Media.MINUS, PopoverColor.ORANGE), DOT(Media.DOT, PopoverColor.GRAY);
        public final Image image;
        public final PopoverColor defaultColor;

        private PopoverType(Image image, PopoverColor defaultColor) {
            this.image = image;
            this.defaultColor = defaultColor;
        }
    }

}

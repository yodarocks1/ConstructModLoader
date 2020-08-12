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
package cml.gui.workshop;

import cml.ErrorManager;
import cml.Media;
import cml.beans.Profile;
import cml.gui.main.MainController;
import cml.lib.animation.Animation;
import cml.lib.animation.AnimationFrame;
import cml.lib.threadmanager.ThreadManager;
import cml.lib.workshop.WorkshopConnectionHandler;
import cml.lib.workshop.WorkshopMod;
import cml.lib.xmliconmap.CMLIconMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javax.imageio.ImageIO;

/**
 *
 * @author benne
 */
public class WorkshopModData implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(WorkshopModData.class.getName());

    private WorkshopController controller;
    protected final WorkshopMod mod;
    private final DoubleProperty listWidth = new SimpleDoubleProperty();
    private boolean initialized = false;

    @FXML private AnchorPane root;
    @FXML private ImageView image;
    @FXML private Text header;
    @FXML private TextFlow description;
    @FXML private Text properties;
    @FXML private ImageView showInWorkshop;
    @FXML private ImageView convert;
    @FXML private Text connectedCount;

    public WorkshopModData(WorkshopMod item) {
        this.mod = item;
    }

    public void doInit(WorkshopController controller) {
        this.controller = controller;
        if (!initialized) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("WorkshopListItem.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not read FXML file", ex);
            }
            initialized = true;
        }
    }

    public void setInfo(DoubleProperty listWidth) {
        this.listWidth.unbind();
        this.listWidth.bind(listWidth);
    }

    public Node toNode() {
        return root;
    }

    public void showInWorkshop() {
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler steam://url/SteamWorkshopPage/" + mod.getWorkshopId());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open mod #" + mod.getWorkshopId() + " in the Steam Workshop", ex);
        }
    }

    public void convert() {
        if (mod.isApplicable()) {
            Profile destination = controller.getDestination();
            if (destination != null) {
                Media.CONVERT_PRESS_ANIM.animate(convert);
                Thread convertThread = new Thread(() -> {
                    WorkshopConnectionHandler.connectAndConvertInto(mod, destination.getDirectory());
                });
                ThreadManager.addThread(convertThread);
                convertThread.start();
            } else {
                LOGGER.log(Level.SEVERE, "Could not convert mod #{0} because the workshop destination profile has not been set", mod.getWorkshopId());
                ErrorManager.addStateCause("WorkshopDestination == null");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MainController.setImageMouseHandlers(showInWorkshop, CMLIconMap.ICON_MAP.WORKSHOP);
        if (mod.isApplicable()) {
            MainController.setImageMouseHandlers(convert, Media.CONVERT, Media.CONVERT_SELECT_ANIM, Media.CONVERT_PRESS_ANIM, false);
        }
        if (mod.getPreview().exists()) {
            try {
                this.image.setImage(new javafx.scene.image.Image(new FileInputStream(mod.getPreview())));
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.FINE, "Mod #{0} does not have a preview.", mod.getWorkshopId());
            }
        }
        WorkshopConnectionHandler.connectionCount.valueAt(mod).addListener((obs, oldValue, newValue) -> {
            this.connectedCount.setText((newValue == null) ? "0" : newValue.toString());
        });
        this.header.setText(mod.getName());
        this.root.setPrefWidth((listWidth.getValue() != null) ? listWidth.doubleValue() : (this.header.prefWidth(-1) + 274));
        this.root.prefWidthProperty().bind(listWidth);

        this.description.getChildren().setAll(parseWorkshopText(mod.getDescription()));
        double newHeight = Math.max(description.prefHeight(description.prefWidth(-1)) + 52, 230 + properties.prefHeight(-1)) + 12;
        root.setMinHeight(newHeight);
        root.setPrefHeight(newHeight);
        root.setMaxHeight(newHeight);

        if (mod.isCMLMod()) {
            this.properties.setText(
                    "\"CML\" : \"true\"\n"
                    + "\"Workshop ID" + mod.getWorkshopId()
            );
        } else {
            this.properties.setText(
                    "\"CML\" : \"false\"\n"
                    + "\"Convertable\" : \"" + mod.getApplicability() + "\"\n"
                    + "\"ID\" : " + mod.getWorkshopId()
            );
        }
    }

    public static List<Node> parseWorkshopText(String text) {
        if (text == null) {
            return new ArrayList();
        }
        List<Node> nodes = new ArrayList();
        text = text.replace("\\t", "\t").replace("\\n", "\n");
        int readIndex = 0;
        while (readIndex < text.length()) {
            //Not supported: Youtube, Steam Store, or Steam Workshop Widgets; Quoted text; Code; Tables
            nodes.addAll(readSpecial(text, readIndex));
            readIndex = staticReadIndex;
        }
        return nodes;
    }

    private static int getNextSpecial(String text, int readIndex) {
        int index = text.indexOf("[", readIndex);
        if (index == -1) {
            return -1;
        }
        if (text.startsWith("[b]", index)
                || text.startsWith("[u]", index)
                || text.startsWith("[i]", index)
                || text.startsWith("[h1]", index)
                || text.startsWith("[img]", index)
                || text.startsWith("[url=", index)
                || text.startsWith("[list]", index)
                || text.startsWith("[olist]", index)
                || text.startsWith("[strike]", index)
                || text.startsWith("[spoiler]", index)
                || text.startsWith("[noparse]", index)) {
            return index;
        } else {
            return getNextSpecial(text, index + 1);
        }
    }

    private static final Map<String, Boolean> CAN_NEST = new HashMap();

    static {
        CAN_NEST.put("b", true);
        CAN_NEST.put("u", true);
        CAN_NEST.put("i", true);
        CAN_NEST.put("h1", true);
        CAN_NEST.put("img", false);
        CAN_NEST.put("url", true);
        CAN_NEST.put("list", true);
        CAN_NEST.put("olist", true);
        CAN_NEST.put("strike", true);
        CAN_NEST.put("spoiler", true);
        CAN_NEST.put("noparse", false);
    }

    private static int staticReadIndex = 0;

    private static List<Node> readSpecial(String text, int readIndex) {
        staticReadIndex = readIndex;
        return readSpecialRec(text, new String[]{"description"});
    }

    private static List<Node> readSpecialRec(String text, String[] styleClasses) {
        List<Node> nodes = new ArrayList();

        int firstSpecial = getNextSpecial(text, staticReadIndex);
        if (staticReadIndex != firstSpecial) {
            Text textNode = new Text(text.substring(staticReadIndex, (firstSpecial != -1 ? firstSpecial : text.length())));
            textNode.getStyleClass().addAll("description", "denote-normal");
            nodes.add(textNode);
            staticReadIndex = (firstSpecial != -1) ? firstSpecial : text.length();
        }
        if (firstSpecial == -1) {
            return nodes;
        }

        if (text.startsWith("[url=", staticReadIndex)) {
            Hyperlink link = new Hyperlink();
            int linkUrlEndIndex = text.indexOf("]", staticReadIndex);
            String linkUrl = text.substring(staticReadIndex + 5, linkUrlEndIndex);
            link.setOnAction((event) -> {
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + linkUrl);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to open link: url=\"" + linkUrl + "\"", ex);
                }
            });
            Tooltip.install(link, new Tooltip(linkUrl));
            staticReadIndex = linkUrlEndIndex + 1;
            String[] newStyles = Arrays.copyOf(styleClasses, styleClasses.length + 1);
            newStyles[newStyles.length - 1] = "denote-url";
            link.getStyleClass().addAll(newStyles);
            int linkEndIndex = text.indexOf("[/url]", staticReadIndex);
            int nextSpecial = getNextSpecial(text, staticReadIndex);
            if (nextSpecial != -1 && nextSpecial < linkEndIndex) {
                TextFlow parent = new TextFlow();
                parent.getChildren().setAll(readSpecialRec(text, newStyles));
                link.setGraphic(parent);
            } else {
                Text textNode = new Text(text.substring(staticReadIndex, linkEndIndex));
                textNode.getStyleClass().addAll(newStyles);
                link.setGraphic(textNode);
            }
            nodes.add(link);
            staticReadIndex = linkEndIndex + 6;
        } else if (text.startsWith("[img]", staticReadIndex)) {
            int imageEndIndex = text.indexOf("[/img]", staticReadIndex);
            String imageUrl = text.substring(staticReadIndex + 5, imageEndIndex);
            if (!imageUrl.endsWith(".gif")) {
                try {
                    ImageView imageView = new ImageView(SwingFXUtils.toFXImage(ImageIO.read(new URL(imageUrl).openStream()), null));
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(imageView.getImage().getHeight());
                    imageView.setFitWidth(imageView.getImage().getWidth());
                    nodes.add(imageView);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not read image at URL \"" + imageUrl + "\"", ex);
                }
            } else {
                try {
                    Animation animation = new Animation(AnimationFrame.fromGif(new URL(imageUrl).openStream()));
                    ImageView imageView = new ImageView();
                    imageView.setPreserveRatio(true);
                    imageView.visibleProperty().addListener((obs, oldValue, newValue) -> {
                        if (!animation.isAnimated()) {
                            animation.animate(imageView);
                        }
                    });
                    animation.animate(imageView);
                    nodes.add(imageView);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not read image at URL \"" + imageUrl + "\"", ex);
                }
            }
            staticReadIndex = imageEndIndex + 6;
        } else {
            String denotation = text.substring(staticReadIndex + 1, text.indexOf("]", staticReadIndex));
            int denotationEndIndex = text.indexOf("[/" + denotation + "]", staticReadIndex);
            if (denotationEndIndex != -1) {
                int nextSpecial = getNextSpecial(text, staticReadIndex + 2);
                if (CAN_NEST.get(denotation) && nextSpecial != -1 && nextSpecial < denotationEndIndex) {
                    String[] newStyles = Arrays.copyOf(styleClasses, styleClasses.length + 1);
                    newStyles[newStyles.length - 1] = "denote-" + denotation;
                    staticReadIndex += denotation.length() + 2;
                    do {
                        String textListed = text.substring(staticReadIndex, nextSpecial);
                        if (denotation.equals("list")) {
                            textListed = textListed.replace("[*]", "\t• ");
                        } else if (denotation.equals("olist")) {
                            int i = 0;
                            while (textListed.contains("[*]")) {
                                textListed = textListed.replaceFirst("\\[\\*\\]", "\t" + (++i) + ". ");
                            }
                        }

                        Text textNode = new Text(textListed);
                        textNode.getStyleClass().addAll(newStyles);
                        nodes.add(textNode);

                        staticReadIndex = nextSpecial;
                        nodes.addAll(readSpecialRec(text, newStyles));

                        nextSpecial = getNextSpecial(text, staticReadIndex);
                    } while (nextSpecial != -1 && nextSpecial < denotationEndIndex);
                    if (staticReadIndex < denotationEndIndex) {
                        String textListed = text.substring(staticReadIndex, denotationEndIndex);
                        if (denotation.equals("list")) {
                            textListed = textListed.replace("[*]", "\t• ");
                        } else if (denotation.equals("olist")) {
                            int i = 0;
                            while (textListed.contains("[*]")) {
                                textListed = textListed.replaceFirst("\\[\\*\\]", "\t" + (++i) + ". ");
                            }
                        }

                        Text textNode = new Text(textListed);
                        textNode.getStyleClass().addAll(newStyles);
                        nodes.add(textNode);
                    }
                } else {
                    Text textNode = new Text(text.substring(staticReadIndex + denotation.length() + 2, denotationEndIndex));
                    String[] newStyles = Arrays.copyOf(styleClasses, styleClasses.length + 1);
                    newStyles[newStyles.length - 1] = "denote-" + denotation;
                    textNode.getStyleClass().addAll(newStyles);
                    nodes.add(textNode);
                }

                staticReadIndex = denotationEndIndex + denotation.length() + 3;
            } else {
                Text textNode = new Text("[");
                textNode.getStyleClass().addAll(styleClasses);
                staticReadIndex++;
                nodes.add(textNode);
            }
        }

        return nodes;
    }

}

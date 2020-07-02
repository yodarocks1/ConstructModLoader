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
package cml.beans;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author benne
 * @param <T> Wrapped data type
 */
public abstract class ANodeData<T> implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(ANodeData.class.getName());
    protected final T wrapped;
    protected AnchorPane root = new AnchorPane();

    protected ANodeData(String fxmlResource, T toWrap) {
        this.wrapped = toWrap;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not read FXML file", ex);
        }
    }

    public final Node getNode() {
        return root;
    }
    
    public final T getWrapped() {
        return wrapped;
    }

}

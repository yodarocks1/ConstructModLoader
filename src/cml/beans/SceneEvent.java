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

import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class SceneEvent extends Event {

    public static final EventType<SceneEvent> ANY
            = new EventType<>(Event.ANY, "SCENE_E");

    public static final EventType<SceneEvent> ICON_CHANGE
            = new EventType<>(Event.ANY, "ICON_CHANGE");

    private Object data = null;

    public SceneEvent(EventType<? extends SceneEvent> eventType) {
        super(eventType);
    }

    public SceneEvent(EventType<? extends SceneEvent> eventType, Object data) {
        super(eventType);
        this.data = data;
    }

    public SceneEvent(Object source, EventTarget target, EventType<? extends SceneEvent> eventType) {
        super(source, target, eventType);
    }

    public SceneEvent(Object source, EventTarget target, EventType<? extends SceneEvent> eventType, Object data) {
        super(source, target, eventType);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static class IconChange {
        public static enum IconChangeType {
            ADD (0), REMOVE (1), SET (2);
            public final int id;
            private IconChangeType(int id) {
                this.id = id;
            }
        }
        public List<Image> icons;
        public IconChangeType changeType;

        public IconChange(IconChangeType changeType, List<Image> icons) {
            this.icons = icons;
            this.changeType = changeType;
        }
        
        public IconChange(IconChangeType changeType, Image... icons) {
            this.icons = Arrays.asList(icons);
            this.changeType = changeType;
        }

    }
    
    public Runnable toRunnable(Node source) {
        return () -> {
            source.fireEvent(this);
        };
    }
    
    public void fire(Node source) {
        Platform.runLater(this.toRunnable(source));
    }
}

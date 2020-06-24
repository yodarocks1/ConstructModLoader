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
package cml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author benne
 */
public class ErrorManager {

    private static final ErrorManager NO_ERROR = new ErrorManager(false);
    private static final ErrorManager ERROR = new ErrorManager(true);
    private static final Map<String, Runnable> RESOLVE = new HashMap();
    private static final Logger LOGGER = Logger.getLogger(ErrorManager.class.getName());

    public static ObjectProperty<ErrorManager> State = new SimpleObjectProperty(NO_ERROR);

    private final List<String> causes = new ArrayList();
    private final boolean error;

    private ErrorManager(boolean error) {
        this.error = error;
    }

    public void removeCause(String cause) {
        if (this.error) {
            if (this.causes.remove(cause)) {
                LOGGER.log(Level.INFO, "User Error resolved: \"{0}\"\n  Remaining errors: {1}", new Object[]{cause, causes.size()});
            }
            if (this.causes.isEmpty()) {
                State.setValue(NO_ERROR);
            }
        }
    }

    public void addCause(String cause) {
        if (!this.error) {
            ERROR.addCause(cause);
            Platform.runLater(() -> {
                State.setValue(ERROR);
            });
        } else if (!causes.contains(cause)) {
            LOGGER.log(Level.WARNING, "User Error caught: \"{0}\"", cause);
            this.causes.add(cause);
        }
    }

    public List<String> getCauses() {
        return this.causes;
    }

    public boolean isError() {
        return this.error;
    }
    
    @Override
    public String toString() {
        return causes.stream().collect(Collectors.joining("\n", "Error: ", " (Unresolved)"));
    }

    public static void addStateCause(String cause) {
        State.getValue().addCause(cause);
    }
    
    public static void addCauseResolver(String cause, Runnable resolver) {
        RESOLVE.put(cause, resolver);
    }

    public static void removeStateCause(String cause) {
        State.getValue().removeCause(cause);
    }

    public static boolean isStateError() {
        return State.getValue().isError();
    }
    
    public static boolean autoResolve() {
        ErrorManager state = State.getValue();
        if (state.isError()) {
            for (String cause : state.getCauses()) {
                if (RESOLVE.containsKey(cause)) {
                    LOGGER.log(Level.INFO, "Resolving Error \"{0}\"", cause);
                    RESOLVE.get(cause).run();
                } else {
                    LOGGER.log(Level.WARNING, "Could auto-resolve Error \"{0}\" - No resolver exists", cause);
                }
            }
        }
        return State.getValue().isError();
    }
}

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
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author benne
 */
public class ErrorManager {

    private static final ErrorManager NO_ERROR = new ErrorManager(false);
    private static final ErrorManager ERROR = new ErrorManager(true);

    public static ObjectProperty<ErrorManager> State = new SimpleObjectProperty(NO_ERROR);

    private final List<String> causes = new ArrayList();
    private final boolean error;

    private ErrorManager(boolean error) {
        this.error = error;
    }

    public void removeCause(String cause) {
        if (this.error) {
            if (this.causes.remove(cause) ) {
                System.out.println((char) 27 + "[34;1mUser Error resolved: \"" + cause + "\"\n"
                        + "  Remaining errors: " + causes.size() + (char) 27 + "[0m");
            }
            if (this.causes.isEmpty()) {
                State.setValue(NO_ERROR);
            }
        }
    }

    public void addCause(String cause) {
        if (!this.error) {
            ERROR.addCause(cause);
            State.setValue(ERROR);
        } else if (!causes.contains(cause)) {
            System.out.println((char) 27 + "[31;1;5mUser Error caught: \"" + cause + "\"" + (char) 27 + "[0m");
            this.causes.add(cause);
        }
    }

    public List<String> getCauses() {
        return this.causes;
    }

    public boolean isError() {
        return this.error;
    }
    
    public static void addStateCause(String cause) {
        State.getValue().addCause(cause);
    }
    
    public static void removeStateCause(String cause) {
        State.getValue().removeCause(cause);
    }
    
    public static boolean isStateError() {
        return State.getValue().isError();
    }
    
    public static void printNonUserError(String error) {
        System.out.println((char) 27 + "[31;1;5m" + error + (char) 27 + "[0m");
    }
}

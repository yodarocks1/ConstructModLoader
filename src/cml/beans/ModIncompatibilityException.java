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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class ModIncompatibilityException extends Exception {

    private static final Logger LOGGER = Logger.getLogger(ModIncompatibilityException.class.getName());
    
    private final Set<Modification> offenders;
    private final boolean certain;

    public ModIncompatibilityException(List<Modification> offenders) {
        super(offenders.size() + " modifications are possibly incompatible");
        this.certain = false;
        this.offenders = new HashSet(offenders);
    }

    public ModIncompatibilityException(Modification... offenders) {
        this(false, Arrays.asList(offenders));
    }
    
    public ModIncompatibilityException(boolean certain, List<Modification> offenders) {
        super(offenders.size() + " modifications are " + (certain ? "definitely" : "possibly") + " incompatible");
        this.certain = certain;
        this.offenders = new HashSet(offenders);
    }

    public ModIncompatibilityException(boolean certain, Modification... offenders) {
        this(certain, Arrays.asList(offenders));
    }

    public Set<Modification> getOffenders() {
        return this.offenders;
    }

    public boolean isCertain() {
        return certain;
    }

    public static final Map<String, List<Modification>> POSSIBLE_INCOMPATIBILITIES = new HashMap();
    public static final Map<String, List<Modification>> CERTAIN_INCOMPATIBILITIES = new HashMap();

    public static void addNewIncompatibility(String pathAddend, List<Modification> mods) {
        String warning = mods.stream().map((mod) -> "\n  " + mod.getName()).reduce("New possible incompatibility: ", String::concat);
        LOGGER.log(Level.WARNING, warning);
        if (POSSIBLE_INCOMPATIBILITIES.containsKey(pathAddend)) {
            POSSIBLE_INCOMPATIBILITIES.get(pathAddend).addAll(mods);
        } else {
            POSSIBLE_INCOMPATIBILITIES.put(pathAddend, new ArrayList());
            addNewIncompatibility(pathAddend, mods);
        }
    }

    public static void addNewIncompatibility(String pathAddend, Modification... mods) {
        addNewIncompatibility(pathAddend, Arrays.asList(mods));
    }

    public static void addNewIncompatibility(String pathAddend, boolean certain, List<Modification> mods) {
        if (!certain) {
            addNewIncompatibility(pathAddend, mods);
        } else {
            String warning = mods.stream().map((mod) -> "\n  " + mod.getName()).reduce("New certain incompatibility: ", String::concat);
            LOGGER.log(Level.WARNING, warning);
            if (CERTAIN_INCOMPATIBILITIES.containsKey(pathAddend)) {
                CERTAIN_INCOMPATIBILITIES.get(pathAddend).addAll(mods);
            } else {
                CERTAIN_INCOMPATIBILITIES.put(pathAddend, new ArrayList());
                addNewIncompatibility(pathAddend, certain, mods);
            }
        }
    }

    public static void addNewIncompatibility(String pathAddend, boolean certain, Modification... mods) {
        addNewIncompatibility(pathAddend, certain, Arrays.asList(mods));
    }

    public static ModIncompatibilityException[] compileIncompatibilities() {
        ModIncompatibilityException[] exceptions = new ModIncompatibilityException[POSSIBLE_INCOMPATIBILITIES.keySet().size() + CERTAIN_INCOMPATIBILITIES.keySet().size()];
        int i = 0;
        for (List<Modification> possibleIncompatibility : POSSIBLE_INCOMPATIBILITIES.values()) {
            exceptions[i] = new ModIncompatibilityException(false, possibleIncompatibility);
            i++;
        }
        for (List<Modification> certainIncompatibility : CERTAIN_INCOMPATIBILITIES.values()) {
            exceptions[i] = new ModIncompatibilityException(true, certainIncompatibility);
            i++;
        }
        POSSIBLE_INCOMPATIBILITIES.clear();
        CERTAIN_INCOMPATIBILITIES.clear();
        return exceptions;
    }
}

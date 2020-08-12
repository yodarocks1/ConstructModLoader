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
package cml.lib.lazyupdate;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author benne
 */
public class Flags extends ConcurrentHashMap<String, Object> {
    static final Map<FlagParent, Flags> FLAGS = new ConcurrentHashMap();
    
    public static void putFlag(FlagParent parent, String flag, Object value) {
        FLAGS.putIfAbsent(parent, new Flags());
        FLAGS.get(parent).put(flag, value);
    }
    
    public static void setFlag(FlagParent parent, Flag flag) {
        putFlag(parent, flag.name(), true);
    }
    
    public static Object takeFlag(FlagParent parent, String flag) {
        if (FLAGS.containsKey(parent)) {
            Object value = FLAGS.get(parent).getOrDefault(flag, null);
            FLAGS.get(parent).remove(flag);
            return value;
        }
        return null;
    }
    
    public static boolean clearFlag(FlagParent parent, Flag flag) {
        return takeFlag(parent, flag.name()) != null;
    }
    
    public static void moveFlags(FlagParent oldParent, FlagParent newParent) {
        FLAGS.put(newParent, FLAGS.get(oldParent));
        FLAGS.remove(oldParent);
    }
    
    public static void clearFlags(FlagParent parent) {
        FLAGS.remove(parent);
    }
    
    public enum Flag {
        DO_UPDATE
    }
    
    public static class FlagParent {
        private final Object key;
        public FlagParent(Object key) {
            if (key instanceof Class) {
                this.key = new Object[] {((Class) key).getName(), key};
            } else {
                this.key = key;
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + Objects.hashCode(this.key);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FlagParent other = (FlagParent) obj;
            if (!Objects.equals(this.key, other.key)) {
                return false;
            }
            return true;
        }
        
    }
    
    public static FlagParent localFlags(Object key) {
        return new FlagParent(key);
    }
    
    public static FlagParent staticFlags(Object key) {
        if (key instanceof Class) {
            return new FlagParent(key);
        } else {
            return new FlagParent(key.getClass());
        }
    }
    
}

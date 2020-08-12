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

import com.sun.javafx.binding.StringFormatter;
import java.io.File;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.util.StringConverter;

/**
 *
 * @author benne
 */
final class InvalidProfile extends Profile {
    
    private static final Logger LOGGER = Logger.getLogger(InvalidProfile.class.getName());
    
    private final ReadOnlyObjectPropertyWrapper readOnlyIcon;
    private final ReadOnlyStringPropertyWrapper readOnlyDesc;
    private final ReadOnlyStringPropertyWrapper readOnlyName;

    public InvalidProfile(Image icon, String name) {
        super(name);
        this.setIcon(icon);
        readOnlyIcon = new ReadOnlyObjectPropertyWrapper(super.getIconProperty());
        readOnlyDesc = new ReadOnlyStringPropertyWrapper(super.getDescProperty());
        readOnlyName = new ReadOnlyStringPropertyWrapper(super.getNameProperty());
    }
    
    //<editor-fold defaultstate="collapsed" desc="Read-Only Modifiers">
    
    @Override
    public void setName(String name) {
    }
    
    @Override
    public void setIcon(Image icon) {
    }

    @Override
    public ObjectProperty<Image> getIconProperty() {
        return readOnlyIcon;
    }

    @Override
    public StringProperty getDescProperty() {
        return readOnlyDesc;
    }

    @Override
    public StringProperty getNameProperty() {
        return readOnlyName;
    }

    @Override
    public void saveAsSelected() {
        LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
    }

    @Override
    public void setDescription(String description) {
        LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
    }
    
    @Override
    public void setModifications(List<Modification> modifications) {
        LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
    }

    @Override
    public List<Modification> getModifications() {
        return new ArrayList();
    }

    @Override
    public void updateModifications() {
    }

    @Override
    public List<Modification> getActiveModifications() {
        return new ArrayList();
    }

    @Override
    public File getDirectory() {
        return null;
    }

    @Override
    public void delete() {
    }

    @Override
    public String getLeft() {
        return "";
    }

    @Override
    public String getCenter() {
        return "";
    }

    @Override
    public String getRight() {
        return "";
    }
    
    private class ReadOnlyStringPropertyWrapper extends SimpleStringProperty {
        
        ReadOnlyStringProperty wrapped;
        
        public ReadOnlyStringPropertyWrapper(ReadOnlyStringProperty wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }

        @Override
        public String getValue() {
            return wrapped.getValue();
        }

        @Override
        public StringExpression concat(Object other) {
            return wrapped.concat(other);
        }

        @Override
        public BooleanBinding isEqualTo(ObservableStringValue other) {
            return wrapped.isEqualTo(other);
        }

        @Override
        public BooleanBinding isEqualTo(String other) {
            return wrapped.isEqualTo(other);
        }

        @Override
        public BooleanBinding isNotEqualTo(ObservableStringValue other) {
            return wrapped.isNotEqualTo(other);
        }

        @Override
        public BooleanBinding isNotEqualTo(String other) {
            return wrapped.isNotEqualTo(other);
        }

        @Override
        public BooleanBinding isEqualToIgnoreCase(ObservableStringValue other) {
            return wrapped.isEqualToIgnoreCase(other);
        }

        @Override
        public BooleanBinding isEqualToIgnoreCase(String other) {
            return wrapped.isEqualToIgnoreCase(other);
        }

        @Override
        public BooleanBinding isNotEqualToIgnoreCase(ObservableStringValue other) {
            return wrapped.isNotEqualToIgnoreCase(other);
        }

        @Override
        public BooleanBinding isNotEqualToIgnoreCase(String other) {
            return wrapped.isNotEqualToIgnoreCase(other);
        }

        @Override
        public BooleanBinding greaterThan(ObservableStringValue other) {
            return wrapped.greaterThan(other);
        }

        @Override
        public BooleanBinding greaterThan(String other) {
            return wrapped.greaterThan(other);
        }

        @Override
        public BooleanBinding lessThan(ObservableStringValue other) {
            return wrapped.lessThan(other);
        }

        @Override
        public BooleanBinding lessThan(String other) {
            return wrapped.lessThan(other);
        }

        @Override
        public BooleanBinding greaterThanOrEqualTo(ObservableStringValue other) {
            return wrapped.greaterThanOrEqualTo(other);
        }

        @Override
        public BooleanBinding greaterThanOrEqualTo(String other) {
            return wrapped.greaterThanOrEqualTo(other);
        }

        @Override
        public BooleanBinding lessThanOrEqualTo(ObservableStringValue other) {
            return wrapped.lessThanOrEqualTo(other);
        }

        @Override
        public BooleanBinding lessThanOrEqualTo(String other) {
            return wrapped.lessThanOrEqualTo(other);
        }

        @Override
        public BooleanBinding isNull() {
            return wrapped.isNull();
        }

        @Override
        public BooleanBinding isNotNull() {
            return wrapped.isNotNull();
        }

        @Override
        public IntegerBinding length() {
            return wrapped.length();
        }

        @Override
        public BooleanBinding isEmpty() {
            return wrapped.isEmpty();
        }

        @Override
        public BooleanBinding isNotEmpty() {
            return wrapped.isNotEmpty();
        }

        @Override
        public String get() {
            return wrapped.get();
        }

        @Override
        public Object getBean() {
            return wrapped.getBean();
        }

        @Override
        public String getName() {
            return wrapped.getName();
        }

        @Override
        public void addListener(ChangeListener<? super String> listener) {
            wrapped.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super String> listener) {
            wrapped.removeListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            wrapped.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            wrapped.removeListener(listener);
        }

        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return wrapped.equals(obj);
        }
        
        
        //

        @Override
        public void unbind() {
            super.unbind(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void bind(ObservableValue<? extends String> newObservable) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        public boolean isBound() {
            return false;
        }

        @Override
        public void set(String newValue) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        protected void invalidated() {
        }

        @Override
        protected void fireValueChangedEvent() {
        }

        @Override
        public void unbindBidirectional(Object other) {
        }

        @Override
        public void unbindBidirectional(Property<String> other) {
        }

        @Override
        public <T> void bindBidirectional(Property<T> other, StringConverter<T> converter) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        public void bindBidirectional(Property<?> other, Format format) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        public void bindBidirectional(Property<String> other) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        public void setValue(String v) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }
        
        
        
    }

    private class ReadOnlyObjectPropertyWrapper<T> extends SimpleObjectProperty<T> {
        
        ReadOnlyObjectProperty<T> wrapped;
        
        public ReadOnlyObjectPropertyWrapper(ReadOnlyObjectProperty<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Object getBean() {
            return wrapped.getBean();
        }

        @Override
        public String getName() {
            return wrapped.getName();
        }

        @Override
        public void addListener(ChangeListener<? super T> listener) {
            wrapped.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super T> listener) {
            wrapped.removeListener(listener);
        }

        @Override
        public T getValue() {
            return wrapped.getValue();
        }

        @Override
        public void addListener(InvalidationListener listener) {
            wrapped.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            wrapped.removeListener(listener);
        }

        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return wrapped.equals(obj);
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }

        @Override
        public T get() {
            return wrapped.get();
        }
        
        
        //
        
        @Override
        public void unbind() {
        }

        @Override
        public void bind(ObservableValue<? extends T> newObservable) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        public boolean isBound() {
            return false;
        }

        @Override
        public void set(T newValue) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        protected void invalidated() {
        }

        @Override
        protected void fireValueChangedEvent() {
        }

        @Override
        public void unbindBidirectional(Property<T> other) {
        }

        @Override
        public void bindBidirectional(Property<T> other) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        public void setValue(T v) {
            LOGGER.log(Level.SEVERE, "Read only property can not be modified", new UnsupportedOperationException());
        }

        @Override
        public StringBinding asString(Locale locale, String format) {
            return (StringBinding) Bindings.format(locale, format, wrapped);
        }

        @Override
        public StringBinding asString(String format) {
            return (StringBinding) Bindings.format(format, wrapped);
        }

        @Override
        public StringBinding asString() {
            return (StringBinding) StringFormatter.convert(wrapped);
        }

        @Override
        public BooleanBinding isNotNull() {
            return Bindings.isNotNull(wrapped);
        }

        @Override
        public BooleanBinding isNull() {
            return Bindings.isNull(wrapped);
        }

        @Override
        public BooleanBinding isNotEqualTo(Object other) {
            return Bindings.notEqual(other, wrapped);
        }

        @Override
        public BooleanBinding isNotEqualTo(ObservableObjectValue<?> other) {
            return Bindings.notEqual(wrapped, other);
        }

        @Override
        public BooleanBinding isEqualTo(Object other) {
            return Bindings.equal(other, wrapped);
        }

        @Override
        public BooleanBinding isEqualTo(ObservableObjectValue<?> other) {
            return Bindings.equal(wrapped, other);
        }
        
        
        
    }
    
    //</editor-fold>
}

package com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.model;

import java.util.HashMap;
import java.util.Map;

public class Observation {
    private String id;
    private String status;
    private Map<String, Component> components = new HashMap<>();

    public static class Component {
        private double value;
        private String unit;

        public Component(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }

        // getters e setters
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    // getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Component> getComponents() { return components; }
    public void setComponents(Map<String, Component> components) { this.components = components; }

    public void addComponent(String code, String unit, double value) {
        this.components.put(code, new Component(value, unit));
    }
}
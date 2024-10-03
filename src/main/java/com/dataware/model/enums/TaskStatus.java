package com.dataware.model.enums;

public enum TaskStatus {
    TO_DO("To Do"),
    DOING("Doing"),
    DONE("Done");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}

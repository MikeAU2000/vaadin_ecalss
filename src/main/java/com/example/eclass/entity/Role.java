package com.example.eclass.entity;

public enum Role {
    ADMIN("管理員"),
    TEACHER("老師"),
    STUDENT("學生");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
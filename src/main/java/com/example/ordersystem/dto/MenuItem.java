package com.example.ordersystem.dto;

import java.util.List;

public class MenuItem {
    private String id;
    private String label;
    private String icon;
    private String route;
    private List<String> requiredRoles; // 需要的角色，空列表表示所有登入用戶都可以訪問
    
    public MenuItem() {
    }
    
    public MenuItem(String id, String label, String icon, String route, List<String> requiredRoles) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.route = route;
        this.requiredRoles = requiredRoles;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getRoute() {
        return route;
    }
    
    public void setRoute(String route) {
        this.route = route;
    }
    
    public List<String> getRequiredRoles() {
        return requiredRoles;
    }
    
    public void setRequiredRoles(List<String> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }
}


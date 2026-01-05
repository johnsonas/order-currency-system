package com.example.ordersystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MENUS")
public class Menu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_seq")
    @SequenceGenerator(name = "menu_seq", sequenceName = "MENU_SEQ", allocationSize = 1)
    @Column(name = "MENU_ID")
    private Long menuId;
    
    @NotBlank(message = "選單ID不能為空")
    @Column(name = "MENU_KEY", nullable = false, unique = true, length = 50)
    private String menuKey;
    
    @NotBlank(message = "選單標籤不能為空")
    @Column(name = "LABEL", nullable = false, length = 100)
    private String label;
    
    @Column(name = "ICON", length = 50)
    private String icon;
    
    @NotBlank(message = "路由不能為空")
    @Column(name = "ROUTE", nullable = false, length = 50)
    private String route;
    
    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;
    
    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "MENU_ROLES",
        joinColumns = @JoinColumn(name = "MENU_ID"),
        inverseJoinColumns = @JoinColumn(name = "ROLE_ID")
    )
    private Set<Role> requiredRoles = new HashSet<>();
    
    // Getters and Setters
    public Long getMenuId() {
        return menuId;
    }
    
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    
    public String getMenuKey() {
        return menuKey;
    }
    
    public void setMenuKey(String menuKey) {
        this.menuKey = menuKey;
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
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<Role> getRequiredRoles() {
        return requiredRoles;
    }
    
    public void setRequiredRoles(Set<Role> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }
}


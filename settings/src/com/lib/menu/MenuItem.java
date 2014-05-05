package com.lib.menu;

/**
 * polozka menu
 */
public class MenuItem {

    protected Integer icon;

    protected Integer title;

    protected Class<?> activity;

    public MenuItem(Integer icon, Integer title, Class<?> activity) {
        this.icon = icon;
        this.title = title;
        this.activity = activity;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public Integer getTitle() {
        return title;
    }

    public void setTitle(Integer title) {
        this.title = title;
    }

    public Class<?> getActivity() {
        return activity;
    }

    public void setActivity(Class<?> activity) {
        this.activity = activity;
    }
}

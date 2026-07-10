package com.theosfera.core.menu.action;

@FunctionalInterface
public interface MenuActionHandler {

    void execute(
            ParsedMenuAction action,
            MenuActionContext context
    );
}
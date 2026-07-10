package com.theosfera.core.variable.placeholder;

import org.bukkit.entity.Player;

public interface ExternalPlaceholderService {

    String apply(Player player, String text);
}
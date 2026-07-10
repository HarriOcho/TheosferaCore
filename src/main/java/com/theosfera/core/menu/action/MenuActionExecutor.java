package com.theosfera.core.menu.action;
import java.util.Optional;

public final class MenuActionExecutor {


    private final MenuActionParser actionParser;
    private final MenuActionRegistry actionRegistry;

    public MenuActionExecutor() {
        this.actionParser = new MenuActionParser();
        this.actionRegistry = new MenuActionRegistry();

        registerDefaultActions();
    }

    public void execute(
            final String rawAction,
            final MenuActionContext context
    ) {
        final Optional<ParsedMenuAction> optionalAction =
                actionParser.parse(rawAction);

        if (optionalAction.isEmpty()) {
            context.plugin().getLogger().warning(
                    "[Menu] Acción mal formada: '" + rawAction
                            + "'. Usa formato [tipo] valor."
            );
            return;
        }

        final ParsedMenuAction action = optionalAction.get();

        actionRegistry.get(action.type()).ifPresentOrElse(
                handler -> handler.execute(action, context),
                () -> context.plugin().getLogger().warning(
                        "[Menu] Acción desconocida: '" + action.type()
                                + "' con valor '" + action.value()
                                + "'."
                )
        );
    }

    private void registerDefaultActions() {
        new NavigationMenuActionHandler().register(actionRegistry);
        new MessageMenuActionHandler().register(actionRegistry);
        new CommandMenuActionHandler().register(actionRegistry);
        new EffectMenuActionHandler().register(actionRegistry);
        new KeybindEditMenuActionHandler().register(actionRegistry);
        new KeybindActionMenuActionHandler().register(actionRegistry);
    }
}
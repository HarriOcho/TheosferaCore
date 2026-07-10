package com.theosfera.core.keybind;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class KeybindManager {

    private final KeybindStorage storage;
    private final Map<Integer, KeybindEntry> keybinds = new LinkedHashMap<>();

    public KeybindManager(KeybindStorage storage) {
        this.storage = storage;
    }

    public void load() {
        keybinds.clear();
        keybinds.putAll(storage.loadAll());
    }

    public void save() {
        storage.saveAll(keybinds);
    }

    public List<KeybindEntry> getAllSorted() {
        return keybinds.values()
                .stream()
                .sorted(Comparator.comparingInt(KeybindEntry::id))
                .toList();
    }

    public Optional<KeybindEntry> getById(int id) {
        return Optional.ofNullable(keybinds.get(id));
    }

    public Optional<KeybindEntry> add(String name, String key, String description) {
        if (findByKey(key) != null) {
            return Optional.empty();
        }

        int id = getNextId();

        KeybindEntry entry = new KeybindEntry(id, name, description, key, new ArrayList<>());
        keybinds.put(id, entry);
        save();

        return Optional.of(entry);
    }

    public boolean remove(int id) {
        KeybindEntry removed = keybinds.remove(id);

        if (removed == null) {
            return false;
        }

        save();
        return true;
    }

    public Optional<KeybindEntry> editName(int id, String newName) {
        return edit(id, newName, null, null, null);
    }

    public Optional<KeybindEntry> editDescription(int id, String newDescription) {
        return edit(id, null, newDescription, null, null);
    }

    public Optional<KeybindEntry> editKey(int id, String newKey) {
        KeybindEntry duplicate = findByKey(newKey);

        if (duplicate != null && duplicate.id() != id) {
            return Optional.empty();
        }

        return edit(id, null, null, newKey, null);
    }

    public Optional<KeybindEntry> addAction(int id, KeybindAction action) {
        KeybindEntry current = keybinds.get(id);

        if (current == null) {
            return Optional.empty();
        }

        List<KeybindAction> actions = new ArrayList<>(current.actions());
        actions.add(action);

        return edit(id, null, null, null, actions);
    }

    public Optional<KeybindEntry> removeAction(int id, int actionIndex) {
        KeybindEntry current = keybinds.get(id);

        if (current == null) {
            return Optional.empty();
        }

        if (actionIndex < 0 || actionIndex >= current.actions().size()) {
            return Optional.empty();
        }

        List<KeybindAction> actions = new ArrayList<>(current.actions());
        actions.remove(actionIndex);

        return edit(id, null, null, null, actions);
    }

    public Optional<KeybindEntry> editAction(int id, int actionIndex, KeybindAction newAction) {
        KeybindEntry current = keybinds.get(id);

        if (current == null) {
            return Optional.empty();
        }

        if (actionIndex < 0 || actionIndex >= current.actions().size()) {
            return Optional.empty();
        }

        List<KeybindAction> actions = new ArrayList<>(current.actions());
        actions.set(actionIndex, newAction);

        return edit(id, null, null, null, actions);
    }

    public Optional<KeybindEntry> moveAction(int id, int fromIndex, int toIndex) {
        KeybindEntry current = keybinds.get(id);

        if (current == null) {
            return Optional.empty();
        }

        if (fromIndex < 0 || fromIndex >= current.actions().size()) {
            return Optional.empty();
        }

        if (toIndex < 0 || toIndex >= current.actions().size()) {
            return Optional.empty();
        }

        if (fromIndex == toIndex) {
            return Optional.of(current);
        }

        List<KeybindAction> actions = new ArrayList<>(current.actions());
        KeybindAction movedAction = actions.remove(fromIndex);
        actions.add(toIndex, movedAction);

        return edit(id, null, null, null, actions);
    }

    private Optional<KeybindEntry> edit(
            int id,
            String newName,
            String newDescription,
            String newKey,
            List<KeybindAction> newActions
    ) {
        KeybindEntry current = keybinds.get(id);

        if (current == null) {
            return Optional.empty();
        }

        KeybindEntry updated = new KeybindEntry(
                id,
                newName != null ? newName : current.name(),
                newDescription != null ? newDescription : current.description(),
                newKey != null ? newKey : current.key(),
                newActions != null ? newActions : current.actions()
        );

        keybinds.put(id, updated);
        save();

        return Optional.of(updated);
    }

    private int getNextId() {
        int id = 1;

        while (keybinds.containsKey(id)) {
            id++;
        }

        return id;
    }

    public KeybindEntry findByKey(String key) {
        if (key == null) {
            return null;
        }

        String normalizedKey = key.trim().toUpperCase();
        KeybindEntry firstMatch = null;

        for (KeybindEntry keybind : getAllSorted()) {
            if (!keybind.key().trim().equalsIgnoreCase(normalizedKey)) {
                continue;
            }

            if (firstMatch == null) {
                firstMatch = keybind;
                continue;
            }

            return firstMatch;
        }

        return firstMatch;
    }

    public List<String> findDuplicateKeyWarnings() {
        Map<String, KeybindEntry> usedKeys = new LinkedHashMap<>();

        return getAllSorted()
                .stream()
                .map(keybind -> {
                    String normalizedKey = keybind.key().trim().toUpperCase();

                    if (!usedKeys.containsKey(normalizedKey)) {
                        usedKeys.put(normalizedKey, keybind);
                        return null;
                    }

                    KeybindEntry original = usedKeys.get(normalizedKey);

                    return "Keybind duplicado detectado: la tecla '" + normalizedKey
                            + "' ya está usada por '" + original.name()
                            + "'. El keybind '" + keybind.name()
                            + "' fue deshabilitado hasta cambiar su tecla.";
                })
                .filter(message -> message != null)
                .toList();
    }
}
# TheosferaCore --- Project State

> Fuente de verdad de continuidad del proyecto.
>
> Este documento resume el estado técnico, funcional y de flujo de
> trabajo confirmado de TheosferaCore. Antes de proponer o implementar
> cambios, revisar `AGENTS.md`, `CONTRIBUTING.md`, este archivo y el
> estado real del repositorio.
>
> Si el código actual contradice este documento, inspeccionar la
> diferencia antes de actuar. No asumir que una tarea pendiente sigue
> pendiente si el repositorio demuestra que ya fue completada.

## 1. Identidad y propósito

-   Proyecto: `TheosferaCore`.
-   Repositorio: `HarriOcho/TheosferaCore`.
-   Package raíz: `com.theosfera.core`.
-   Plataforma objetivo: Paper/Purpur 1.21.11.
-   Java objetivo: Java 21.
-   Build system: Gradle Kotlin DSL mediante Gradle Wrapper.
-   TheosferaCore es el core general del servidor y futuro puente con
    Theosfera Client.
-   La lógica específica de Skyblock no pertenece a TheosferaCore.
-   El trabajo específico de Skyblock se separará en
    `TheosferaSkyblockAddons` sobre SuperiorSkyblock2.
-   No se mantiene como plan principal convertir el código completo de
    SuperiorSkyblock2 en un fork llamado TheosferaSkyblock.

## 2. Estado Git y flujo profesional

La fundación profesional de GitHub ya fue completada y fusionada en
`main`.

Archivos incorporados:

-   `.github/ISSUE_TEMPLATE/bug.yml`
-   `.github/ISSUE_TEMPLATE/feature.yml`
-   `.github/pull_request_template.md`
-   `.github/workflows/build.yml`
-   `CONTRIBUTING.md`

El primer Pull Request de fundación GitHub fue validado con CI,
fusionado mediante **Squash and merge** y su rama remota fue eliminada.

Al cierre de ese flujo:

-   `main` estaba sincronizada con `origin/main`;
-   el working tree estaba limpio;
-   GitHub Actions había pasado correctamente;
-   no existían conflictos con la rama base.

No repetir la fundación GitHub ni crear una rama para volver a añadir
CI, plantillas de issues, plantilla de PR o `CONTRIBUTING.md`.

### Flujo de ramas

Usar ramas enfocadas:

-   `feature/<nombre>`
-   `fix/<nombre>`
-   `refactor/<nombre>`
-   `docs/<nombre>`
-   `chore/<nombre>`
-   `test/<nombre>`

Flujo esperado:

1.  Actualizar `main`.
2.  Crear una rama enfocada.
3.  Implementar un cambio de alcance limitado.
4.  Ejecutar verificaciones.
5.  Revisar el diff completo.
6.  Crear commits descriptivos.
7.  Hacer push.
8.  Abrir Pull Request.
9.  Esperar y revisar CI.
10. Fusionar preferentemente con **Squash and merge**.
11. Actualizar `main` local.
12. Limpiar la rama cuando corresponda.

## 3. Documentos de continuidad

La continuidad del proyecto se divide así:

-   `AGENTS.md`: reglas arquitectónicas y forma de trabajar.
-   `PROJECT_STATE.md`: estado real conocido, decisiones confirmadas,
    pruebas y siguiente paso.
-   `CONTRIBUTING.md`: flujo Git/GitHub.

No depender principalmente de frases de checkpoint entre chats.

Al comenzar una nueva sesión de trabajo:

1.  identificar el repositorio;
2.  revisar `AGENTS.md`;
3.  revisar `PROJECT_STATE.md`;
4.  revisar `CONTRIBUTING.md` si habrá cambios Git;
5.  comprobar rama y `git status`;
6.  inspeccionar el código relacionado con la tarea;
7.  contrastar este documento con el estado real antes de modificar
    archivos.

## 4. Arquitectura funcional actual

### `com.theosfera.core`

#### `TheosferaCore`

Clase principal del plugin.

Responsabilidades confirmadas:

-   inicializar y conectar servicios;
-   registrar comandos;
-   registrar tab completers;
-   registrar listeners;
-   integrar el sistema de menús;
-   integrar la entrada por chat.

### `com.theosfera.core.command`

Clases confirmadas:

-   `TheosferaCommand`
-   `TheosferaTabCompleter`
-   `KeybindCommand`
-   `KeybindTabCompleter`
-   `KeyCommand`

La gestión administrativa de keybinds está separada del comando general
del core.

### `com.theosfera.core.keybind`

Clases confirmadas:

-   `KeybindEntry`
-   `KeybindAction`
-   `KeybindActionType`
-   `KeybindActionExecutor`
-   `KeybindManager`
-   `KeybindStorage`
-   `KeybindValidator`

`KeybindManager` y las clases relacionadas conservan la lógica real de
keybinds.

Los menús consumen esa lógica. No deben duplicarla ni reemplazarla.

### `com.theosfera.core.variable`

-   `VariableService`

Servicio central de variables propias de TheosferaCore.

### `com.theosfera.core.ui`

Clases confirmadas:

-   `Language`
-   `MessageService`
-   `MessageCenteringService`
-   `TheosferaPalette`

Responsables de idioma, mensajes, centrado visual y paleta/UI.

### `com.theosfera.core.menu`

Clases confirmadas:

-   `MenuConfig`
-   `MenuConfigLoader`
-   `MenuHolder`
-   `MenuItemConfig`
-   `MenuItemFactory`
-   `MenuItemTag`
-   `MenuListener`
-   `MenuManager`
-   `MenuRenderer`
-   `KeybindMenuRenderer`
-   `MenuService`
-   `MenuTextResolver`
-   `SlotParser`

### `com.theosfera.core.menu.action`

Clases confirmadas:

-   `MenuActionHandler`
-   `MenuActionContext`
-   `MenuActionExecutor`
-   `MenuActionParser`
-   `MenuActionRegistry`
-   `ParsedMenuAction`
-   `MenuActionCodec`
-   `NavigationMenuActionHandler`
-   `MessageMenuActionHandler`
-   `CommandMenuActionHandler`
-   `EffectMenuActionHandler`
-   `KeybindEditMenuActionHandler`
-   `KeybindActionMenuActionHandler`
-   `ParticleActionConfig`
-   `ParticleActionParser`

### `com.theosfera.core.menu.input`

Clases confirmadas:

-   `MenuChatInputService`
-   `MenuChatInputListener`

Este subsistema permite cerrar un inventario, solicitar un valor por
chat, consumir la siguiente entrada del jugador y continuar el flujo del
menú.

## 5. Comandos

### `/theosfera`

Es el comando core-general.

Subcomandos confirmados:

-   `help`
-   `variables`
-   `reload`

La gestión de keybinds ya no vive bajo `/theosfera`.

### `/keybind`

Es el comando administrativo independiente de keybinds.

Funciones confirmadas:

-   `help`
-   `list`
-   `menu`
-   `get`
-   `edit`
-   `add`
-   `remove`
-   gestión de `action`

`/keybind` y `/keybind help` muestran la misma ayuda.

`KeybindCommand` y `KeybindTabCompleter` están separados.

### `/key <key>`

Ejecuta la keybind asociada a la tecla indicada.

### Migración cerrada

-   `/theosfera keybind` ya no existe.
-   Sus referencias textuales antiguas fueron eliminadas.
-   Las referencias obsoletas también fueron eliminadas de `AGENTS.md`.
-   `/theosfera help` puede anunciar `/keybind`, pero no debe restaurar
    `/theosfera keybind`.

## 6. Seguridad administrativa

Permiso confirmado:

`theosfera.admin`

Configuración:

`default: false`

La intención es mantener discretos los comandos internos para jugadores
sin permiso.

El acceso administrativo fue probado en servidor mediante LuckPerms.

## 7. Modelo y gestión de keybinds

Una keybind contiene:

-   `id`
-   `name`
-   `description`
-   `key`
-   `actions`

### Validación de nombre

Los nombres no admiten espacios.

Se permiten separadores como:

-   `-`
-   `_`

### Validación de tecla

Una tecla normal admite un único carácter.

También se contemplan explícitamente `F1` a `F12`.

### Duplicados

Las teclas duplicadas se bloquean.

La creación no permite una nueva keybind si la tecla ya está ocupada.

La edición de tecla también comprueba duplicados.

### Operaciones confirmadas

La lógica/comandos soportan:

-   listar keybinds;
-   obtener una keybind;
-   añadir;
-   editar nombre;
-   editar descripción;
-   editar tecla;
-   eliminar;
-   listar acciones;
-   añadir acciones;
-   editar acciones;
-   mover acciones;
-   eliminar acciones.

`getAllSorted()` se obtiene una sola vez al abrir la lista de keybinds.

## 8. Acciones de keybind

Tipos confirmados en `KeybindActionType`:

-   `PLAYER_COMMAND`
-   `CONSOLE_COMMAND`
-   `MESSAGE`

`KeybindAction` representa una acción mediante tipo y valor.

La lógica real de gestión permanece en `KeybindManager`.

## 9. Variables

`VariableService` está implementado.

`/theosfera variables` funciona.

Variables confirmadas:

-   `%player%`
-   `%player_name%`
-   `%player_uuid%`
-   `%player_world%`
-   `%player_x%`
-   `%player_y%`
-   `%player_z%`

`VariableService` se propaga al contexto de acciones de menú mediante
`MenuActionContext`.

## 10. Idiomas y mensajes

Idiomas implementados:

-   español;
-   inglés.

Recursos:

-   `lang/es.yml`
-   `lang/en.yml`

Clases principales:

-   `Language`
-   `MessageService`

### Selección de idioma

-   Si el locale del jugador comienza por `es`, se usa español.
-   En otro caso se usa inglés como fallback.

### Robustez

El sistema dispone de:

-   defaults internos;
-   merge de paths nuevos conservando personalizaciones;
-   fallback ante YAML de idioma roto;
-   reload funcional de idiomas.

Los textos visibles de `TheosferaCommand` fueron migrados al sistema
multilenguaje.

`MessageService` soporta placeholders y formato legacy/HEX.

### Prompts de menús

Los prompts visibles de entrada por chat en:

-   `KeybindEditMenuActionHandler`;
-   `KeybindActionMenuActionHandler`;

usan `MessageService.sendLineKey` y se resuelven desde:

-   `lang/es.yml`;
-   `lang/en.yml`.

Se localizaron los flujos de:

-   edición de nombre;
-   edición de descripción;
-   edición de tecla;
-   adición de acciones;
-   edición de acciones.

La cancelación acepta:

-   `cancelar`;
-   `cancel`.

Los nuevos paths se incorporan automáticamente a archivos de idioma
existentes sin eliminar personalizaciones.

### Centrado visual

`MessageCenteringService` proporciona centrado visual real.

`MessageService.sendTitle` usa el servicio de centrado.

También existe `sendCentered`.

La acción de menú `[centered_message]` fue implementada y probada.

## 11. Sistema configurable de menús

Menús actuales:

-   `menus/keybind-list.yml`
-   `menus/keybind-details.yml`
-   `menus/keybind-edit.yml`
-   `menus/keybind-actions.yml`

El antiguo `keybinds-menu.yml` fue eliminado.

El sistema de menús es configurable por YAML y reutiliza la lógica real
del core.

### `MenuManager`

Gestiona los menús cargados.

`keybind-edit.yml` inicialmente no abría porque no estaba
cargado/registrado. La carga fue corregida y el menú abrió correctamente
en servidor.

### `MenuConfigLoader`

Carga la configuración de menús.

Registra advertencias para slots inválidos.

### `SlotParser`

Permite definir slots y rangos configurables desde YAML.

### `MenuConfig` y `MenuItemConfig`

Representan menús e ítems configurados.

Los ítems pueden definir:

-   id;
-   material;
-   nombre;
-   lore;
-   slot o slots;
-   acciones.

## 12. Renderizado de menús

### `MenuRenderer`

Coordina la creación de inventarios.

Flujos confirmados:

-   lista de keybinds;
-   detalles;
-   reutilización del renderizado ligado a una keybind para menús como
    edición.

Crea el inventario con `MenuHolder` y posteriormente asigna al holder la
referencia real del `Inventory`.

### `KeybindMenuRenderer`

Responsable del contenido dinámico de keybinds.

IDs especiales confirmados:

-   `keybind-entry`
-   `keybind-info`

Placeholders confirmados:

-   `%keybind_id%`
-   `%keybind_name%`
-   `%keybind_description%`
-   `%keybind_key%`
-   `%keybind_actions%`

El menú dinámico de acciones muestra información y conteo de acciones
mediante placeholders configurados.

## 13. `MenuHolder`

Estado conservado:

-   `menuId`
-   `page`
-   `maxPage`
-   `keybindId`
-   referencia real a `Inventory`

`keybindId` usa `OptionalInt`.

Existen constructores para:

-   menú paginado;
-   menú asociado a una keybind;
-   estado completo con página máxima y keybind opcional.

`setInventory()` asigna la referencia después de crear el inventario
Bukkit.

Este ajuste resolvió el problema relacionado con
`InventoryHolder#getInventory()`.

## 14. `MenuItemFactory`, PDC y protección de ítems

`MenuItemFactory` construye los `ItemStack` de los menús.

Usa:

-   `MenuTextResolver`;
-   `MenuActionCodec`;
-   `PersistentDataContainer`.

Protecciones confirmadas:

-   material nulo;
-   material vacío;
-   material desconocido;
-   `AIR`;
-   materiales no utilizables como ítem;
-   materiales sin `ItemMeta`.

En casos inválidos usa `STONE` como fallback y registra una advertencia.

### Tags PDC confirmados

`MenuItemTag` contiene:

-   `ITEM_ID`
-   `KEYBIND_ID`
-   `ACTIONS`
-   `ACTION_INDEX`

Los ítems dinámicos conservan el ID de keybind y, en el menú de
acciones, el índice real de la acción.

## 15. Codificación de acciones de menú

`MenuActionCodec` codifica cada acción en Base64 URL-safe.

Las acciones codificadas se separan mediante punto.

Esto evita una separación frágil por caracteres comunes y preserva
valores complejos.

Las acciones se guardan en PDC bajo `MenuItemTag.ACTIONS`.

`MenuListener` decodifica la lista y envía cada acción a
`MenuActionExecutor`.

## 16. Arquitectura de acciones de menú

### `MenuActionExecutor`

Es un coordinador.

Flujo:

1.  parsear mediante `MenuActionParser`;
2.  validar la acción;
3.  resolver el handler en `MenuActionRegistry`;
4.  ejecutar el handler;
5.  registrar warnings para acciones mal formadas o desconocidas.

Formato esperado:

`[tipo] valor`

Handlers registrados:

-   `NavigationMenuActionHandler`
-   `MessageMenuActionHandler`
-   `CommandMenuActionHandler`
-   `EffectMenuActionHandler`
-   `KeybindEditMenuActionHandler`
-   `KeybindActionMenuActionHandler`

### `MenuActionContext`

Transporta las dependencias necesarias.

Referencias confirmadas:

-   plugin;
-   player;
-   holder;
-   PDC del ítem;
-   `MenuService`;
-   `VariableService`;
-   `MessageService`;
-   `KeybindManager`;
-   `MenuChatInputService`;
-   tipo de click.

`ClickType` fue propagado al contexto para diferenciar click izquierdo y
derecho.

## 17. Handlers de acciones de menú

### `NavigationMenuActionHandler`

Acciones confirmadas:

-   `close`
-   `back`
-   `previous_page`
-   `next_page`
-   `keybind_details`
-   `open_menu`

Los límites de paginación no reabren inútilmente la primera o última
página.

`keybind_details` obtiene `KEYBIND_ID` desde PDC.

`open_menu` resuelve explícitamente:

-   `keybind-list`
-   `keybind-details`
-   `keybind-edit`

La navegación conserva la página de origen mediante `MenuHolder`.

### `MessageMenuActionHandler`

Gestiona acciones de mensajes, incluido mensaje centrado.

### `CommandMenuActionHandler`

Gestiona acciones de comandos desde menús.

### `EffectMenuActionHandler`

Gestiona efectos.

Incluye validaciones y warnings para sonidos y partículas inválidos.

`ParticleActionConfig` y `ParticleActionParser` pertenecen a este flujo.

### `KeybindEditMenuActionHandler`

Tipo registrado:

`keybind_edit`

Campos manejados:

-   `name`
-   `description`
-   `key`

Flujo:

1.  obtiene `keybindId` desde `MenuHolder`;
2.  verifica la existencia de la keybind;
3.  cierra el inventario;
4.  solicita el nuevo valor por chat;
5.  permite `cancelar` y `cancel`;
6.  valida el dato;
7.  llama a `KeybindManager`;
8.  envía el mensaje correspondiente;
9.  reabre el menú de edición.

#### Nombre

Usa `KeybindValidator.isValidName`.

Probado y funcional por chat.

#### Descripción

Usa `KeybindValidator.isValidDescription`.

Probado y funcional por chat.

#### Tecla

Normaliza la entrada a mayúsculas.

Usa `KeybindValidator.isValidKey`.

Llama a `KeybindManager.editKey`.

Si la tecla está duplicada, muestra el error y no aplica el cambio.

Probado y funcional por chat.

Los prompts de nombre, descripción y tecla usan claves localizadas y
fueron probados en español e inglés.

### `KeybindActionMenuActionHandler`

Tipo registrado:

`keybind_action`

Valores manejados:

-   `manage`
-   `add`

#### `manage`

Estado confirmado:

-   click derecho: elimina la acción;
-   click izquierdo: edita la acción mediante entrada por chat.

Obtiene `ACTION_INDEX` desde PDC y `keybindId` desde `MenuHolder`.

La eliminación llama a `KeybindManager.removeAction`.

La edición:

1.  solicita el nuevo tipo;
2.  valida `PLAYER_COMMAND`, `CONSOLE_COMMAND` o `MESSAGE`;
3.  solicita el nuevo valor;
4.  permite `cancelar` y `cancel`;
5.  rechaza valores vacíos;
6.  llama a `KeybindManager.editAction`;
7.  reabre `keybind-actions` conservando la página.

La eliminación con click derecho y la edición con click izquierdo fueron
probadas en servidor y funcionan.

#### `add`

Implementa dos entradas por chat:

1.  solicitar tipo;
2.  solicitar valor.

Tipos aceptados:

-   `PLAYER_COMMAND`
-   `CONSOLE_COMMAND`
-   `MESSAGE`

Permite `cancelar` y `cancel`.

Valida tipo y valor vacío.

Finalmente llama a `KeybindManager.addAction` y reabre el menú de
acciones.

Los prompts de adición y edición de acciones usan claves localizadas y
fueron probados en español e inglés.

## 18. Entrada por chat

### `MenuChatInputService`

Administra solicitudes pendientes por jugador.

Se usa en edición de campos y adición de acciones.

### `MenuChatInputListener`

Consume la entrada de chat asociada a una solicitud pendiente.

Los flujos implementados reabren el menú correspondiente después de:

-   cancelar;
-   error de validación;
-   edición exitosa;
-   adición exitosa.

## 19. `MenuListener` y protección de inventarios

### Clicks

Si el holder no es `MenuHolder`, el evento se ignora.

Para menús de Theosfera:

-   cancela el evento;
-   exige `Player`;
-   valida raw slot;
-   ignora clicks fuera del inventario superior;
-   obtiene el ítem;
-   obtiene PDC;
-   lee y decodifica `ACTIONS`;
-   construye `MenuActionContext`;
-   propaga `event.getClick()`;
-   ejecuta las acciones.

### Drags

`InventoryDragEvent` se cancela si alguno de los raw slots afectados
pertenece al inventario superior manejado por `MenuHolder`.

La protección de click y drag fue probada en servidor.

## 20. Estado de cada menú

### `keybind-list.yml`

Confirmado:

-   listado dinámico;
-   `keybind-entry`;
-   paginación;
-   límites seguros;
-   `KEYBIND_ID` por entrada;
-   apertura de detalles;
-   una sola obtención de `getAllSorted()` por apertura.

### `keybind-details.yml`

Confirmado:

-   información de keybind;
-   `keybind-info`;
-   apertura desde entrada dinámica;
-   conservación de página de origen;
-   back a la misma página del listado.

### `keybind-edit.yml`

Confirmado:

-   apertura desde detalles;
-   conservación de `keybindId`;
-   edición de nombre;
-   edición de descripción;
-   edición de tecla;
-   las tres ediciones fueron probadas visualmente en servidor;
-   los valores actualizados se reflejan al reabrir.

### `keybind-actions.yml`

Confirmado:

-   tamaño 54;
-   `keybind-info` en slot 4;
-   `action-entry` dinámico en rangos configurados;
-   número, tipo y valor de cada acción;
-   botón para añadir;
-   botón para volver a `keybind-edit`.

Se corrigió una estructura YAML errónea en `action-entry`.

La forma correcta deja directamente:

``` yaml
actions:
  - "[keybind_action] manage"
```

Después de corregir el YAML, la eliminación por click derecho funcionó.

## 21. Pruebas y builds confirmados

Se realizaron múltiples builds Gradle exitosos.

Salidas registradas:

-   `BUILD SUCCESSFUL in 1s`
-   `BUILD SUCCESSFUL in 958ms`
-   `BUILD SUCCESSFUL in 2s`
-   `BUILD SUCCESSFUL in 250ms`
-   `BUILD SUCCESSFUL in 6s`

No había tests automatizados en esos builds:

-   `compileTestJava NO-SOURCE`
-   `test NO-SOURCE`

Pruebas funcionales confirmadas:

-   `/keybind` validado con LuckPerms;
-   menú de listado abre;
-   paginación y límites seguros trabajados;
-   menú de detalles abre;
-   back conserva página;
-   bloqueo de inventario probado;
-   bloqueo de drag del inventario superior añadido/probado;
-   menú de edición abre después de corregir su carga;
-   edición de nombre funciona;
-   edición de descripción funciona;
-   edición de tecla funciona;
-   cambios se reflejan al reabrir;
-   menú dinámico de acciones abre y muestra acciones;
-   click derecho elimina una acción;
-   click izquierdo edita el tipo y valor de una acción;
-   cancelación y validaciones de la edición de acciones funcionan;
-   mensajes centrados implementados y probados;
-   warnings/protecciones de materiales, acciones, sonidos y partículas
    incorporados;
-   builds posteriores a `KeybindActionMenuActionHandler` exitosos.
-   prompts de edición de nombre, descripción y tecla probados en
    español e inglés;
-   prompts de adición y edición de acciones probados en español e
    inglés;
-   `cancelar` funciona en los flujos localizados;
-   `cancel` funciona en los flujos localizados;
-   tipos de acción inválidos muestran el error localizado;
-   no se muestran claves internas de idioma al jugador;
-   `/theosfera reload` conserva traducciones y datos;
-   un reinicio completo conserva las modificaciones;
-   los nuevos paths se añadieron automáticamente a los archivos de
    idioma existentes;
-   las personalizaciones existentes se conservaron;
-   la batería de localización terminó con 47 de 47 verificaciones
    aprobadas;
-   no se registraron errores, stack traces ni warnings de paths
    faltantes.

## 22. Decisiones cerradas y elementos eliminados

-   `/theosfera keybind` fue eliminado.
-   `/keybind` quedó como comando administrativo independiente.
-   Las referencias antiguas fueron eliminadas, incluido `AGENTS.md`.
-   `keybinds-menu.yml` fue eliminado.
-   Los menús se separan en lista, detalles, edición y acciones.
-   `MenuActionExecutor` es coordinador sobre un registry de handlers.
-   Navegación, mensajes, comandos y efectos tienen handlers separados.
-   La lógica de keybinds permanece en `KeybindManager`.
-   Skyblock no se integra dentro de TheosferaCore.
-   El futuro trabajo de Skyblock irá a `TheosferaSkyblockAddons` sobre
    SuperiorSkyblock2.
-   La fundación GitHub ya está fusionada y no debe repetirse.

## 23. Incidencia local conocida: Windows y OneDrive

El checkout local se encuentra dentro de OneDrive.

Gradle y Git han mostrado problemas ocasionales al eliminar directorios
porque Windows, IntelliJ, OneDrive u otro proceso mantiene archivos
abiertos.

Síntoma de Gradle:

`Unable to delete directory '...\build'`

Procedimiento que funcionó:

``` powershell
.\gradlew.bat --stop
Remove-Item -Recurse -Force .\build
.\gradlew.bat build --no-daemon
```

Antes de repetir eliminaciones forzadas de ramas o directorios internos
de Git, comprobar el estado real con:

``` powershell
git branch
git status
```

Git llegó a reportar fallos al eliminar carpetas internas de
referencias, aunque la rama ya no aparecía después.

## 24. TheosferaPluginTemplate

Existe el repositorio `HarriOcho/TheosferaPluginTemplate`.

Está configurado como **Public template** y dispone de **Use this
template**.

Base confirmada:

-   Java 21;
-   Gradle Kotlin DSL;
-   Gradle Wrapper 9.0.0;
-   Paper API 1.21.11;
-   GitHub Actions;
-   Configuration Cache compatible;
-   `.gitignore`;
-   `README.md`;
-   `AGENTS.md`;
-   `CONTRIBUTING.md`;
-   plantillas de Pull Request, bug y feature;
-   clase principal genérica;
-   `plugin.yml`.

El build final de la plantilla terminó con `BUILD SUCCESSFUL` y
`Configuration cache entry stored.`

El primer commit fue:

`chore: initialize professional plugin template`

La plantilla quedó sincronizada con `origin/main` y con working tree
limpio.

Este repositorio es la base para futuros plugins. No trasladar lógica
específica de TheosferaCore a la plantilla.

## 25. Edición de acciones completada

La edición de acciones mediante click izquierdo desde
`keybind-actions` quedó confirmada en el código real y fue probada en
servidor.

Flujo validado:

1.  obtiene `ACTION_INDEX` desde PDC;
2.  obtiene `keybindId` desde `MenuHolder`;
3.  diferencia click izquierdo y derecho mediante `ClickType`;
4.  reutiliza `MenuChatInputService`;
5.  solicita y valida el nuevo tipo;
6.  solicita y valida el nuevo valor;
7.  llama a `KeybindManager.editAction`;
8.  persiste el cambio;
9.  muestra el resultado;
10. reabre el menú conservando la página.

También fueron confirmados la cancelación, los tipos inválidos y los
valores vacíos.

## 26. Batería de pruebas pendiente

Después de completar la edición con click izquierdo, probar de extremo a
extremo:

-   lista;
-   páginas;
-   detalles;
-   back;
-   edición de nombre;
-   cancelación de nombre;
-   nombre inválido;
-   edición de descripción;
-   descripción inválida o vacía;
-   edición de tecla;
-   tecla inválida;
-   tecla duplicada;
-   adición de `PLAYER_COMMAND`;
-   adición de `CONSOLE_COMMAND`;
-   adición de `MESSAGE`;
-   tipo de acción inválido;
-   valor vacío;
-   cancelación durante adición;
-   eliminación con click derecho;
-   persistencia tras reload;
-   persistencia tras reinicio;
-   YAML mal formado;
-   materiales o elementos inválidos;
-   límites de paginación;
-   clicks fuera del inventario superior;
-   drag sobre el inventario superior.

## 27. Localización de prompts completada

Los textos visibles del flujo de chat fueron migrados desde literales
hardcodeados hacia el sistema multilenguaje.

Archivos Java actualizados:

-   `KeybindEditMenuActionHandler`;
-   `KeybindActionMenuActionHandler`.

Recursos actualizados:

-   `lang/es.yml`;
-   `lang/en.yml`.

Resultado confirmado:

-   no quedan llamadas `sendLine` con prompts hardcodeados en estos
    handlers;
-   se usan ocho claves mediante `sendLineKey`;
-   español e inglés contienen los mismos paths;
-   `cancelar` y `cancel` son aceptados;
-   el build fue exitoso;
-   las pruebas en servidor fueron aprobadas.

### PlaceholderAPI

PlaceholderAPI permanece pendiente del roadmap.

Debe diseñarse como integración opcional para evitar que su ausencia
afecte los módulos no relacionados de TheosferaCore.

No se considera implementado.

## 28. Punto exacto de reanudación

La administración gráfica de keybinds y sus prompts multilenguaje están
funcionales y fueron probados de extremo a extremo.

Estado confirmado:

-   listado y paginación;
-   detalles y navegación;
-   edición de nombre, descripción y tecla;
-   listado, adición, edición y eliminación de acciones;
-   prompts en español e inglés;
-   cancelación mediante `cancelar` y `cancel`;
-   persistencia tras reload y reinicio;
-   merge automático de nuevos paths de idioma;
-   consola sin errores relacionados.

El siguiente punto recomendado es inspeccionar el uso actual de
placeholders y diseñar la integración opcional con PlaceholderAPI antes
de modificar código.

La integración debe:

1.  ser opcional;
2.  detectar si PlaceholderAPI está disponible;
3.  no romper TheosferaCore cuando esté ausente;
4.  preservar `VariableService`;
5.  evitar expansión repetida o innecesaria;
6.  registrar una alerta clara si una característica dependiente queda
    desactivada.

Nombre de rama sugerido para la fase de implementación:

`feature/placeholderapi-hook`

No implementar la integración sin revisar primero:

-   `VariableService`;
-   `plugin.yml`;
-   `TheosferaCore`;
-   los puntos actuales donde se resuelven variables;
-   las reglas de módulos y dependencias opcionales de `AGENTS.md`.

# TheosferaCore --- Instrucciones para agentes

## Propósito

Este archivo contiene instrucciones persistentes del repositorio para
agentes de código. Antes de modificar el proyecto, lee este archivo y
los archivos relevantes. Conserva las convenciones correctas existentes
y evita reescribir código funcional no relacionado con la tarea.

## Identidad del proyecto

TheosferaCore es el core principal del servidor Minecraft Theosfera.

-   Propietario y alias de desarrollo: HarriOcho.
-   Plataforma: Paper.
-   Java: 21.
-   Build: Gradle con Kotlin DSL.
-   Versión objetivo actual de Minecraft/Paper: 1.21.11.
-   Paquete raíz: `com.theosfera.core`.

El proyecto está en desarrollo temprano. Mantén una arquitectura limpia,
modular, eficiente y escalable.

## Prioridades

Prioriza en este orden:

1.  Corrección.
2.  Estabilidad del servidor.
3.  Rendimiento.
4.  Mantenibilidad.
5.  Legibilidad.
6.  Extensibilidad.

No sacrifiques corrección ni mantenibilidad por microoptimizaciones sin
impacto medible.

Evita trabajo innecesario en el hilo principal de Minecraft, acceso
repetido al disco en rutas frecuentes, parsing repetido de YAML, polling
innecesario, tareas del scheduler sin necesidad y creación innecesaria
de objetos en eventos de alta frecuencia.

No introduzcas asincronía prematura. Respeta las restricciones de hilo
de Paper y Bukkit.

## Arquitectura

TheosferaCore debe permanecer modular.

No concentres grandes cantidades de lógica en `TheosferaCore`,
`TheosferaCommand` ni listeners.

`TheosferaCore` se ocupa principalmente del ciclo de vida,
inicialización de servicios y módulos, registro de comandos y apagado
controlado.

Los comandos validan sintaxis, entrada y permisos; delegan a managers o
services; y presentan resultados. No deben implementar persistencia.

Las clases storage se ocupan de carga y persistencia.

Los managers y services contienen lógica de negocio y coordinan
operaciones.

Los modelos representan datos.

Separación conceptual actual:

-   `KeybindCommand`: entrada administrativa de keybinds.
-   `KeybindTabCompleter`: sugerencias contextuales de keybinds.
-   `TheosferaCommand`: entrada administrativa general del core.

Si una clase crece demasiado, separa responsabilidades. Evita god
classes, estado mutable global estático y service locators. Prefiere
inyección por constructor.

## Sistema de módulos

Cada característica principal debe poder evolucionar a un módulo
independiente.

Ejemplos:

-   keybinds;
-   integración con SuperiorSkyblock2;
-   NPC;
-   hologramas.

Cada módulo debe poder configurarse individualmente y desactivarse desde
YAML. El control administrativo en runtime podrá añadirse mediante
comandos de TheosferaCore.

Si un módulo se desactiva, sus listeners, tareas, hooks y funcionalidad
activa no deben continuar consumiendo recursos innecesariamente.

Un fallo en un módulo opcional no debe apagar todo TheosferaCore si el
resto puede continuar de forma segura.

Diseña el ciclo de vida para evitar listeners, tareas o hooks duplicados
después de una recarga.

## Theosfera Keybinds y el core

TheosferaCore no depende globalmente del mod cliente Theosfera Keybinds.

Si un jugador no tiene el mod, solo deben quedar indisponibles las
funciones que requieran explícitamente el mod cliente. Los demás módulos
de TheosferaCore deben continuar funcionando.

## Dependencias opcionales

Las integraciones externas deben tratarse normalmente como opcionales
salvo que todo el plugin requiera fundamentalmente una dependencia.

Posibles integraciones incluyen SuperiorSkyblock2, Citizens,
DecentHolograms y PlaceholderAPI.

Una dependencia opcional ausente, deshabilitada, incompatible o con
inicialización fallida no debe romper módulos no relacionados.

Cuando una integración falle:

1.  Desactiva únicamente el módulo o integración afectada cuando sea
    posible.
2.  Registra una alerta muy visible en consola.
3.  Indica la dependencia afectada.
4.  Indica el módulo desactivado.
5.  Mantén operativos los módulos no relacionados.

Formato conceptual de alerta:

    ============================================================
                         THEOSFERA ALERT
    ============================================================
    Dependencia: SuperiorSkyblock2
    Estado: NO DISPONIBLE
    Módulo afectado: Island Integration
    Acción: La característica afectada ha sido desactivada.
    ============================================================

No hagas spam de alertas. Registra la alerta una vez durante la
inicialización o ante un cambio significativo de estado.

## Mensajes para jugadores

Nunca muestres a jugadores normales stack traces, excepciones sin
procesar, nombres internos de clases Java ni detalles técnicos de
implementación.

Si una característica está desactivada por su módulo o integración, usa
un mensaje amigable equivalente a:

`Esta característica está desactivada por el momento.`

Los detalles técnicos pertenecen a la consola.

## Identidad visual

Usa `TheosferaPalette` y `MessageService` cuando corresponda.

Paleta actual:

-   `GOLD`: `#DFB864`
-   `LIGHT`: `#F6E389`
-   `BRONZE`: `#985D24`
-   `COPPER`: `#6E320F`
-   `IVORY`: `#F2E8D5`
-   `GRAY`: `#A99D8C`
-   `MUTED`: `#70685D`
-   `DARK`: neutral oscuro
-   `SUCCESS`: éxito semántico
-   `WARNING`: advertencia semántica
-   `ERROR`: error semántico

No dupliques literales HEX de la paleta en clases no relacionadas.

Jerarquía visual:

-   Hero/display: `LIGHT`.
-   Títulos: `GOLD` o `LIGHT`.
-   Subtítulos: `GOLD` o `BRONZE`.
-   Texto normal: `IVORY`.
-   Texto secundario: `GRAY`.
-   Información atenuada o desactivada: `MUTED`.
-   Énfasis inline: `GOLD`.
-   Estados: colores semánticos.

No uses grandes separadores ASCII en interfaces normales del chat del
jugador. Prefiere espacios en blanco y composición limpia. Los
separadores grandes sí están permitidos en alertas importantes de
consola.

## Sistema de keybinds

Los keybinds se almacenan actualmente en `keybinds.yml`.

Cada keybind tiene ID numérico, nombre, tecla y lista ordenada de
acciones.

Ejemplo conceptual:

    keybinds:
      '1':
        name: 'abrir_inventario'
        key: 'K'
        actions:
          - 'player_command: dm open %player% menu'
          - 'message: Preparando interfaz...'

El ID numérico es la identidad administrativa estable.

Los nombres son editables y no son el identificador principal.

No cambies IDs existentes silenciosamente ni renumeres todos los
keybinds después de eliminar uno.

El manager actual puede reutilizar el primer ID positivo disponible.

Los nombres no deben contener espacios. Normaliza las teclas de forma
consistente. Conserva el orden de las acciones.

## Tipos de acciones

Tipos soportados:

-   `PLAYER_COMMAND`
-   `CONSOLE_COMMAND`
-   `MESSAGE`

Nombres YAML:

-   `player_command`
-   `console_command`
-   `message`

`PLAYER_COMMAND` ejecuta un comando como jugador.

`CONSOLE_COMMAND` ejecuta un comando como consola.

Los comandos almacenados no deben requerir `/` inicial.

Los placeholders como `%player%` solo deben sustituirse cuando la
implementación lo soporte explícitamente.

`MESSAGE` envía un mensaje al jugador y debe integrarse con el sistema
centralizado de mensajes cuando corresponda.

No infieras tipos desconocidos. Rechaza o ignora de forma segura
acciones desconocidas o malformadas y registra información apropiada.

Nunca ejecutes una acción malformada como comando de consola por
fallback.

## Comandos actuales

Árbol actual:

    /theosfera
    /theosfera help
    /theosfera reload
    /theosfera variables

    /keybind
    /keybind list <page>
    /keybind menu <page>
    /keybind get <ID> <name|description|key|actions>
    /keybind edit <ID> <name|description|key> <change>
    /keybind add <name> <key>
    /keybind remove <ID>

La administración de acciones usa:

    /keybind action
    /keybind action list <ID>
    /keybind action add <ID> <type> <value>
    /keybind action edit <ID> <action> <type> <value>
    /keybind action move <ID> <action> <position>
    /keybind action remove <ID> <action>

El tab completion debe ser contextual.

Debe sugerir comandos raíz, subcomandos, IDs existentes, campos `name`,
`key` y `actions`, y los tipos `PLAYER_COMMAND`, `CONSOLE_COMMAND` y
`MESSAGE` en los contextos apropiados.

Cuando exista filtrado de permisos, el tab completion no debe revelar
comandos administrativos a usuarios sin autorización.

## Arquitectura futura de acciones

La implementación actual puede aplicar un mismo tipo a varias acciones
separadas por comas.

Antes de ampliar significativamente los keybinds, prefiere una
administración granular:

    /keybind action
    /keybind action list <ID>
    /keybind action add <ID> <type> <value>
    /keybind action edit <ID> <action> <type> <value>
    /keybind action move <ID> <action> <position>
    /keybind action remove <ID> <action>

Esto permitirá mezclar tipos en un mismo keybind.

Ejemplo:

    actions:
      - 'message: Preparando teletransporte...'
      - 'player_command: spawn'
      - 'console_command: effect give %player% speed 5 1'

## YAML y persistencia

Se prefiere YAML legible por humanos.

Las cadenas pueden escribirse entre comillas cuando mejoren claridad o
reduzcan ambigüedad visual.

No confundas el estilo visual de las comillas con la lógica semántica.
El parser determina el valor cargado.

Usa APIs estructuradas de configuración. No analices manualmente texto
YAML cuando las API de Paper/Bukkit puedan cargar los datos de forma
segura.

Registra fallos de persistencia. No informes éxito si el guardado falló.

Evita guardar el archivo completo en rutas frecuentes. Las operaciones
administrativas `add`, `edit` y `remove` pueden persistir inmediatamente
por ser de baja frecuencia.

## Recarga

`/theosfera reload` debe recargar únicamente TheosferaCore de forma
controlada.

Nunca invoques el `/reload` global de Bukkit o Paper ni reinicies el
servidor desde el plugin.

Proceso conceptual:

1.  detener o liberar recursos existentes cuando sea necesario;
2.  recargar configuración;
3.  validar dependencias;
4.  inicializar módulos habilitados;
5.  informar el resultado.

No registres listeners, tareas o hooks dos veces.

## Permisos

El permiso administrativo principal previsto es `theosfera.admin`.

No expongas administración de keybinds a jugadores normales.

Las funciones para jugadores podrán usar permisos separados
posteriormente.

Aplica el control de permisos tanto a ejecución como a tab completion
administrativo cuando se implemente.

## Estilo de código

Usa Java 21 cuando mejore la claridad.

Prefiere clases `final` cuando no se pretenda herencia, datos
inmutables, records para modelos simples, inyección por constructor,
métodos pequeños, nombres significativos, retornos tempranos y
`Optional` cuando la ausencia sea parte significativa del contrato.

Evita reflexión sin razón fuerte, excepciones ignoradas, `catch` vacíos,
sincronización innecesaria, asincronía prematura, parsing duplicado,
formato duplicado, colores duplicados, condicionales profundamente
anidados y magic strings dispersos.

No añadas dependencias para problemas triviales que Java o Paper ya
resuelvan limpiamente.

## Reglas de Paper

Este es un plugin de Paper.

Prefiere APIs modernas de Paper.

No uses NMS salvo necesidad demostrada.

No introduzcas hacks de reflexión específicos de versión sin documentar
el motivo.

Asume que las API de mundo y entidades de Bukkit/Paper no son seguras
para acceso asíncrono salvo documentación explícita en contrario.

No bloquees innecesariamente el hilo principal con I/O pesado o trabajo
computacional significativo.

## Flujo obligatorio para agentes

Antes de modificar código:

1.  Lee este `AGENTS.md`.
2.  Inspecciona las clases relevantes.
3.  Comprende la implementación actual.
4.  Preserva las convenciones válidas.
5.  Limita los cambios al alcance de la tarea.
6.  Identifica riesgos de compatibilidad o arquitectura.

Durante los cambios:

1.  No reescribas archivos no relacionados sin necesidad.
2.  No elimines funcionalidad operativa sin justificación.
3.  Prefiere cambios enfocados frente a reescrituras masivas.
4.  Mantén la separación de responsabilidades.
5.  Añade comentarios solo cuando expliquen decisiones no obvias.

Después de modificar código:

1.  Ejecuta el build de Gradle.
2.  Informa si tuvo éxito o falló.
3.  Resume los archivos modificados.
4.  Explica los cambios de comportamiento.
5.  Menciona warnings o problemas pendientes.
6.  Indica pasos exactos de verificación manual.

Nunca afirmes que algo fue probado dentro de Minecraft si no se ejecutó
realmente en un servidor de prueba.

`BUILD SUCCESSFUL` demuestra éxito del build, no corrección de runtime.

Si no puedes ejecutar una prueba necesaria, dilo claramente.

Si una solicitud crea deuda técnica importante, explica el problema y
propone una alternativa más limpia antes de introducirla.

## Estado actual conocido

Estructura aproximada:

    com.theosfera.core
    ├── TheosferaCore
    ├── command
    │   ├── TheosferaCommand
    │   └── TheosferaTabCompleter
    ├── keybind
    │   ├── KeybindActionType
    │   ├── KeybindEntry
    │   ├── KeybindManager
    │   └── KeybindStorage
    └── ui
        ├── MessageService
        └── TheosferaPalette

Recursos conocidos:

    plugin.yml
    keybinds.yml

El proyecto compilaba correctamente al redactar estas instrucciones.

El sistema administrativo inicial de keybinds fue probado manualmente en
un servidor local antes de los últimos refinamientos de acciones tipadas
y tab completion.

No asumas que cambios posteriores fueron probados en runtime sin
confirmación explícita.

## Comunicación con HarriOcho

Responde en español salvo solicitud contraria.

HarriOcho está aprendiendo desarrollo de plugins Java y Paper
activamente.

Explica conceptos desconocidos de forma sencilla. No asumas
conocimientos avanzados de Java. Explica por qué se realiza un cambio
estructural. Indica con precisión archivos, clases y métodos.
Proporciona pasos exactos de verificación cuando sea posible.

Diferencia claramente entre error de compilación, warning, problema de
arquitectura y fallo de runtime.

Mantén un tono cercano, colaborativo y claro.

No uses complejidad innecesaria para aparentar sofisticación. La meta es
que HarriOcho comprenda el proyecto mientras TheosferaCore crece con
calidad.

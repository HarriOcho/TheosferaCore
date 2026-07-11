# Theosfera — Roadmap modular

## 1. Propósito

Theosfera se diseña como una network compuesta por servicios globales,
servidores Paper y plugins especializados por modalidad.

TheosferaCore contiene identidad y servicios generales compartidos.
TheosferaProxy coordina presencia, comunicación y movimientos globales.
Los plugins de modalidad contienen mecánicas, misiones, logros,
economías e integraciones específicas.

## 2. Topología inicial

```text
Theosfera Network
├── Proxy
├── Auth
├── Lobby
└── Skyblock
```

La arquitectura permitirá añadir posteriormente Survival, Prison,
Minijuegos y otras modalidades. Los nombres técnicos de instancias, como
`skyblock-01`, no deben confundirse con la modalidad lógica `SKYBLOCK`.

## 3. Componentes principales

### TheosferaCore

Plugin instalado en servidores Paper.

Responsabilidades generales:

- keybinds;
- perfiles;
- motor de menús configurables;
- comandos esenciales;
- estadísticas globales;
- progreso general;
- infraestructura de waypoints;
- integraciones Paper;
- API para plugins de modalidad.

### TheosferaProxy

Plugin futuro para Velocity.

Responsabilidades globales:

- presencia;
- servidor y modalidad actual;
- sesiones de red;
- movimientos entre servidores;
- amigos;
- parties;
- escuadrones;
- invitaciones;
- eventos entre servidores;
- coordinación de `party warp` y `party tpauto`.

### Plugins de modalidad

`TheosferaSkyblockAddons` contendrá:

- integración con SuperiorSkyblock2;
- misiones, logros y estadísticas de Skyblock;
- objetivos y waypoints de misiones;
- recompensas y economías específicas;
- crafteo personalizado;
- encantamientos personalizados;
- yunque personalizado;
- ender chests avanzados;
- player vaults.

## 4. Persistencia y sincronización

### Base de datos central

Datos permanentes:

- perfiles;
- amigos;
- escuadrones;
- niveles y puntos;
- logros y misiones;
- preferencias y privacidad;
- estadísticas;
- historial de modalidades;
- beneficios.

### Redis

Estado temporal y comunicación:

- presencia;
- servidor y modalidad actual;
- parties e invitaciones;
- periodos de reconexión;
- eventos Core–Proxy;
- `tpauto`;
- caché e invalidación.

La base de datos es la fuente permanente. Redis coordina estado y eventos
en tiempo real. Un fallo de Redis no debe provocar pérdida de perfiles o
progreso.

## 5. Auth

Auth es un servidor especial. Antes de confirmar la autenticación no deben
exponerse completamente perfiles, amigos, parties, escuadrones,
estadísticas ni comandos sociales.

Durante Auth existe únicamente una sesión mínima. Después de autenticar al
jugador, el proxy habilita su identidad global y permite su traslado al
Lobby.

## 6. Keybinds

El sistema administrativo y gráfico de keybinds está implementado en
TheosferaCore.

Responsabilidades futuras:

- comunicación con Theosfera Client;
- sincronización de keybinds;
- detección de capacidades del cliente;
- compatibilidad sin el mod.

La ausencia del mod no debe afectar módulos generales.

## 7. Waypoints

TheosferaCore proporciona la infraestructura:

- `WaypointService`;
- modelos de waypoint;
- registro de proveedores;
- creación, actualización y eliminación;
- limpieza al desconectar;
- API para plugins externos.

Proveedores previstos:

- Lunar Client;
- Theosfera Client;
- fallback visual futuro.

Los plugins de modalidad deciden por qué aparece un waypoint, su ubicación,
texto, color y cuándo debe eliminarse.

## 8. Perfil global

### Comandos

```text
/profile
/profile <player>
```

`/profile` abre el centro personal completo. `/profile <player>` abre una
ficha pública resumida y limitada por privacidad.

### ProfileService

Datos generales:

- identidad;
- rango;
- nivel y puntos;
- estadísticas;
- primera y última conexión;
- tiempo total;
- preferencias;
- modalidad actual;
- historial reciente.

### Menú personal

Secciones persistentes:

- resumen;
- estadísticas;
- progreso;
- escuadrón;
- party;
- amigos;
- jugados recientemente;
- beneficios;
- tienda;
- configuración.

La navegación permanece visible y el contenido central cambia según la
sección elegida.

### Perfil público

Puede mostrar nombre, skin, rango, nivel general, escuadrón, logros
destacados, estado online, modalidad actual y fechas permitidas por la
privacidad.

No debe mostrar IP, servidor físico, datos administrativos, sanciones
internas, configuraciones privadas ni información social restringida.

## 9. Modalidades jugadas recientemente

Se almacenan modalidades lógicas únicas. No se registran Proxy, Auth,
Lobby, servidores internos ni instancias técnicas.

Volver a una modalidad actualiza su posición sin duplicarla.

```text
Antes:
1. Survival
2. Skyblock
3. Prison

Después de entrar a Skyblock:
1. Skyblock
2. Survival
3. Prison
```

Datos previstos:

- UUID del jugador;
- ID de modalidad;
- primera visita;
- última visita;
- sesiones;
- tiempo total.

La actividad se registra después de confirmar la llegada y permanecer un
tiempo mínimo. El menú puede funcionar como acceso rápido hacia una
instancia disponible de la modalidad seleccionada.

## 10. Amigos

Funciones previstas:

- enviar, aceptar y rechazar solicitudes;
- eliminar amistades;
- bloquear jugadores;
- consultar estado;
- mostrar modalidad según privacidad;
- unirse a un amigo cuando sea permitido.

La relación de amistad es permanente.

## 11. Parties

Las parties son grupos temporales globales.

Funciones previstas:

- crear, invitar, aceptar y rechazar;
- abandonar, expulsar y disolver;
- transferir liderazgo;
- viajar hacia el líder;
- activar o desactivar traslado automático.

Comandos conceptuales:

```text
/party warp
/party tpauto on
/party tpauto off
```

Alias principal: `/p`.

### Reconexión

Los miembros conservan su lugar durante un máximo de cinco minutos bajo
el estado `OFFLINE_GRACE`. Si regresan dentro del límite, recuperan su
lugar. Si el tiempo expira, salen de la party.

### Líder desconectado

- conserva liderazgo durante cinco minutos;
- `tpauto` queda pausado;
- si regresa, recupera su estado;
- si expira, el liderazgo pasa al miembro conectado más antiguo;
- si no queda nadie, la party se disuelve;
- `tpauto` se desactiva después de una transferencia automática.

### Seguridad

- no mover jugadores en Auth;
- no omitir permisos o restricciones;
- validar destinos;
- evitar bucles;
- aplicar cooldown;
- validar en el proxy toda instrucción global.

## 12. Escuadrones

Los escuadrones son grupos sociales permanentes.

Rangos iniciales sugeridos:

- Líder;
- Colíder;
- Oficial;
- Miembro;
- Recluta.

Cada rango contiene prioridad, nombre, icono, color y permisos internos.

Funciones:

- crear e invitar;
- aceptar y abandonar;
- ascender y descender;
- expulsar;
- transferir liderazgo;
- disolver;
- administrar configuraciones.

### Menú dinámico

La lista muestra miembros mediante cabezas dinámicas.

- click izquierdo: perfil público;
- click derecho: administración;
- acciones destructivas: confirmación obligatoria.

El menú administrativo puede incluir ascender, descender, expulsar y
transferir liderazgo. Las opciones dependen de la jerarquía del observador
y del objetivo.

El backend debe volver a validar cada acción. Ocultar un botón no reemplaza
la comprobación de permisos.

### Auditoría

Registrar invitaciones, ascensos, descensos, expulsiones, transferencias,
cambios de configuración y disoluciones.

## 13. Progreso global

Sistemas previstos:

- nivel general y puntos generales;
- nivel de misiones y puntos de misiones;
- nivel de logros y puntos de logros.

Los plugins de modalidades entregan progreso mediante una API central.
Las modalidades contienen misiones y logros específicos. TheosferaCore
conserva y presenta el progreso global.

## 14. Comandos esenciales

Comandos iniciales previstos:

- `/spawn`;
- `/warp`;
- `/home`;
- `/tp`;
- `/tpa`;
- `/fly`;
- `/gamemode`.

Candidatos generales posteriores:

- `/back`;
- `/heal`;
- `/feed`;
- `/seen`.

Cada comando pertenece a un módulo configurable, dispone de permisos
separados, respeta restricciones de modalidad y puede desactivarse.

## 15. Funciones reservadas para Skyblock

No pertenecen al módulo general de comandos esenciales:

- `/workbench` o `/craft`;
- `/enderchest`;
- `/anvil`;
- `/enchant`;
- `/pv` o `/playervault`.

TheosferaCore proporciona el motor visual. TheosferaSkyblockAddons
implementa las mecánicas e inventarios.

### Almacenamiento avanzado

Ender chests y player vaults pueden variar según rango.

Requisitos críticos:

- bloqueo de apertura simultánea;
- guardado al cerrar y desconectar;
- protección ante cambio de servidor;
- recuperación ante crash;
- prevención de duplicación;
- versionado de datos;
- impedir que sesiones antiguas sobrescriban datos nuevos;
- auditoría de operaciones sensibles.

La integridad de ítems tiene prioridad sobre la estética.

## 16. Orden de implementación

### Fase 1 — Fundación de red

- crear TheosferaProxy;
- identidad global;
- mapa de servidores y modalidades;
- comunicación Core–Proxy;
- presencia;
- base de datos;
- Redis;
- protección especial de Auth.

### Fase 2 — Perfil básico

- `ProfileService`;
- `/profile` y `/profile <player>`;
- datos generales;
- privacidad;
- modalidades recientes.

### Fase 3 — Amigos

- solicitudes;
- relaciones;
- bloqueos;
- presencia;
- acceso social.

### Fase 4 — Party

- gestión básica;
- reconexión de cinco minutos;
- `party warp`;
- `tpauto`;
- movimiento global.

### Fase 5 — Escuadrones

- miembros;
- rangos y permisos;
- menú dinámico;
- administración;
- auditoría.

### Fase 6 — Progreso global

- niveles y puntos;
- API para modalidades;
- menús de progreso.

### Fase 7 — Waypoints

- proveedores;
- Lunar Client;
- API de objetivos;
- integración con modalidades.

### Fase 8 — Comandos esenciales

- módulos configurables;
- permisos;
- restricciones por modalidad.

## 17. Principios arquitectónicos

- no crear god classes;
- preferir inyección por constructor;
- separar Paper, Velocity y lógica compartida;
- validar en el componente autoritativo;
- no confiar en datos enviados por clientes;
- conservar compatibilidad sin integraciones opcionales;
- priorizar seguridad e integridad;
- evitar trabajo pesado en el hilo principal;
- documentar contratos entre plugins;
- mantener módulos desactivables.

## 18. Siguiente paso

Después de aprobar este roadmap:

1. fusionar la documentación;
2. crear `TheosferaProxy`;
3. definir el contrato Core–Proxy;
4. diseñar almacenamiento central;
5. implementar presencia global antes de los sistemas sociales.

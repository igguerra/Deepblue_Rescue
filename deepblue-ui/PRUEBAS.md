# Laboratorio-DeepBlue_Rescue_React

ISABELLA GUERRA – 2024114058, 
VALERIA GÁMEZ – 2024214029

## Cómo ejecutar

1. Base de datos: `docker run -d --name deepblue-db -e POSTGRES_DB=deepblue -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:17`
2. Backend (raíz del repo): `mvn spring-boot:run`
3. Frontend (`deepblue-ui/`): `pnpm install` y `pnpm dev`

## Pruebas

**Ramas:**
- `feature/ui-casos`: Pasos 1, 2, 3 y 5 (base, lista de casos y cambio de estado)
- `feature/ui-tratamientos`: Pasos 4 y 6 (detalle del animal y registro de tratamientos)

**Datos de prueba:** casos RC-001 a RC-005, animales AN-001 a AN-005, especialistas SP-001 y SP-002.

---

## Checkpoint 4 — Detalle del animal

| Prueba | Resultado esperado | Resultado observado |
|---|---|---|
| Seleccionar RC-001 | Se muestran animal, elegibilidad y "Sin tratamientos registrados" | Se muestran los datos de AN-001 (Tortuga caguama, *Caretta caretta*, FEMALE), la elegibilidad y "Sin tratamientos registrados" |
| Seleccionar RC-004 | La tabla muestra los tratamientos del animal | Se muestra el tratamiento existente: 2026-09-22T10:00:00, OBSERVATION, SP-002, "Control de peso y alimentacion" |
| Volver de RC-002 a RC-001 | No se repite la consulta (caché) | Los datos aparecen al instante sin "Cargando...", porque vienen de la caché. En *Network* sí se repiten las peticiones de AN-001 (animal, treatments y treatment-eligibility) |

**Observación sobre la caché:** TanStack Query muestra los datos guardados y a la vez los vuelve a pedir en segundo plano. Esto ocurre porque el `staleTime` por defecto es 0, así que los datos se consideran desactualizados de inmediato. Con un `staleTime` mayor (por ejemplo 30 segundos) las peticiones no se repetirían.

---

## Paso 5 — Cambio de estado

| Prueba | Resultado esperado | Resultado observado |
|---|---|---|
| RC-001: `ADMITTED → UNDER_EVALUATION` | **200**: el caso sale de la lista ADMITTED sin recargar y aparece en UNDER_EVALUATION | [completar] |
| RC-002: `ADMITTED → RELEASED` | **409**: mensaje en rojo con la regla de negocio y la app sigue funcionando | [Error 409: Invalid status transition from ADMITTED to RELEASED] |

---

## Paso 6 — Registro de tratamiento

| Prueba | Resultado esperado | Resultado observado |
|---|---|---|
| Descripción corta ("hola") | **400** con `details.description` | **400** Request validation failed: *description: Description must contain between 10 and 500 characters* |
| `specialistCode` vacío | **400** con `details.specialistCode` | **400** Request validation failed: *specialistCode: Specialist code is required* |
| Fecha en el futuro (01/01/2027) | **400** "Treatment date cannot be in the future" | **400** Request validation failed: *performedAt: Treatment date cannot be in the future* |
| Fecha vacía | **400** ("Required..." o JSON inválido, según el handler) | **400** Request validation failed: *performedAt: Treatment date is required* |
| `specialistCode` = `SP-999` | **404** | **404**: *Specialist not found: SP-999* |
| Datos válidos (SP-001, 25/10/2026 11:11 p. m., WOUND_CARE, "limpieza de herida en aleta") | **201**: aparece en la tabla sin recargar y el formulario se limpia | **201** "Tratamiento registrado ✔". Aparece en la tabla (2026-10-25T23:11:00, WOUND_CARE, SP-001, limpieza de herida en aleta), el formulario se limpia y se vuelven a consultar tratamientos y elegibilidad |

**Observaciones:**
- **Errores por campo:** en un primer intento con descripción corta y fecha futura aparecieron los dos errores a la vez (`performedAt` y `description`). Esto confirma que los errores 400 se muestran por campo.
- **Fecha vacía:** el campo vacío llega como nulo y lo rechaza la validación del DTO. No se produce un error de JSON inválido.
- **SP-999:** no muestra detalles por campo porque no es un error de validación de formato, sino de negocio: el especialista no existe en la base de datos.
- **Refresco tras registrar:** la invalidación con la clave `['animal', animalCode]` refresca tratamientos y elegibilidad del animal.

---

## Observación adicional

El animal AN-001 (caso RC-001, estado ADMITTED) aparece con "Puede recibir tratamiento: No", pero el backend permitió registrar el tratamiento con 201. Es una posible inconsistencia del backend: el endpoint `POST /api/treatments` no verifica la elegibilidad, o la regla de elegibilidad mide un criterio distinto. Queda pendiente revisarlo en `TreatmentService`.

---

## Respuestas "Para pensar"

### Checkpoint 1 — ¿Por qué la URL del fetch es `/api/...` y no `http://localhost:8080/api/...`?
Porque así la petición va al mismo origen del frontend (`localhost:5173`) y el proxy configurado en `vite.config.js` la reenvía al backend en el puerto 8080. Para el navegador no es una petición a otro origen, por lo que no se aplica CORS y no hay que modificar el backend. Además, la dirección del backend no queda escrita en el código de los componentes.

### Checkpoint 2 — ¿Por qué `request` lanza el error en lugar de devolverlo?
Porque TanStack Query determina `isError` según si la promesa se rechaza. Si el error se devolviera como valor normal, la consulta se consideraría exitosa y el error quedaría guardado en `data`. Al lanzar un `ApiError`, el componente recibe `isError = true` y el objeto `error` con `status`, `message` y `details` del `ErrorResponse` del backend.

### Checkpoint 3 — ¿Qué pasaría si `queryKey` fuera solo `['rescue-cases']` sin `status`?
Todos los estados compartirían una sola entrada de caché. Al cambiar el filtro, TanStack Query no detectaría un cambio de clave y no volvería a consultar, así que se seguirían mostrando los casos del estado anterior. Incluir `status` en la clave hace que cada estado tenga su propia caché y que cambiar el filtro dispare la consulta correcta.

### Checkpoint 4 — ¿Para qué sirve `enabled`? ¿Qué pasaría sin él si `animalCode` fuera `undefined`?
`enabled` controla si la consulta se ejecuta. Con `enabled: Boolean(animalCode)` las tres consultas del animal solo se lanzan cuando existe un código. Sin él, si `animalCode` fuera `undefined` se harían peticiones a `/api/animals/undefined`, que terminarían en errores 404 innecesarios y en entradas de caché inválidas.

### Checkpoint 5 — ¿Qué hace `invalidateQueries({ queryKey: ['rescue-cases'] })` y por qué afecta a todas las listas?
Marca como desactualizadas todas las consultas cuya clave empieza por `'rescue-cases'`, porque la coincidencia es por prefijo. Las consultas visibles se vuelven a pedir de inmediato y las demás se refrescan cuando se vuelvan a usar. Afecta a todas las listas por estado porque un cambio de estado saca el caso de una lista y lo agrega a otra, así que más de una lista queda desactualizada.

### Checkpoint 6 — ¿Por qué `useReducer` y no un `useState` por campo? ¿Por qué `invalidateQueries` usa `['animal', animalCode]` sin sufijos?
`useReducer` concentra todo el estado del formulario en un objeto y en una sola función que define cómo cambia (`field` y `reset`). Con un único manejador (`handleChange`) se actualiza cualquier campo usando su atributo `name`, y limpiar el formulario es una sola acción. Con `useState` habría cuatro estados y cuatro manejadores separados.

La clave `['animal', animalCode]` sin sufijos coincide por prefijo con las tres consultas del animal: datos, tratamientos y elegibilidad. Así, tras registrar un tratamiento se refrescan las tres, ya que el nuevo tratamiento cambia la lista y puede cambiar la elegibilidad.

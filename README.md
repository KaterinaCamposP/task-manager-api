# Task Manager API

API REST para gestión de tareas personales con autenticación JWT.

**Stack:** Java 17 · Spring Boot 4.0.7 · PostgreSQL 16 · Flyway · Spring Security · JJWT 0.12.3

---

## Decisiones técnicas y desviaciones del backlog

## Sprint 1
### Spring Boot 4.0.7 en vez de 3.3.x
El backlog especifica Spring Boot 3.3. Al momento de iniciar el proyecto
(julio 2026), start.spring.io solo ofrecía versiones 4.x estables.
Se usó 4.0.7 — la API es prácticamente idéntica pero requirió ajustes:
- `@Where` deprecado → reemplazado por `@SQLRestriction` (Hibernate 6.x)
- `DaoAuthenticationProvider()` sin constructor vacío → se pasa `UserDetailsService` directo
- `@NonNull` de `org.springframework.lang` deprecated en Spring 7.0

### spring-dotenv para variables de entorno
El backlog no especifica cómo gestionar variables de entorno locales.
Se usó `springboot4-dotenv 5.0.1` (vía BOM `spring-dotenv-bom`) para
leer el archivo `.env` automáticamente. La versión `spring-dotenv 4.0.0`
es incompatible con Spring Boot 4.x.

### Dependencia circular en SecurityConfig
`SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig`
Solución: `JwtAuthenticationFilter` se recibe como parámetro del método
`securityFilterChain()` en vez de en el constructor, rompiendo el ciclo.

### JWT usa email como subject
El backlog no especifica qué campo usar como identificador en el token.
Se decidió usar el email porque es único, inmutable, y es el campo
usado para login — evita inconsistencias entre login y validación del token.

### UserRepository — métodos adicionales
Se agregaron `findByUsername` y `existsByUsername` durante el desarrollo
para explorar autenticación por username. No se usan actualmente pero
se mantienen para uso futuro.

### CORS habilitado para frontend
Se configuró CORS en `SecurityConfig` para permitir requests desde
`http://localhost:5173` (Vite dev server). Métodos permitidos:
GET, POST, PUT, DELETE, PATCH. Para producción se debe actualizar
con la URL de Vercel o el dominio del frontend.

## Sprint 2
### UserDetailsService — email como username interno
Se detectó que JwtAuthenticationFilter fallaba (403 en /api/tasks) porque
el JWT usa el email como subject, pero User.getUsername() devuelve el
username elegido por el usuario, no el email. Se corrigió userDetailsService()
en SecurityConfig para envolver la entidad en un UserDetails de Spring Security
cuyo getUsername() devuelve el email, alineándolo con el subject del token.

### Soft delete y borrado físico en tests
@SQLDelete intercepta cualquier borrado de Task (incluido deleteAll()) y lo
convierte siempre en UPDATE deleted_at = NOW(), nunca en DELETE real. Para
limpiar la BD de test antes de cada @BeforeEach, se agregó un método con
@Query(nativeQuery = true) + @Modifying + @Transactional en TaskRepository
que ejecuta un DELETE SQL puro, evitando el conflicto de FK con users.

## Sprint 3
### Redis vía WSL2 en desarrollo local
El backlog no especifica cómo instalar Redis. Como Redis no tiene soporte
nativo oficial en Windows, se instaló dentro de WSL2 (Ubuntu) en vez de
usar Docker o una alternativa como Memurai. La app Spring Boot corre en
Windows y se conecta a `localhost:6379`, que WSL2 expone automáticamente
al host de Windows sin configuración adicional.

### Blacklist de tokens con TTL automático
Al hacer logout, el access token se guarda en Redis con `SET` + expiración
igual al tiempo de vida restante del token (`getRemainingExpiration()` en
JwtService). Redis borra la clave automáticamente cuando expira, sin
necesidad de un job de limpieza. JwtAuthenticationFilter consulta la
blacklist antes de validar cualquier token.

### Paginación como objeto Page en vez de PagedModel
GET /api/tasks devuelve directamente `Page<TaskResponse>`. Spring emite un
warning recomendando envolver la respuesta en `PagedModel` (vía
`@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`) para mayor
estabilidad del JSON a futuro. Se decidió no aplicarlo por ahora ya que
`Page` funciona correctamente y el backlog no exige ese nivel de refinamiento;
queda como mejora opcional.

### Rutas públicas de Swagger en SecurityConfig
Swagger UI requirió agregar explícitamente `/swagger-ui.html`, `/swagger-ui/**`,
`/v3/api-docs/**` y `/v3/api-docs.yaml` como rutas públicas — el patrón
`/swagger-ui/**` por sí solo no cubre la URL exacta `/swagger-ui.html`.

### Índice user_id + status creado antes de tiempo (Sprint 1)
El backlog pide este índice explícitamente en Sprint 3 ("optimizar filtrado
por estado"), pero ya existía desde la migración V1__init.sql de Sprint 1.
No fue una anticipación planificada — el prompt de esa migración pidió
"índices básicos" de forma genérica y el resultado generado incluyó tanto
`idx_task_user_id` como `idx_task_user_id_status` sin que Sprint 1 lo
exigiera puntualmente. Se documenta aquí para mantener trazabilidad honesta
entre lo planificado y lo que realmente ocurrió.
---

## Configuración local

### Requisitos
- Java 17
- PostgreSQL 16
- Maven
- Redis (via WSL2/Ubuntu, Docker, o Memurai)

### Variables de entorno
Crear archivo `.env` en la raíz del proyecto:
JWT_SECRET=tu_secret_generado_con_crypto
DB_PASSWORD=tu_password_postgresql

Generar JWT_SECRET seguro:
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"

### Base de datos
CREATE DATABASE task_manager_db;

### Ejecutar
./mvnw spring-boot:run

La app levanta en http://localhost:8080
Flyway crea las tablas automáticamente al primer arranque.

---

## Endpoints Sprint 1 — Auth

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| POST | /api/auth/register | No | Registro de usuario |
| POST | /api/auth/login | No | Login, retorna tokens |
| POST | /api/auth/refresh | Bearer | Renovar access token |
| GET | /api/auth/profile | Bearer | Perfil del usuario |

### Ejemplo registro
curl -X POST http://localhost:8080/api/auth/register 
-H "Content-Type: application/json" 
-d '{"username":"katerina","email":"katerina@test.com","password":"123456"}'

### Ejemplo login
curl -X POST http://localhost:8080/api/auth/login 
-H "Content-Type: application/json" 
-d '{"email":"katerina@test.com","password":"123456"}'

---
## Endpoints Sprint 2 — Tasks

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| POST | /api/tasks | Bearer | Crear tarea (title obligatorio, status PENDING por defecto) |
| GET | /api/tasks | Bearer | Listar tareas del usuario autenticado |
| GET | /api/tasks/{id} | Bearer | Obtener una tarea por ID (valida propiedad) |
| PUT | /api/tasks/{id} | Bearer | Actualizar title, description y status |
| DELETE | /api/tasks/{id} | Bearer | Soft delete (setea deleted_at) |
| PATCH | /api/tasks/{id}/status | Bearer | Alterna status entre PENDING y COMPLETED |

### Ejemplo crear tarea
curl -X POST http://localhost:8080/api/tasks \
-H "Authorization: Bearer {tu_access_token}" \
-H "Content-Type: application/json" \
-d '{"title":"Primera tarea","description":"Probando el CRUD"}'

---
## Endpoints Sprint 3 — Paginación, filtros y logout

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| GET | /api/tasks?page=&size=&sort= | Bearer | Lista paginada y ordenable |
| GET | /api/tasks?status=PENDING | Bearer | Filtra por estado |
| POST | /api/auth/logout | Bearer | Invalida el token actual (blacklist Redis) |

### Ejemplo paginación + orden + filtro
curl http://localhost:8080/api/tasks?page=0&size=5&sort=title,desc&status=PENDING \
-H "Authorization: Bearer {tu_access_token}"

### Documentación interactiva (Swagger)
http://localhost:8080/swagger-ui/index.html

---
## Estado del proyecto

### Sprint 1 — Backend Auth completado
- [x] Setup Spring Boot + dependencias
- [x] Entidades JPA (User, Task) con auditoría y soft delete
- [x] Flyway V1 — migración inicial
- [x] Repositories (UserRepository, TaskRepository)
- [x] DTOs (Register, Login, AuthResponse, UserProfile, Task)
- [x] JwtService — generación y validación de tokens
- [x] JwtAuthenticationFilter — interceptor de requests
- [x] SecurityConfig — configuración Spring Security
- [x] AuthController — register, login, refresh, profile
- [x] GlobalExceptionHandler

### Sprint 1 — Listo!
- [x] QA Tests (register, login, refresh, profile)
- [x] Frontend UX (login, registro, dashboard, modal tarea)

### Sprint 2 — Backend y QA completados
- [x] POST /api/tasks
- [x] GET /api/tasks
- [x] GET /api/tasks/{id}
- [x] PUT /api/tasks/{id}
- [x] DELETE /api/tasks/{id}
- [x] PATCH /api/tasks/{id}/status
- [x] Validaciones @Valid en TaskRequest
- [x] Auditoría automática (@CreatedDate/@LastModifiedDate)
- [x] TaskMapper con MapStruct
- [x] Soft delete con @SQLDelete + @SQLRestriction
- [x] QA Tests (CRUD completo, validaciones, seguridad, soft delete)

### Sprint 2 — Frontend
- [x] Frontend UX: conectar dashboard al CRUD real
- [x] Frontend UX: editar/eliminar tarea desde UI
- [x] Frontend UX: toggle de estado
- [ ] Frontend UX: feedback visual (loading, toasts) — movido a Sprint 4

### Sprint 3 — Backend completado
- [x] GET /api/tasks con paginación (Pageable + metadata)
- [x] GET /api/tasks con ordenamiento (sort=campo,asc|desc)
- [x] GET /api/tasks con filtrado por status
- [x] POST /api/auth/logout con blacklist Redis (TTL automático)
- [x] Configurar Redis (WSL2 + spring-boot-starter-data-redis)
- [x] CORS (ya cubierto desde Sprint 1)
- [x] OpenAPI/Swagger con @Tag y @Operation
- [x] Variables de entorno REDIS_HOST/REDIS_PORT
- [x] Índice user_id + status (verificar si ya existe de Sprint 1)

### Sprint 3 — Pendiente
- [ ] QA Tests (paginación, ordenamiento, filtrado, seguridad, coverage JaCoCo)
- [ ] Frontend: paginación UI, ordenamiento, filtros, dashboard con tarjetas resumen, responsive, logout

---

## Estructura del proyecto
```text
src/main/java/com/katerinacampos/task_manager/
├── config/
│   └── RedisConfig.java
├── controller/
│   ├── AuthController.java
│   └── TaskController.java
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── TaskRequest.java
│   ├── TaskResponse.java
│   └── UserProfileResponse.java
├── exception/
│   └── GlobalExceptionHandler.java
├── mapper/
│   └── TaskMapper.java
├── model/
│   ├── Task.java
│   ├── TaskStatus.java
│   └── User.java
├── repository/
│   ├── TaskRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   └── SecurityConfig.java
├── service/
│   ├── TaskService.java
│   └── TokenBlacklistService.java
└── TaskManagerApplication.java

src/main/resources/db/migration/
├── V1__init.sql
└── V2__add_index_created_at.sql

src/test/java/com/katerinacampos/task_manager/
├── controller/
│   ├── AuthControllerTest.java
│   └── TaskControllerTest.java
└── TaskManagerApplicationTests.java
```
## Testing

### Ejecutar tests

### Tests incluidos — Sprint 1
Los tests de integración usan `@SpringBootTest` con MockMvc y se ejecutan contra la base de datos local.

**Registro:**
- Registro exitoso retorna accessToken, refreshToken y tokenType Bearer
- Email duplicado retorna 400
- Password menor a 6 caracteres retorna 400
- Email con formato inválido retorna 400
- Username vacío retorna 400

**Login:**
- Login exitoso retorna accessToken y tokenType Bearer
- Credenciales incorrectas retorna 4xx

**Profile:**
- Sin token retorna 401
- Con token válido retorna email y username del usuario

### Tests incluidos — Sprint 2

**CRUD de tareas:**
- Crear, listar, obtener por ID, actualizar, eliminar y cambiar estado (flujo completo)
- Título vacío o menor a 3 caracteres retorna 400
- ID inexistente retorna error
- Sin token retorna 401/403

**Soft delete:**
- Tarea eliminada no aparece en el listado (filtrada por @SQLRestriction)

### Notas técnicas
- `@AutoConfigureMockMvc` no existe en Spring Boot 4.x — se construye MockMvc manualmente con `MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()`
- Limpieza de BD antes de cada test con `userRepository.findByEmail().ifPresent(delete)`
- Tests corren contra PostgreSQL local, no H2 en memoria
- `@SQLDelete` intercepta cualquier borrado de `Task` (incluido `deleteAll()`) y lo convierte en `UPDATE deleted_at = NOW()`. Para limpiar la BD en tests, `TaskRepository` usa un `@Query(nativeQuery = true)` con `@Modifying @Transactional` que ejecuta un `DELETE` SQL puro
# 📋 TASK MANAGER API - BACKLOG Y ESTIMACIONES

## 🎯 VISIÓN GENERAL DEL PROYECTO

|Concepto|Detalle|
|---|---|
|**Proyecto**|Task Manager API con Java Spring|
|**Duración Total**|6 días hábiles (48 horas)|
|**Equipo**|1 Desarrolladora (Backend) + IA (Frontend)|
|**Stack**|Java 17, Spring Boot 3.x, JWT, PostgreSQL, Redis, Docker|
|**Total Tareas**|77 tareas|

---

## 🗓️ CALENDARIO DE SPRINTS

text

┌─────────────────────────────────────────────────────────────────┐
│  DÍA 1-2    │  DÍA 2-3    │  DÍA 3-4    │  DÍA 4-5           │
│  SPRINT 1   │  SPRINT 2   │  SPRINT 3   │  SPRINT 4           │
│  16 HORAS   │  12 HORAS   │  12 HORAS   │  8 HORAS            │
├─────────────┼─────────────┼─────────────┼─────────────────────┤
│  Setup      │  CRUD       │  Filtros    │  Pulir             │
│  Auth       │  Tareas     │  Paginación │  Docker            │
│  JWT        │  Soft Delete│  Redis      │  Deploy            │
│  BD         │  Validación │  Swagger    │  Documentación     │
└─────────────────────────────────────────────────────────────────┘

---

## 📊 RESUMEN DE CARGA POR ÁREA

text

┌─────────────────────────────────────────────────────────────────┐
│  🎨 UX/UI (IA)   ████████████████░░░░░░░░  19 tareas (25%)   │
│  ☕ Backend Java  ████████████████████████  29 tareas (37%)   │
│  🗄️ Base Datos   ████████████░░░░░░░░░░░░  12 tareas (16%)   │
│  🧪 QA/Testing   ████████████████░░░░░░░░  17 tareas (22%)   │
└─────────────────────────────────────────────────────────────────┘

---

# 🚀 SPRINT 1 - SETUP Y AUTENTICACIÓN

## 📅 2 DÍAS (16 HORAS)

### 🎯 Objetivo del Sprint

> Configurar Spring Boot 3.3, implementar JWT con access/refresh tokens, bcrypt y endpoints de registro/login.

---

### 🎨 UX / FRONTEND (IA) - 4 TAREAS (~3 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 45min|**Pantalla Login**|Formulario email/password con validación y llamada a `/api/auth/login`|
|🟢 45min|**Pantalla Registro**|Formulario username/email/password con validación y llamada a `/api/auth/register`|
|🟡 1h|**Dashboard**|Lista de tareas del usuario con encabezado y botón "Crear tarea"|
|🟢 30min|**Modal Nueva Tarea**|Formulario con título (obligatorio), descripción y estado inicial PENDING|

**⏱️ Total UX: ~3 horas**

---

### ☕ BACKEND (JAVA) - 9 TAREAS (~9 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🔴 1h|**Configurar Spring Boot**|Setup con Security 6, JPA, PostgreSQL, JJWT, Lombok y Flyway|
|🟡 45min|**SecurityConfig**|Configurar filtros, deshabilitar CSRF, permitir `/api/auth/**`|
|🔴 1.5h|**JwtService**|Generación y validación de JWT, extracción de claims y usuario|
|🔴 1h|**JwtAuthenticationFilter**|Interceptar peticiones, validar token del header Authorization|
|🔴 1h|**POST /api/auth/register**|Validar email único, hashear password con BCrypt, guardar usuario|
|🟡 45min|**POST /api/auth/login**|Validar credenciales, generar access token (1h) y refresh token (7d)|
|🟡 45min|**POST /api/auth/refresh**|Recibir refresh token y devolver nuevo access token válido|
|🟢 30min|**GET /api/auth/profile**|Retornar username, email y created_at del usuario autenticado|
|🔴 1h|**GlobalExceptionHandler**|Manejar MethodArgumentNotValid, AccessDenied, EntityNotFound|

**⏱️ Total Backend: ~9 horas**

---

### 🗄️ BASE DE DATOS - 7 TAREAS (~3 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 30min|**Entidad User**|id, username, email, password, created_at, updated_at con JPA|
|🟢 30min|**Entidad Task**|id, title, description, status, user_id, created_at, updated_at, deleted_at|
|🟢 20min|**UserRepository**|findById, findByEmail, existsByEmail con Spring Data JPA|
|🟢 20min|**TaskRepository**|findByUser_Id, findByIdAndUser_Id, @Query para soft delete|
|🟢 30min|**DTOs**|RegisterRequest, LoginRequest, AuthResponse, UserProfileResponse|
|🟢 30min|**PostgreSQL**|Configurar application.yml, crear base de datos local|
|🟢 20min|**Flyway V1**|Crear tablas users y tasks con UNIQUE email, FK, índices básicos|

**⏱️ Total Base de Datos: ~3 horas**

---

### 🧪 QA/TESTING - 4 TAREAS (~1 HORA)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 15min|**Test registro**|email duplicado, password corto, email inválido, username vacío|
|🟢 15min|**Test login**|credenciales correctas/incorrectas, formato del token|
|🟢 15min|**Test refresh**|token válido, expirado, inválido o malformado|
|🟢 15min|**Test profile**|acceso sin token, con token válido, datos correctos|

**⏱️ Total QA: ~1 hora**

---

### 📊 SPRINT 1 - RESUMEN

|Área|Tareas|Horas|
|---|---|---|
|🎨 UX/UI (IA)|4|3h|
|☕ Backend Java|9|9h|
|🗄️ Base de Datos|7|3h|
|🧪 QA/Testing|4|1h|
|**TOTAL**|**24**|**16h**|

---

# 🚀 SPRINT 2 - CRUD DE TAREAS

## 📅 1.5 DÍAS (12 HORAS)

### 🎯 Objetivo del Sprint

> CRUD completo de tareas con soft delete, auditoría automática, validaciones de entrada y manejo de errores.

---

### 🎨 UX / FRONTEND (IA) - 6 TAREAS (~3.5 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟡 45min|**GET /api/tasks**|Mostrar lista de tareas con estado y botones de acción|
|🟢 30min|**POST /api/tasks**|Enviar formulario para crear tarea y actualizar lista|
|🟡 45min|**PUT /api/tasks/{id}**|Editar título/descripción y estado en modal|
|🟢 30min|**DELETE /api/tasks/{id}**|Eliminar con confirmación, soft delete visual (tachado en gris)|
|🟢 30min|**PATCH /tasks/{id}/status**|Cambiar estado con toggle (checkbox o botón)|
|🟢 30min|**Feedback visual**|Loading spinners, toast success/error, confirmación|

**⏱️ Total UX: ~3.5 horas**

---

### ☕ BACKEND (JAVA) - 8 TAREAS (~6 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🔴 1h|**POST /api/tasks**|title (obligatorio min 3), description (opcional), status PENDING|
|🟡 45min|**GET /api/tasks**|Listar tareas del usuario (excluyendo deleted_at != null)|
|🟢 30min|**GET /api/tasks/{id}**|Obtener tarea por ID validando propiedad (deleted_at lanza 404)|
|🟡 45min|**PUT /api/tasks/{id}**|Actualizar title, description, status validando existencia|
|🟢 30min|**DELETE /api/tasks/{id}**|Soft delete seteando deleted_at = timestamp actual|
|🟢 30min|**PATCH /tasks/{id}/status**|Cambiar estado entre PENDING y COMPLETED|
|🟡 45min|**Validaciones @Valid**|@NotBlank y @Size en title, @Email, @Size min=6 en password|
|🟢 30min|**Auditoría automática**|@CreatedDate y @LastModifiedDate en User y Task|

**⏱️ Total Backend: ~6 horas**

---

### 🗄️ BASE DE DATOS - 2 TAREAS (~1 HORA)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 30min|**Soft delete en Task**|@SQLDelete y @Where para filtrar automáticamente|
|🟢 30min|**TaskMapper**|Convertir Task a TaskResponseDTO y TaskRequest a Task|

**⏱️ Total Base de Datos: ~1 hora**

---

### 🧪 QA/TESTING - 4 TAREAS (~1.5 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 30min|**Test CRUD completo**|Crear, listar, obtener, actualizar, eliminar y cambiar estado|
|🟢 30min|**Test integración**|GET, GET/{id}, POST, PUT, DELETE y PATCH de tareas|
|🟢 20min|**Test validaciones**|title vacío, corto, status inválido, ID inexistente|
|🟢 15min|**Test soft delete**|Verificar que no aparece en listados pero existe en BD|

**⏱️ Total QA: ~1.5 horas**

---

### 📊 SPRINT 2 - RESUMEN

|Área|Tareas|Horas|
|---|---|---|
|🎨 UX/UI (IA)|6|3.5h|
|☕ Backend Java|8|6h|
|🗄️ Base de Datos|2|1h|
|🧪 QA/Testing|4|1.5h|
|**TOTAL**|**20**|**12h**|

---

# 🚀 SPRINT 3 - FILTRADO Y MEJORAS

## 📅 1.5 DÍAS (12 HORAS)

### 🎯 Objetivo del Sprint

> Paginación, ordenamiento, filtros, CORS, Swagger, logout con Redis blacklist y mejoras de experiencia.

---

### 🎨 UX / FRONTEND (IA) - 6 TAREAS (~3.5 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🔴 1h|**Paginación UI**|Navegación `<< < 1 2 3 ... > >>` con page y size|
|🟢 30min|**Ordenamiento UI**|Selector para ordenar por created_at, title, status|
|🟢 30min|**Filtros UI**|Botones/select para filtrar por PENDING, COMPLETED o ALL|
|🟡 45min|**Dashboard**|Tarjetas con total, completadas y pendientes|
|🟢 30min|**UI Responsive**|Adaptar a móvil, tablet y escritorio con Grid/Flexbox|
|🟢 20min|**Logout**|Botón que elimina token local y redirige a login|

**⏱️ Total UX: ~3.5 horas**

---

### ☕ BACKEND (JAVA) - 8 TAREAS (~5.5 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🔴 1h|**GET /tasks con paginación**|Pageable y Page<TaskResponseDTO> con metadata|
|🟢 30min|**GET /tasks con ordenamiento**|Soportar `sort=created_at,asc` y `sort=title,desc`|
|🟢 30min|**GET /tasks con filtrado**|Soportar `status=PENDING` o `status=COMPLETED`|
|🔴 1h|**POST /auth/logout**|Invalidar token en blacklist Redis con TTL|
|🟡 45min|**Configurar Redis**|Connection factory, serialization y template|
|🟢 15min|**CORS**|Permitir peticiones desde frontend con credentials|
|🔴 1h|**OpenAPI/Swagger**|springdoc-openapi con anotaciones @Operation|
|🟢 15min|**Variables de entorno**|Placeholders para JWT_SECRET, DATABASE_URL, REDIS_URL|

**⏱️ Total Backend: ~5.5 horas**

---

### 🗄️ BASE DE DATOS - 2 TAREAS (~1 HORA)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 30min|**Índice user_id + created_at**|Optimizar paginación y ordenamiento|
|🟢 30min|**Índice user_id + status**|Optimizar filtrado por estado|

**⏱️ Total Base de Datos: ~1 hora**

---

### 🧪 QA/TESTING - 5 TAREAS (~2 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 25min|**Test paginación**|Verificar page, size, totalElements, totalPages|
|🟢 20min|**Test ordenamiento**|Ordenar por created_at ASC/DESC y title ASC/DESC|
|🟢 20min|**Test filtrado**|Filtrar por status PENDING y COMPLETED|
|🟢 30min|**Test seguridad**|401 sin token, 403 con token inválido o revocado|
|🟢 30min|**Test coverage**|Asegurar cobertura >= 70% con JaCoCo|

**⏱️ Total QA: ~2 horas**

---

### 📊 SPRINT 3 - RESUMEN

|Área|Tareas|Horas|
|---|---|---|
|🎨 UX/UI (IA)|6|3.5h|
|☕ Backend Java|8|5.5h|
|🗄️ Base de Datos|2|1h|
|🧪 QA/Testing|5|2h|
|**TOTAL**|**21**|**12h**|

---

# 🚀 SPRINT 4 - FINALIZACIÓN Y DESPLIEGUE

## 📅 1 DÍA (8 HORAS)

### 🎯 Objetivo del Sprint

> Pulir UI/UX, Dockerizar, desplegar en Render/Railway con PostgreSQL y documentación completa.

---

### 🎨 UX / FRONTEND (IA) - 3 TAREAS (~2 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🔴 1h|**Pulir UI/UX**|Colores, tipografía, espaciados, animaciones, accesibilidad|
|🟢 30min|**Feedback visual**|Snackbars/Toasts, skeleton loading, botones con estado|
|🟢 30min|**Tests integración frontend**|Flujo completo registro → login → CRUD tareas|

**⏱️ Total UX: ~2 horas**

---

### ☕ BACKEND (JAVA) - 4 TAREAS (~3.5 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🔴 1h|**Dockerfile multi-stage**|Build JAR con Maven, OpenJDK 17, exponer puerto 8080|
|🔴 1h|**docker-compose.yml**|Orquestar app + PostgreSQL + Redis para entorno local|
|🔴 1h|**Desplegar en Render/Railway**|Servicio web, variables de entorno y PostgreSQL|
|🟢 30min|**Rate Limiting (opcional)**|Bucket4j para limitar 100 req/min por IP|

**⏱️ Total Backend: ~3.5 horas**

---

### 🗄️ BASE DE DATOS - 1 TAREA (~1 HORA)

|⏱️|Tarea|Detalle|
|---|---|---|
|🔴 1h|**BD producción**|Crear instancia PostgreSQL en Render/Railway, migraciones|

**⏱️ Total Base de Datos: ~1 hora**

---

### 🧪 QA/TESTING - 4 TAREAS (~1.5 HORAS)

|⏱️|Tarea|Detalle|
|---|---|---|
|🟢 30min|**Tests E2E**|Automatizar con Newman (Postman CLI) todo el flujo|
|🟢 20min|**Validación producción**|Probar endpoints en URL desplegada|
|🟢 20min|**Colección Postman**|Exportar con todos los endpoints, ejemplos y variables|
|🟢 30min|**README.md**|Instalación, configuración, variables, tests y deploy|

**⏱️ Total QA: ~1.5 horas**

---

### 📊 SPRINT 4 - RESUMEN

|Área|Tareas|Horas|
|---|---|---|
|🎨 UX/UI (IA)|3|2h|
|☕ Backend Java|4|3.5h|
|🗄️ Base de Datos|1|1h|
|🧪 QA/Testing|4|1.5h|
|**TOTAL**|**12**|**8h**|

---

# 📊 TABLA RESUMEN COMPLETA

text

┌─────────────┬──────┬───────┬────────┬─────────┬──────┬───────────────┐
│  SPRINT     │ DÍAS │ HORAS │ UX/IA  │ BACKEND │  BD  │      QA       │
├─────────────┼──────┼───────┼────────┼─────────┼──────┼───────────────┤
│  Sprint 1   │  2   │  16h  │  4 (3h)│ 9 (9h)  │7 (3h)│  4 (1h)       │
│  Sprint 2   │ 1.5  │  12h  │  6(3.5h)│8 (6h)  │2 (1h)│  4 (1.5h)     │
│  Sprint 3   │ 1.5  │  12h  │  6(3.5h)│8(5.5h) │2 (1h)│  5 (2h)       │
│  Sprint 4   │  1   │  8h   │  3 (2h)│4(3.5h) │1 (1h)│  4 (1.5h)     │
├─────────────┼──────┼───────┼────────┼─────────┼──────┼───────────────┤
│  TOTAL      │  6   │  48h  │ 19(12h)│29(24h) │12(6h)│ 17 (6h)       │
└─────────────┴──────┴───────┴────────┴─────────┴──────┴───────────────┘

---

# ⚠️ FACTORES DE RIESGO Y CONTINGENCIA

|🔴 Riesgo|Impacto|✅ Mitigación|
|---|---|---|
|Configuración JWT/Security compleja|+2h|Usar Spring Security 6 con ejemplos oficiales|
|Redis para logout/blacklist|+1h|Usar Redis local para desarrollo, documentar bien|
|Despliegue en nube|+2h|Tener cuenta en Render/Railway previamente|
|Integración frontend-backend|+2h|Mantener comunicación constante con IA|
|Tests de cobertura|+1h|Usar JaCoCo y enfocarse en lógica crítica|

---

# 📌 RECOMENDACIONES PARA LA DESARROLLADORA

## ✅ ANTES DE EMPEZAR

- Tener instalado: Java 17, Maven, PostgreSQL, Redis, Docker
    
- Crear cuenta en Render o Railway
    
- Tener Postman instalado
    
- Clonar repositorio base de Spring Boot
    

## ✅ DURANTE EL DESARROLLO

- Hacer commits frecuentes (mínimo 1 por tarea completada)
    
- Correr tests después de cada endpoint
    
- Documentar mientras se codifica (Swagger)
    
- Usar ramas por sprint (sprint-1, sprint-2, etc.)
    

## ✅ AL FINALIZAR CADA SPRINT

- Todos los tests pasando
    
- README actualizado
    
- Colección Postman exportada
    
- Hacer merge a main
    

---

# 📞 CONTACTO Y SOPORTE

|Recurso|Enlace|
|---|---|
|Documentación Spring Security|[https://spring.io/projects/spring-security](https://spring.io/projects/spring-security)|
|Documentación JWT|[https://jwt.io/](https://jwt.io/)|
|Documentación PostgreSQL|[https://www.postgresql.org/docs/](https://www.postgresql.org/docs/)|
|Documentación Redis|[https://redis.io/docs/](https://redis.io/docs/)|
|Documentación Docker|[https://docs.docker.com/](https://docs.docker.com/)|

---

# 🏁 CHECKLIST FINAL DE ENTREGA

- Código fuente en repositorio Git (GitHub/GitLab)
    
- README.md con instrucciones de instalación
    
- Colección Postman completa
    
- Tests unitarios (cobertura mínima 70%)
    
- Despliegue en Railway/Render con PostgreSQL
    
- Documentación Swagger/OpenAPI accesible
    
- Dockerfile funcionando
    

---

**📝 NOTA:** Este documento está diseñado para ser consultado rápidamente. Cada tarea tiene un color según su complejidad:

- 🟢 **Fácil** (< 30min)
    
- 🟡 **Media** (30-60min)
    
- 🔴 **Compleja** (> 1h)
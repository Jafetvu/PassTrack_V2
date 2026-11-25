# INTEGRACIONES · Servicios y Flujo CI/CD (PassTrack)

## 1. Tabla de Decisiones de Integración

| # | Decisión | Alternativa descartada | Por qué | Fecha |
| :--- | :--- | :--- | :--- | :--- |
| **I-01** | Persistencia de comprobantes mediante volumen Docker dedicado (`/app/uploads`) o URL de almacenamiento en la nube servida de forma controlada. | Guardar los PDFs en una carpeta temporal dentro de la capa efímera del contenedor. | Si Jenkins recrea el contenedor de `PassTrack_backend` en un despliegue, los archivos guardados sin volumen persistente se perderían. | 2026-11-05 |
| **I-02** | Inyección de secretos (`DB_PASSWORD`, `JWT_SECRET`) mediante variables de entorno definidas en Jenkins Credentials y archivo `.env` local no versionado. | Dejar contraseñas fijas en `application.properties` dentro del repositorio Git. | Cumple las buenas prácticas de DevSecOps y evita exponer la llave privada que firma los tokens JWT. | 2026-11-05 |
| **I-03** | Disparo del pipeline en Jenkins mediante GitHub Webhook (`push` sobre la rama `main`). | Ejecución manual del pipeline en la interfaz de Jenkins. | Automatiza de punta a punta el ciclo de actualización al subir cambios al repositorio. | 2026-11-05 |

## 2. Mapa de Integraciones en el Roadmap

| Orden | Familia | Integración | Propósito en PassTrack | Fase del SOW |
| :---: | :--- | :--- | :--- | :---: |
| **1** | Base de Datos | MySQL 8.0 (`mysql:8.0`) | Almacenamiento relacional de usuarios, grupos, inscripciones, asistencias y justificantes. | Fase 1 |
| **2** | Almacenamiento | Volumen persistente `/app/uploads` (o API Cloudinary/S3) | Resguardo de evidencias en formato PDF/JPG (máximo 2 MB por archivo). | Fase 3 |
| **3** | Automatización CI/CD | GitHub Webhook → Jenkins → Docker Compose | Ejecutar pruebas automatizadas, empaquetar artefactos y actualizar los contenedores en producción. | Fase 1 a 3 |

## 3. Detalle Técnico de las Integraciones

### 3.1 GitHub Webhook + Jenkins + Docker Engine
* **Qué datos viajan:** GitHub envía un payload JSON por `POST` a `/github-webhook/` en Jenkins cada vez que se registra un `push` en `main`.
* **Flujo de ejecución del `Jenkinsfile`:**
  1. `Checkout`: Clona el repositorio.
  2. `Test Backend & Frontend`: Ejecuta `./scripts/check.sh` (`./mvnw test` en `PassTrack_backend` y `npm run lint` / `test` en `PassTrack_frontend`).
  3. `Build Docker Images`: Ejecuta `docker compose build` usando las variables inyectadas por `withCredentials`.
  4. `Deploy`: Ejecuta `docker compose up -d --remove-orphans` actualizando los contenedores sin intervención manual.
* **Qué pasa si falla:** Si una prueba unitaria o de roles (`403 Forbidden`) falla en la etapa 2, Jenkins detiene el pipeline inmediatamente, marca el build como `FAILURE` y no reemplaza los contenedores que están corriendo actualmente.

### 3.2 Almacenamiento de Comprobantes de Justificantes
* **Validación de entrada:** `PassTrack_backend` valida que el archivo tenga extensión/MIME type permitido (`application/pdf`, `image/jpeg`, `image/png`) y un tamaño menor o igual a `2 MB`.
* **Cómo se prueba en CI:** Durante la ejecución de `./mvnw test` en Jenkins, el componente de almacenamiento utiliza un directorio temporal en memoria o mock (`@MockBean`) para no depender de almacenamiento externo.
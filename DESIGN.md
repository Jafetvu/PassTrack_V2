<!-- ... existing code ... -->
## 1. Decisiones de Diseño UI/UX

| \# | Decisión | Alternativa descartada | Por qué | Fecha | 
| ----- | ----- | ----- | ----- | ----- | 
| **DS-01** | Estética Tech Corporativa (Minimalista, alta legibilidad). | Estética plana gubernamental o muy colorida/vibrante. | Transmite modernidad, seriedad y alta eficiencia. Inspirado en estándares de diseño como Vercel o Stripe. | 2026-11-07 | 
| **DS-02** | Tipografía única: 'Inter' (Google Fonts). | Combinar Serif (títulos) y Sans-Serif (cuerpos). | 'Inter' está optimizada para interfaces digitales, maximiza la legibilidad en tablas de datos y mantiene consistencia. | 2026-11-07 | 
| **DS-03** | Bordes sutiles y redondeado de `6px` a `8px`. | Bordes completamente rectos o formas tipo "píldora". | Logra un balance corporativo; suaviza la interfaz pero mantiene la estructura rígida de una tabla de datos. | 2026-11-07 | 
| **DS-04** | Sombras muy ligeras (`box-shadow: 0 1px 3px rgba(0,0,0,0.1)`). | Sombras profundas o diseño 100% flat (Material Design antiguo). | Crea una jerarquía visual limpia para diferenciar las tarjetas (cards) del fondo sin saturar la vista. | 2026-11-07 | 

## 2. Paleta de Colores Base

Los colores base deben configurarse en el archivo `tailwind.config.js` extendiendo el tema predeterminado.

* **Main Text / Nav Bg (Slate Oscuro):** `#0F172A` (Tailwind `slate-900`) — *Texto principal, fondos de barra lateral o navegación.*

* **Muted Text (Slate Muted):** `#475569` (Tailwind `slate-600`) — *Subtítulos, placeholders y metadatos.*

* **Primary Accent (Azul Océano):** `#0284C7` (Tailwind `sky-600`) — *Botones principales, focus states y enlaces. Aporta un toque tech moderno.*

* **Background Page (Gris Muy Claro):** `#F8FAFC` (Tailwind `slate-50`) — *Fondo general de la aplicación para hacer resaltar las tarjetas.*

* **Surface (Blanco):** `#FFFFFF` — *Fondo de las tarjetas, inputs y tablas.*

* **Borders (Gris Sutil):** `#E2E8F0` (Tailwind `slate-200`) — *Divisores y bordes de elementos.*

## 3. Estados Semánticos (Badges)

Los colores de estado se aplican estrictamente según las reglas del dominio de `Asistencia` y `Justificante`.

| Estado | Contexto | Color (Texto/Borde) | Color (Fondo) | 
| ----- | ----- | ----- | ----- | 
| **`PRESENTE`** | Pase de lista normal. | Verde (`#10B981` / `emerald-500`) | Verde Claro (`#ECFDF5` / `emerald-50`) | 
| **`FALTA`** | Inasistencia / Solicitud PENDIENTE. | Rojo (`#E11D48` / `rose-600`) | Rojo Claro (`#FFF1F2` / `rose-50`) | 
| **`RETARDO`** | Llegada tardía. | Naranja (`#F59E0B` / `amber-500`) | Naranja Claro (`#FFFBEB` / `amber-50`) | 
| **`JUSTIFICADO`** | Falta justificada / Solicitud APROBADA. | Azul (`#0284C7` / `sky-600`) | Azul Claro (`#F0F9FF` / `sky-50`) | 

## 4. Escala Tipográfica (Inter)

Se utiliza la familia de fuentes `font-sans` configurada con 'Inter'.

* **H1 (Títulos de Página):** `text-3xl font-bold tracking-tight text-slate-900` (30px).

* **H2 (Secciones):** `text-xl font-semibold tracking-tight text-slate-900` (20px).

* **H3 (Tarjetas/Modales):** `text-lg font-semibold text-slate-900` (18px).

* **Body (Texto general):** `text-sm font-normal text-slate-900` (14px). *Optimizado para dashboards densos.*

* **Small (Metadatos/Subtítulos):** `text-xs text-slate-600` (12px).

## 5. Directrices de Componentes (React)

En alineación con la convención **C-02 (Contenedor/Presentacional)**, los componentes de interfaz deben seguir el estilo minimalista corporativo.

* **Inputs y Formularios:** Fondo blanco, borde `#E2E8F0` y texto `#0F172A`. En estado `:focus`, el borde cambia a `#0284C7` y recibe un `box-shadow` suave del mismo color (Ring de Tailwind).

* **Botones:** `border-radius: 6px`. El botón primario es `#0284C7` (sin borde). Los botones secundarios (`outline`) tienen fondo blanco y borde sutil, con un ligero oscurecimiento del fondo en estado `:hover`.

* **Tarjetas (Cards):** Deben tener `bg-white`, un borde sutil `border-slate-200`, `rounded-lg` (8px) y una sombra ligera `shadow-sm`. Sin encabezados pesados, dejando que la tipografía establezca la jerarquía.
<!-- ... existing code ... -->
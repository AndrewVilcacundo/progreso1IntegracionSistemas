# Progreso 1: Integración de Sistemas - SaludVital

Repositorio con la solución completa para la evaluación de **Integración de Sistemas (ISWZ3104)**, enfocada en la automatización del flujo de pre-registros médicos entre los sistemas de **Admisiones** y **Facturación** de la clínica SaludVital.

---

# Descripción del problema

Actualmente, la clínica maneja la integración mediante archivos CSV procesados manualmente, lo que genera:

* Reprocesamiento de archivos
* Pérdida de información
* Errores en los datos
* Falta de trazabilidad

Este proyecto implementa una solución automatizada basada en el patrón **File Transfer** usando **Apache Camel**.

---

# Tecnologías utilizadas

* Java
* Apache Camel
* Maven
* File Component (`file:`)

---

# Estructura del repositorio

```bash
/
├── data/
│   ├── archive/        # Respaldos con timestamp
│   ├── error/          # Archivos inválidos
│   ├── input/          # Entrada de archivos CSV
│   └── output/         # Archivos válidos para facturación
├── src/main/java/      # Lógica Camel (MainApp.java, FileRoute.java)
├── Entregables.md      # Documento de análisis y diseño
├── api-admisiones.yaml # Especificación OpenAPI (Swagger)
└── pom.xml             # Configuración Maven
```

---

# Flujo de integración

```text
input → validación → (válido → output + archive)
                     (inválido → error)
```

---

# Contenido del proyecto

## 1. Implementación de integración (Apache Camel)

* Automatización mediante rutas (`FileRoute.java`)
* Procesamiento de archivos CSV desde `/data/input`
* Validación completa de contenido
* Enrutamiento inteligente según resultado

### Validaciones implementadas

* Encabezado correcto:

```csv
patient_id,full_name,appointment_date,insurance_code
```

* Campos obligatorios no vacíos
* Fecha en formato `YYYY-MM-DD`
* insurance_code válido:

  * IESS
  * PRIVADO
  * NINGUNO

---

## 2. Documento analítico (`Entregables.md`)

Incluye:

* Análisis del problema de integración
* Justificación del uso de File Transfer
* Identificación de riesgos
* Diseño de la solución (flujo, carpetas, validaciones)
* Propuesta de evolución hacia API REST

---

## 3. API futura (`api-admisiones.yaml`)

Se define una API REST basada en OpenAPI (Swagger) que permite:

* Eliminar dependencia de archivos
* Procesamiento en tiempo real
* Mejor trazabilidad

### Endpoint principal:

```http
POST /patients
```

---

# Ejecución del proyecto

## 1. Colocar archivo CSV

Ubicar un archivo en:

```bash
data/input/
```

Ejemplo:

```csv
patient_id,full_name,appointment_date,insurance_code
1,Juan Perez,2026-04-23,IESS
2,Ana Lopez,2026-04-24,PRIVADO
```

---

## 2. Ejecutar la aplicación

Ejecutar:

```bash
MainApp.java
```

---

## 3. Resultado esperado

### Archivo válido:

* Se mueve a `data/output`
* Se copia a `data/archive` con timestamp
* Se elimina de `input`

### Archivo inválido:

* Se mueve a `data/error`

---

# Ejemplo de archivado

```bash
pacientes_2026-04-23_195108.csv
```

---

# Manejo de errores

* Uso de `onException` en Apache Camel
* Validaciones estructurales y de contenido
* Logs en consola para trazabilidad

---

# Evidencias del sistema

El sistema permite evidenciar:

* Automatización del flujo
* Separación de archivos válidos e inválidos
* Archivado con timestamp
* Prevención de reprocesamiento

---

# Evolución futura

Se propone migrar a una arquitectura basada en APIs REST para:

* Integración en tiempo real
* Mayor seguridad
* Mejor control y trazabilidad

---

# Autor

**Andrew Vilcacundo**

---

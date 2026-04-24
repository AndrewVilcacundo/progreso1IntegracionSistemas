# Progreso 1: Integración de Sistemas

## Parte 1: Análisis del caso

**a. Problema de integración principal**
El problema fundamental radica en la falta de integración en línea entre el Sistema de Admisiones y el Sistema de Facturación de la clínica SaludVital. Actualmente, la dependencia de un proceso completamente manual para mover archivos CSV, validarlos, respaldarlos y evitar reprocesamientos ha introducido errores humanos (archivos perdidos, reprocesados, inconsistencia de datos y falta de trazabilidad). Este enfoque no es escalable ante el crecimiento de la clínica.

**b. Justificación del estilo de integración (File Transfer)**
Dado que se indica que una integración basada en servicios (API) no es viable de inmediato, la **Transferencia de Archivos (File Transfer)** es la única opción realista y pragmática a corto plazo. Permite automatizar el flujo actualmente manual (mover, respaldar, enrutar por error/éxito) utilizando middleware sin tener que alterar el código interno de ninguno de los dos sistemas involucrados de forma intrusiva, ya que Admisiones ya genera archivos periódicos.

**c. Riesgos y limitaciones de esta aproximación vs APIs**
1. **Latencia / Asincronía:** En File Transfer no hay inmediatez; se procesan datos por lotes en diferido, a diferencia de una API REST que permite operaciones síncronas en tiempo real.
2. **Fragilidad de esquemas y formatos:** Los CSV son susceptibles a errores de formato (columnas movidas, comas en los datos, codificación de caracteres) obligando a lógicas de validación complejas, mientras que el contrato de una API (JSON Schema) es mucho más estricto y fácil de validar a nivel de plataforma.
3. **Manejo de errores:** Si un registro en un CSV falla, ¿rechazamos todo el archivo o solo una línea? En una API, cada pre-registro exitoso/fallido tiene un código HTTP inmediato (201, 400), facilitando el retry granular.

---

## Parte 2: Diseño de la Solución

El diseño de integración utiliza **Apache Camel** para automatizar el procesamiento mediante el patrón _Content-Based Router_ y _Message Filter_ bajo el endpoint `file:`.

**Estructura del proceso:**
1. **Endpoint de entrada (`data/input`):** Un _consumer_ (poleo) constante revisa la llegada de archivos CSV. Una vez levantado el mensaje al _exchange_, se elimina el archivo original (`delete=true`) para evitar reprocesamiento constante.
2. **Proceso de validación:** Se ejecuta un `Process` local para validar línea a línea que no falten datos obligatorios (encabezado, columnas, fechas y formato de código de seguro).
3. **Flujo Cíclico Atómico:**
   - Si la validación **ES EXITOSA**, el flujo primero escribe en la carpeta `data/archive` con un nombre de archivo dinámico (`toD`) usando un timestamp para trazabilidad (evita que se sobreescriba historial). Luego lo copia a `data/output` para ser consumido por facturación.
   - Si la validación **FALLA** (se lanza una Excepción de Java), un bloque de control de errores general (`onException`) lo intercepta. Usando `useOriginalMessage()`, toma el mensaje original intacto, registra el fallo en consola para trazabilidad y mueve el CSV a la carpeta `data/error`.

---

## Parte 4: Evolución futura del proceso mediante API

**a. Propuesta de mejora**
Se propone exponer el Sistema de Admisiones vía API REST. En lugar de generar archivos CSV periódicos con pre-registros no confirmados, el Sistema de Admisiones insertará (o Facturación consultará) directamente los pre-registros listos. 
Optimizaríamos el **envío de pre-registros** haciendo inserciones atómicas (`POST /api/v1/pre-registros`). Esto reduce drásticamente la latencia, valida en tiempo real con JSON estricto, asegura trazabilidad con códigos HTTP de éxito y anula por completo la necesidad de lidiar con archivos corruptos compartidos.

**b. Diseño de la API**
- **Recurso:** `/pre-registros`
- **Endpoint:** `POST /api/v1/pre-registros`
- **Body de Request:** Objeto JSON con los 4 campos pre-validados.
- **Códigos HTTP de respuesta:** 
   - `201 Created` en caso de registro correcto.
   - `400 Bad Request` en caso de fallo de validación (JSON Schema/Insurance value incorrecto).
   - `409 Conflict` (Ej: Si el patient_id para esa cita ya existe).

*(La documentación detallada en estándar OpenAPI se adjunta en el archivo `api-admisiones.yaml`)*

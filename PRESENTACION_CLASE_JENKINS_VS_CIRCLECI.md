# GUÍA DE PRESENTACIÓN: CI/CD COMPARATIVA JENKINS VS CIRCLECI

**Actividad:** Presentación en Clase (25% P1)  
**Asignatura:** Integración de Sistemas  
**Ponente:** Andrew Vilcacundo  

---

## ESTRUCTURA DE DIAPOSITIVAS Y GUION ORAL

---

### Diapositiva 1: Portada
- **Título:** Optimización de Pipelines CI/CD: Comparación Técnica entre Jenkins y CircleCI
- **Subtítulo:** Caso de Estudio sobre un Microservicio Apache Camel
- **Autor:** Andrew Vilcacundo
- **Evidencia / Visual sugerido:** Logos de Jenkins, CircleCI, Docker y Apache Camel.
- **Explicación oral:**
  > "Buenas tardes profesor y compañeros. Hoy voy a presentar el análisis comparativo de integración y entrega continua implementado sobre nuestro proyecto de procesamiento de admisiones médicas. Evaluamos dos de los orquestadores más influyentes de la industria: Jenkins, como referente On-Premise, y CircleCI, como solución nativa en la nube, analizando cómo impactan la caché, el paralelismo y la ejecución selectiva en el ciclo de vida del software."

---

### Diapositiva 2: ¿Qué es CI/CD y por qué es crítico?
- **Contenido:**
  - **CI (Continuous Integration):** Fusión frecuente del código, compilación automatizada y ejecución continua de pruebas unitarias y análisis estático.
  - **CD (Continuous Delivery):** Automatización del empaquetado y preparación de artefactos listos para desplegar a entornos de prueba o producción.
  - **Beneficios clave:** Detección temprana de bugs, reducción drástica del tiempo de entrega y reproducibilidad de compilación.
- **Evidencia / Visual:** Diagrama del loop infinito de DevOps (Plan -> Code -> Build -> Test -> Release -> Deploy).
- **Explicación oral:**
  > "La integración continua busca que ningún cambio rompa el sistema; cada commit se valida automáticamente mediante pruebas y linters. La entrega continua da un paso más: garantiza que tengamos un artefacto validado y listo para producción sin intervención manual propensa a errores humanos."

---

### Diapositiva 3: Jenkins vs CircleCI: Dos Filosofías Diferentes
- **Contenido:**
  - **Jenkins:**
    - Arquitectura On-Premise / Servidor dedicado.
    - Configuración Groovy DSL (`Jenkinsfile`).
    - Gran ecosistema de plugins; alta sobrecarga administrativa.
  - **CircleCI:**
    - Cloud-Native / SaaS (Serverless).
    - Configuración declarativa YAML (`.circleci/config.yml`).
    - Contenedores Docker efímeros y limpios en cada ejecución.
- **Evidencia / Visual:** Tabla comparativa visual entre servidor dedicado vs contenedores cloud.
- **Explicación oral:**
  > "La diferencia fundamental radica en el modelo de ejecución: mientras Jenkins requiere instalar, mantener y parchar un servidor con Java y plugins, CircleCI opera bajo demanda en la nube, levantando contenedores Docker limpios para cada tarea, eliminando la contaminación de workspaces entre ejecuciones."

---

### Diapositiva 4: Proyecto Base: Microservicio Apache Camel
- **Contenido:**
  - **Tecnología:** Java 17, Apache Maven, Apache Camel 3.20.5.
  - **Propósito:** Ingesta y validación de archivos CSV con admisiones médicas (`FileRoute.java`).
  - **Pruebas y Calidad:**
    - JUnit 5 + Camel Test (`FileRouteTest.java`).
    - Checkstyle (Reglas de estilo Google).
    - SpotBugs (Análisis de vulnerabilidades y defectos de código).
- **Evidencia / Visual:** Diagrama de arquitectura del microservicio y estructura de carpetas en Git.
- **Explicación oral:**
  > "El proyecto evaluado no es un 'Hola Mundo', sino un servicio real de integración empresarial con Apache Camel que procesa lotes de admisiones médicas. Incluye pruebas unitarias rigurosas con JUnit 5 y dos herramientas de auditoría estática de código: Checkstyle y SpotBugs."

---

### Diapositiva 5: Arquitectura del Pipeline Original en Jenkins
- **Contenido:**
  - Etapas secuenciales y paralelas definidas en `Jenkinsfile`.
  - Etapa 1: Checkout y preparación de directorio `.m2`.
  - Etapa 2: Bloque `parallel` (Build & Tests vs Calidad/SpotBugs).
  - Etapa 3: Continuous Delivery condicionado a rama `main`.
- **Evidencia / Visual:** Captura de pantalla del Pipeline en Jenkins (Blue Ocean o Stage View) mostrando las ramas paralelas.
- **Explicación oral:**
  > "En Jenkins definimos un pipeline declarativo. Tras el checkout, abrimos un bloque de paralelismo para compilar y probar por un lado, mientras paralelamente se ejecutaba el análisis de calidad con Checkstyle y SpotBugs. Al finalizar ambas, si estábamos en la rama main, se comprobaba la existencia del artefacto JAR final."

---

### Diapositiva 6: Implementación en CircleCI: Jobs y Workflows
- **Contenido:**
  - Arquitectura modular basada en 6 Jobs independientes:
    1. `checkout-code`: Clona e inspecciona metadatos.
    2. `build`: Compilación y resolución de dependencias.
    3. `unit-tests`: Ejecución JUnit 5.
    4. `code-quality`: Checkstyle + SpotBugs.
    5. `package-and-delivery`: Empaquetado JAR.
    6. `notify-success`: Alerta a Discord.
  - Orquestación en Grafo Acíclico Dirigido (DAG).
- **Evidencia / Visual:** Captura del grafo interactivo de CircleCI con los jobs conectados.
- **Explicación oral:**
  > "Al trasladar este pipeline a CircleCI, diseñamos una arquitectura orientada a jobs desacoplados. A diferencia de Jenkins donde las etapas corren en el mismo workspace local, en CircleCI cada job corre en un contenedor Docker independiente, comunicándose mediante espacios de trabajo persistidos y cachés compartidas."

---

### Diapositiva 7: Estrategia de Optimización 1: Caché de Dependencias
- **Contenido:**
  - **Clave Criptográfica:** `v1-dependencies-{{ checksum "pom.xml" }}`.
  - **Mecanismo:** Salva y restaura el repositorio `.m2/repository`.
  - **Impacto:** Si `pom.xml` no cambia, no se descargan las librerías de Maven Central por internet.
- **Evidencia / Visual:** Fragmento de código YAML con `restore_cache` y captura de CircleCI mostrando 'Restoring cache (Found)'.
- **Explicación oral:**
  > "La primera gran optimización fue la caché declarativa. Mediante una clave ligada al hash criptográfico del pom.xml, CircleCI solo descarga dependencias la primera vez. En ejecuciones posteriores, restaura el repositorio local en segundos desde la caché interna, ahorrando ancho de banda y tiempo valioso."

---

### Diapositiva 8: Estrategia de Optimización 2: Paralelismo y Concurrencia
- **Contenido:**
  - Los jobs `unit-tests` y `code-quality` se bifurcan simultáneamente tras el `build`.
  - Ambos jobs corren en máquinas virtuales independientes.
  - El tiempo del pipeline se reduce al tiempo del job más crítico (Camino Crítico).
- **Evidencia / Visual:** Gráfico comparativo de ejecución secuencial vs paralela.
- **Explicación oral:**
  > "Aprovechamos el paralelismo configurando que tanto las pruebas unitarias como el análisis estático se ejecuten al mismo tiempo tan pronto finaliza la compilación. Esto reduce el tiempo total de espera de los desarrolladores prácticamente a la mitad durante la fase de auditoría."

---

### Diapositiva 9: Estrategia de Optimización 3: Ejecución Selectiva
- **Contenido:**
  - Filtro por ramas: `filters: branches: only: [main]`.
  - Los jobs `package-and-delivery` y `notify-success` solo se ejecutan cuando el código se integra en la rama de producción.
  - Ahorro de créditos de cómputo en ramas temporales (`feature/*`).
- **Evidencia / Visual:** Fragmento de código del bloque `filters` en CircleCI.
- **Explicación oral:**
  > "No todas las etapas deben correr en todas las ramas. Implementamos ejecución selectiva: si un desarrollador trabaja en una rama feature, solo se validan compilación, pruebas y linters; la fase de empaquetado final y notificación oficial de release queda restringida exclusivamente a la rama main."

---

### Diapositiva 10: Notificaciones Seguras en Tiempo Real (Discord)
- **Contenido:**
  - Integración mediante **Discord Webhook** sin plugins adicionales.
  - Seguridad: Credenciales inyectadas mediante variable de entorno segura `DISCORD_WEBHOOK`.
  - Envío automático de Embeds con Rama, Commit SHA, Autor y enlace de auditoría.
- **Evidencia / Visual:** Captura real del mensaje embed verde recibido en el canal de Discord.
- **Explicación oral:**
  > "Para la observabilidad del equipo, integramos notificaciones en tiempo real a Discord. Implementamos un comando reutilizable que envía un payload JSON formateado sin exponer jamás el webhook en el repositorio de GitHub, protegiendo las credenciales mediante las variables de entorno seguras de CircleCI."

---

### Diapositiva 11: Comparativa Técnica de Resultados y Tiempos
- **Contenido:**
  - Tabla comparativa de métricas reales (Jenkins vs CircleCI).
  - Tiempos de Build, Tests, Calidad y Total.
  - Facilidad de configuración y reproducibilidad.
- **Evidencia / Visual:** Tabla con los tiempos reales cronometrados en ambas plataformas.
- **Explicación oral:**
  > "Al comparar los resultados de ambos pipelines bajo el mismo commit, observamos que CircleCI ofrece una consistencia superior gracias a sus contenedores efímeros. Mientras Jenkins depende del estado y librerías preinstaladas en la máquina host, CircleCI garantiza que el pipeline se ejecute de idéntica manera en cualquier entorno."

---

### Diapositiva 12: Conclusiones y Lecciones Aprendidas
- **Contenido:**
  - **¿Cuándo usar Jenkins?** En organizaciones con infraestructura propia, requerimientos de datos locales sin salida a internet o pipelines hipercomplejos dependientes de hardware físico.
  - **¿Cuándo usar CircleCI?** En proyectos modernos, microservicios, equipos ágiles y entornos cloud donde la velocidad, el aislamiento por Docker y la baja sobrecarga operativa sean prioridad.
- **Evidencia / Visual:** Resumen visual de conclusiones clave.
- **Explicación oral:**
  > "Como conclusión, hemos demostrado que un pipeline CI/CD bien diseñado aprovecha al máximo la caché, el paralelismo y la ejecución selectiva para minimizar tiempos sin sacrificar calidad. CircleCI destaca por su agilidad y modernidad, mientras que Jenkins mantiene su valor en infraestructuras cerradas. Muchas gracias, quedo atento a sus preguntas."


# Camel CSV Processor

Proyecto Java con **Apache Camel 3.20.5** que procesa archivos CSV médicos
desde una carpeta de entrada y los enruta según su validez.

## 📁 Estructura

```
camel-csv-processor/
├── pom.xml
├── data/
│   ├── input/      ← Poner aquí los CSV a procesar
│   ├── output/     ← CSV válidos
│   ├── archive/    ← CSV válidos con timestamp
│   └── error/      ← CSV inválidos
└── src/main/java/
    ├── MainApp.java
    └── FileRoute.java
```

## 🚀 Cómo ejecutar

### Con Maven (recomendado)
```bash
mvn compile exec:java
```

### Desde IntelliJ / Antigravity
1. Abrir el proyecto (`pom.xml` como proyecto Maven)
2. Ejecutar `MainApp`

## ✅ Validaciones del CSV

| Campo              | Regla                              |
|--------------------|------------------------------------|
| Encabezado         | `patient_id,full_name,appointment_date,insurance_code` |
| Columnas por fila  | Mínimo 4                           |
| Campos vacíos      | No permitidos                      |
| `appointment_date` | Formato `yyyy-MM-dd`               |
| `insurance_code`   | Solo: `IESS`, `PRIVADO`, `NINGUNO` |

## 🧪 Archivos de prueba incluidos

- `data/input/pacientes_validos.csv` → debe ir a `/output` y `/archive`
- `data/input/pacientes_invalidos.csv` → debe ir a `/error`

## 📌 Características destacadas

- ✔ `onException` para manejo centralizado de errores
- ✔ `toD` para nombre dinámico con timestamp en archive
- ✔ `delete=true` para evitar reprocesamiento
- ✔ Logs con emojis para trazabilidad clara
- ✔ Normalización de saltos de línea Windows/Linux

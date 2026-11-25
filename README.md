# Junta de Vecinos Zaror

App Android (Kotlin, Jetpack Compose) para gestionar arriendos y finanzas de una junta de vecinos, conectada por Retrofit al backend `JuntaVecinosBackend`

## Formas de ejecutar
| Forma | Dirección que usa la app | Requiere |
|---|---|---|
| A. Emulador (por defecto) | `http://10.0.2.2:9090/` | Nada adicional |
| B. Teléfono por USB | `http://localhost:9090/` | `apiBaseUrl` activo y `adb -d reverse` |

En todas, el backend debe estar levantado primero

## Requisitos
- Docker Desktop
- Android Studio con el SDK de Android 37 instalado
- Emulador o teléfono con Android 7.0 o superior
- Conexión a Internet la primera vez, para descargar imágenes y librerías

Para las pruebas unitarias solo se necesita Android Studio


## 1. Levantar el backend
Con Docker Desktop iniciado, desde la carpeta `JuntaVecinosBackend`:

1. Ejecutar:

```powershell
docker compose up --build -d
```

2. Comprobar que los 8 contenedores digan `healthy`:

```powershell
docker compose ps
```

El backend queda en `http://localhost:9090`. Estado del Gateway: `http://localhost:9090/actuator/health`

Para detenerlo: `docker compose down`. Agregar `-v` **borra la base de datos**

## 2. Ejecutar la app

### A. Emulador
1. En `gradle.properties`, la línea debe estar comentada:

```properties
# apiBaseUrl=http://localhost:9090/
```

2. Presionar **Sync Now**
3. Elegir el emulador y presionar **Run**
4. En Android 17 o superior, aceptar el permiso de dispositivos cercanos (acceso a la red local)

### B. Teléfono por USB
1. En el teléfono, activar las **Opciones de desarrollador** y la **Depuración por USB**
2. Conectarlo al PC y aceptar la autorización
3. En `gradle.properties`, quitar el `#`:

```properties
apiBaseUrl=http://localhost:9090/
```

4. Presionar **Sync Now**
5. Conectar el puerto del teléfono con el PC, desde PowerShell:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -d reverse tcp:9090 tcp:9090
```

6. Elegir el teléfono y presionar **Run**

El paso 5 se repite al desconectar el cable o reiniciar el teléfono. Para comprobarlo, abrir `http://localhost:9090/actuator/health` en el navegador del teléfono

## 3. Iniciar sesión
Cuentas ficticias iniciales. También se puede crear una cuenta desde la app, siempre con perfil Vecino

| Perfil | Correo | Contraseña |
|---|---|---|
| Vecino | vecino@zaror.test | Vecino.2026 |
| Directiva | directiva@zaror.test | Directiva.2026 |
| Tesorería | tesoreria@zaror.test | Tesoreria.2026 |
| Admin | admin@zaror.test | Admin.2026 |

## 4. Pruebas unitarias
Son 30 pruebas, ubicadas en `app/src/test`. Se ejecutan en el PC, **sin emulador, teléfono ni backend**

| Herramienta | Para qué se usa |
|---|---|
| JUnit 5 | Motor que ejecuta las pruebas |
| Kotest | Escribir las pruebas y comparar resultados |
| MockK | Simular el backend, la base local y los repositorios |
| coroutines-test | Probar funciones asíncronas (`suspend`) |

### Qué se prueba
| Archivo | Pruebas | Comprueba |
|---|---|---|
| `logic/ValidacionTest` | 5 | Teléfono, fecha, capacidad, choque de horario y formulario de reserva |
| `model/RolTest` | 4 | Permisos de cada perfil |
| `utils/FechaUtilsTest` | 1 | Conversión de fechas entre la app y el backend |
| `data/remote/ResultadoTest` | 2 | Sin conexión y mensaje de error del backend |
| `data/remote/AuthInterceptorTest` | 3 | Uso del token y cierre de sesión con 401 |
| `data/repository/AuthRepositoryTest` | 2 | Inicio de sesión correcto e incorrecto |
| `data/repository/ReservasRepositoryTest` | 3 | Reservas propias, cambio de sesión y horario ocupado |
| `data/repository/FinanzasRepositoryTest` | 1 | El vecino no accede a los movimientos |
| `viewmodel/SesionViewModelTest` | 2 | Error de login y registro |
| `viewmodel/ReservasViewModelTest` | 3 | Envío de solicitud, conflicto y cambio de estado |
| `viewmodel/FinanzasViewModelTest` | 3 | Resumen, reservas sin pago y no duplicar un pago |
| `viewmodel/AdminViewModelTest` | 1 | Activar y desactivar usuarios |

### Ejecutar desde Android Studio
1. En la vista **Project**, clic derecho sobre `app/src/test/java`
2. Elegir **Run 'Tests in...'**
3. Revisar que todas aparezcan en verde en el panel **Run**

Para ejecutar un solo archivo, clic derecho sobre él y **Run**

### Ver los informes
Se abren con el navegador:

| Informe | Ubicación |
|---|---|
| Resultados | `app/build/reports/tests/testDebugUnitTest/index.html` |
| Cobertura | `app/build/reports/jacoco/cobertura/html/index.html` |

Las pruebas cubren  validaciones, fechas, comunicación, repositorios, estados y ViewModel. No se consideran las pantallas ni las clases que dependen del dispositivo (sesión cifrada, cámara, imágenes y notificaciones)

## Para generar y firmar una nueva APK
1. Cambiar nombre de `keystore.properties.example` a `keystore.properties`
2. Escribir las contraseñas de `juntavecinos.jks`
3. En Android Studio: **Build > Generate Signed App Bundle or APK**

## Problemas frecuentes
| Problema | Solución |
|---|---|
| "No se pudo conectar con el servidor" | Revisar que el backend esté levantado y que `apiBaseUrl` corresponda al dispositivo. En teléfono, repetir `adb -d reverse` |
| "Ocurrió un error en el servidor" | Revisar con `docker compose ps` que todos los contenedores digan `healthy` |
| Se cambió `apiBaseUrl` y no se aplica | Sincronizar y presionar **Run** otra vez: la dirección se define al compilar |
| Funciona en el emulador, pero no en el teléfono | La app se compiló con `10.0.2.2`, que solo existe en el emulador |
| No conecta usando la IP del PC por Wi-Fi | La app solo permite HTTP hacia `10.0.2.2` y `localhost` |
| `adb`: `unauthorized` | Desbloquear el teléfono y aceptar la depuración USB |
| `adb`: `more than one device` | Usar `-d` (teléfono) o `-e` (emulador) |
| `Access denied` en los registros del backend | `MYSQL_PASSWORD` del `.env` no coincide con la guardada en MySQL |
| Docker indica que falta `SECRET` | No existe el archivo `.env`: copiar `.env.example` |
| El puerto 9090 está ocupado | Cerrar el programa que lo usa y volver a levantar el backend |
| `JAVA_HOME is not set` al ejecutar `gradlew` | Ejecutar la línea de `JAVA_HOME` de la sección 4 |
| `SDK location not found` | Abrir el proyecto en Android Studio para que cree `local.properties` |
| Las pruebas no aparecen en Android Studio | Presionar **Sync Now** y volver a intentarlo |
| Error de `checksum` de Liquibase | Se editó un changeset ya aplicado: restaurarlo y agregar uno nuevo |

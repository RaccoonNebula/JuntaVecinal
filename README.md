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

## 1. Levantar el backend
Con Docker Desktop iniciado, desde la carpeta `JuntaVecinosBackend`:

1. Solo la primera vez, copiar `.env.example` como `.env`
2. Ejecutar:

```powershell
docker compose up --build -d
```

3. Comprobar que los 8 contenedores digan `healthy`:

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
| Error de `checksum` de Liquibase | Se editó un changeset ya aplicado: restaurarlo y agregar uno nuevo |

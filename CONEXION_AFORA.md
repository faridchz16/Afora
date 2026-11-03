# Conexión AFORA y puesta en marcha

## Compatibilidad conservada

AppAfora mantiene minSdk 19 (Android 4.4 en adelante, incluida 4.4.2), targetSdk 28, compileSdk 37, Java 8, AGP 9.3.3, Gradle 9.5.0 y su única dependencia de pruebas JUnit 4.13.2. No se añadió ninguna dependencia, SDK Firebase ni requisito de Google Play Services. afora-main conserva minSdk 24 (Android 7.0 en adelante), target/compileSdk 37 y sus dependencias existentes.

Se reutiliza Firebase Realtime Database **REST**, que ya estaba en AppAfora. Se usan HTTPS y TLS 1.2 con verificación normal de certificados y nombre del servidor. Una tablet antigua puede tener certificados raíz desactualizados o cifrados no aceptados por el servidor: debe probarse el equipo real. Si falla TLS, no activar HTTP ni aceptar todos los certificados; diagnosticar la cadena del servidor y, si fuera necesario, preparar un intermediario HTTPS con certificado y cifrados compatibles. Esa alternativa no está desplegada.

## Configurar el servicio compartido

1. En el proyecto Firebase existente, confirmar la URL exacta de Realtime Database. El valor anterior era `https://appafora-8d62f-default-rtdb.firebaseio.com`.
2. Habilitar **Authentication > Proveedores > Correo electrónico/contraseña**.
3. Copiar la clave Web API del mismo proyecto desde su configuración general a `app/src/main/res/values/firebase_config.xml` **en ambos proyectos**. Ajustar allí también `firebase_database_url`. El marcador `CONFIGURAR_CLAVE_WEB_FIREBASE` no es una credencial funcional. No incluir secretos de base de datos ni cuentas de servicio en los APK. Si la clave tiene restricciones, permitir Identity Toolkit y Secure Token para este cliente REST.
4. Crear en Authentication una cuenta exclusiva del encargado. Copiar su UID. No usar esa cuenta como cuenta de estudiante.
5. Desde la consola de Realtime Database, agregar `/controllers/UID_DEL_ENCARGADO = true` (booleano). Esta lista solo se administra desde la consola o un servidor de confianza. Ninguna app puede asignarse ese permiso.
6. Publicar **backend/database.rules.json** como reglas de Realtime Database, reemplazando las reglas públicas anteriores. Alternativamente usar `firebase deploy --only database --project ID_REAL_DEL_PROYECTO` desde esta carpeta con la CLI autenticada.
7. Conservar el `/aforo` existente. Si no existe, inicializarlo desde consola con `currentPeople: 0`, `maxCapacity: 50` y `lastUpdated` en milisegundos. `backend/initial-data.example.json` es una referencia: sustituir el UID y no importar sobre datos existentes. Si los datos anteriores superan la capacidad, corregirlos conscientemente antes de probar.
8. Compilar e instalar cada APK en su dispositivo correspondiente. No es necesario `google-services.json` para REST.

Las reglas permiten consultar públicamente únicamente el aforo, que no contiene datos personales. Solo la cuenta de controlador autorizada puede modificarlo y consultar/escribir historial. Un registro nuevo no obtiene privilegios. Los estudiantes nunca deben recibir las credenciales del controlador. El permiso pertenece a la cuenta autenticada; no depende del gesto oculto ni de ocultar la URL.

## Funcionamiento

AppAfora conserva el panel oculto. Al primer movimiento solicita las credenciales del encargado; permanecen en memoria durante la sesión del controlador, sin almacenar contraseña. La sesión renueva el token cuando vence. Al salir del panel con Atrás se cierra la sesión. Las escrituras conservan la protección ETag ante conflictos, rechazan aforo negativo, entradas por encima de la capacidad y una capacidad inferior al conteo actual. El reinicio conserva su confirmación existente.

El servidor conserva `/aforo` aunque la tablet cierre AppAfora. El celular consulta ese mismo objeto cada dos segundos mientras la app está visible, sin depender de que la tablet esté abierta. Tras una desconexión muestra el último valor recibido con aviso, sin inventar un contador local. Los movimientos requieren conexión; no hay cola fuera de línea. Ante un corte durante un envío, comprobar el contador compartido antes de repetir: el servidor podría haber aceptado el movimiento aunque no llegara su respuesta.

El historial sigue siendo secundario: si falla después de guardar el aforo, no se repite el movimiento. En ese caso puede faltar un evento del historial. No se promete una transacción conjunta entre contador e historial.

En afora-main, “Crear una cuenta” solicita nombre, correo, contraseña y confirmación. Se validan campos, formato y coincidencia; Firebase evita duplicados y gestiona las contraseñas sin guardarlas en el dispositivo. Se inicia sesión con la cuenta registrada y se saluda por el nombre. Solo se conserva el token de renovación, cifrado mediante Android Keystore, excluido de copias y transferencias. Cerrar sesión lo borra. Si el servicio no está disponible al recuperar una sesión, se presenta el acceso y se puede intentar de nuevo.

## Comprobación en tablet y celular

1. Conectar ambos a Internet (pueden estar en redes distintas).
2. Registrar un estudiante desde el celular; probar campos vacíos, correo inválido, contraseñas distintas y correo duplicado. Cerrar sesión y probar contraseña incorrecta y correcta.
3. Cerrar y volver a abrir afora-main; debe recuperar la sesión y mostrar el nombre. Cerrar sesión debe exigir acceso al volver a abrir.
4. Abrir el panel de AppAfora y autorizar al encargado. Hacer tres ingresos y dos salidas; ambos dispositivos deben coincidir en aproximadamente dos segundos más la latencia de red.
5. Comprobar cero, capacidad máxima y reinicio con confirmación. Usar una capacidad pequeña para facilitar la prueba y restaurarla después.
6. Cerrar AppAfora: afora-main debe seguir mostrando el último aforo del servidor. Reabrir la tablet y comprobar coincidencia.
7. Desconectar Internet en el celular: debe avisar que el dato es anterior; reconectar y comprobar recuperación automática.
8. Intentar autorizar el panel con la cuenta del estudiante: el servidor debe denegar cambios. No basta con ocultar botones.
9. Probar HTTPS en la tablet Android 4.4.2 real. Una compilación correcta no prueba sus certificados ni su red.

## Archivos

**afora-main:** `AforaApp.kt` (registro/login, sesión y consulta de aforo), `FirebaseSession.kt` (REST y renovación), `SessionStore.kt` (token cifrado), `SharedAforoRepository.kt` (solo lectura), manifiesto (permiso Internet), `firebase_config.xml` y reglas de copias/transferencias. Se agregaron `backend/database.rules.json`, ejemplo de datos, pruebas REST locales y `firebase.json`.

**AppAfora:** `MainActivity.kt` (autorizar encargado), `data/AforoRestRepository.kt` (autenticación, límites y manejo del historial), nuevo `data/FirebaseSession.kt`, `firebase_config.xml` y README. Se conservan archivos de compilación, dependencias, vistas y modelos existentes.

Las pantallas de ambiente, gráficas y avisos de ejemplo existentes en afora-main no se han convertido en nuevas funciones. La sincronización solicitada afecta al aforo actual en Inicio y Aforo.

## Pruebas locales del servicio

Desde afora-main, con Node, Java y Firebase CLI disponibles:

```powershell
npm.cmd exec --yes --package firebase-tools -- firebase emulators:exec --only auth,database --project demo-afora "node backend/integration-test.mjs"
```

El script se limita a emuladores de bucle local y usa cuentas ficticias. Comprueba registro, duplicados, contraseña incorrecta, nombre, permisos de estudiantes/controladores, límites, reinicio, conflictos ETag y consultas compartidas. No conecta a producción ni sustituye las pruebas de interfaz, cierre/reapertura de Android o TLS en dispositivos físicos.

## Verificación realizada el 26 de octubre de 2026

- Ambos proyectos generan APK debug con la configuración existente.
- AppAfora: compilación y tarea de pruebas unitarias completadas; esas pruebas existentes no prueban la red física.
- afora-main: tres pruebas instrumentadas completadas en el emulador Medium_Phone Android 17/API 37, incluidas las dos nuevas para validaciones del formulario y token cifrado/recuperación/borrado. No equivale a probar el ciclo completo de acceso contra producción.
- Firebase Auth y Realtime Database locales: pruebas REST de registro, duplicados, contraseña incorrecta/correcta, recuperación del nombre, permisos de controlador, prohibición de autoasignar rol, rechazo de valores negativos/sobre capacidad/borrado, entradas/salidas, reinicio, lectura compartida y conflicto ETag completadas.
- Análisis de AppAfora: sin errores `NewApi`; el análisis completo no pasa por el error existente `ExpiredTargetSdkVersion` asociado a targetSdk 28, más advertencias de recursos/configuración preexistentes. No se cambió targetSdk ni se silenciaron esas comprobaciones.
- Pendientes: configurar clave Web API y reglas en el proyecto real, autorización del encargado, acceso completo contra producción, persistencia del aforo tras reiniciar el servicio real, cierre/reapertura real de ambas aplicaciones conectadas y prueba física simultánea tablet/celular (incluido TLS/certificados de Android 4.4.2). No se publicaron reglas ni se modificó Firebase de producción.

## Configuración posterior de la clave

Se encontró `AppAFORA/app/google-services.json` del proyecto `appafora-8d62f` y se copió su clave Web API existente a los recursos de ambos proyectos. Ya no es necesario sustituir el marcador en estas copias locales. Esto no verifica que Authentication por correo/contraseña esté habilitado, que las restricciones de la clave permitan ambos clientes REST ni que las reglas y el UID del encargado estén publicados. Esos pasos siguen pendientes de comprobación en el proyecto real.

## Autorización de AppAfora sin contraseña (revisión posterior)

AppAfora ya no utiliza el formulario de correo/contraseña descrito en las instrucciones anteriores. En Firebase Authentication debe habilitarse el proveedor **Anónimo**. Desde AppAfora, pulsar **Autorizar tablet**, copiar su UID y agregar `/controllers/UID_DE_LA_TABLET = true` (booleano) en Realtime Database. Finalmente pulsar **Comprobar**. No hay que crear una cuenta de encargado con contraseña. Se mantienen las mismas reglas seguras y los estudiantes conservan sus cuentas de correo en afora-main.

La identificación se guarda cifrada en la tablet y se recupera al volver a abrir la app. La habilitación del proveedor y la aprobación del UID en el proyecto real no se ejecutaron desde aquí. Se validaron con emuladores locales tanto el rechazo de una tablet no autorizada como las escrituras y la renovación de una autorizada, y la revocación posterior.

# Recipe: Crear una Pantalla de Login (Android)

Aplica: [`../knowledge/android/architecture.md`](../knowledge/android/architecture.md), [`../decisions/jwt-vs-session.md`](../decisions/jwt-vs-session.md), `CLAUDE.md` sección 12.

## Pasos

### 1. Caso de uso de autenticación

```kotlin
class IniciarSesionUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resultado<Sesion> {
        return authRepository.iniciarSesion(email, password) // el repository llama al backend (JWT, ver decisions/)
    }
}
```

### 2. Almacenamiento seguro del token

```kotlin
class AuthRepositoryImpl(
    private val api: AuthApi,
    private val encryptedPrefs: SharedPreferences, // creado con EncryptedSharedPreferences, nunca SharedPreferences normal
) : AuthRepository {

    override suspend fun iniciarSesion(email: String, password: String): Resultado<Sesion> {
        return try {
            val response = api.login(LoginRequest(email, password))
            encryptedPrefs.edit()
                .putString("access_token", response.accessToken)
                .putString("refresh_token", response.refreshToken)
                .apply()
            Resultado.Exito(Sesion(response.usuarioId))
        } catch (e: HttpException) {
            if (e.code() == 401) Resultado.Error(CredencialesInvalidasException())
            else Resultado.Error(e)
        }
    }
}
```

```kotlin
val encryptedPrefs = EncryptedSharedPreferences.create(
    context, "auth_prefs",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
)
```

`SharedPreferences` sin cifrar es legible por cualquier proceso con acceso root al dispositivo — `EncryptedSharedPreferences` cifra tanto claves como valores, apropiado para tokens de sesión (ver `CLAUDE.md` sección 12).

### 3. ViewModel con validación de formulario

```kotlin
class LoginViewModel(private val iniciarSesion: IniciarSesionUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onSubmit() {
        val estado = _uiState.value
        if (!estado.email.esEmailValido()) { // ver knowledge/kotlin/language.md, extension function
            _uiState.update { it.copy(errorEmail = "Email no válido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            when (val resultado = iniciarSesion(estado.email, estado.password)) {
                is Resultado.Exito -> navegarAHome()
                is Resultado.Error -> _uiState.update {
                    it.copy(cargando = false, errorGeneral = "Credenciales incorrectas")
                }
            }
        }
    }
}
```

### 4. Nunca loguear credenciales

```kotlin
// PROHIBIDO
Timber.d("Intentando login con email=%s password=%s", email, password)

// Correcto: sin datos sensibles
Timber.d("Intento de login iniciado")
```

Ver [`../standards/logging-standard.md`](../standards/logging-standard.md) — ningún dato de credenciales, ni siquiera el email, debería aparecer en logs de depuración que puedan terminar en un sistema de recolección de logs.

### 5. Refresh token automático ante expiración

```kotlin
val authenticator = object : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val nuevoToken = runBlocking { authRepository.refrescarToken() } ?: return null // null: no reintentar, forzar logout
        return response.request.newBuilder().header("Authorization", "Bearer $nuevoToken").build()
    }
}

val client = OkHttpClient.Builder().authenticator(authenticator).build()
```

Ver [`../decisions/jwt-vs-session.md`](../decisions/jwt-vs-session.md) para el razonamiento del framework sobre access tokens de vida corta + refresh token.

## Checklist final

- [ ] ¿El token se almacena en `EncryptedSharedPreferences`, nunca en `SharedPreferences` sin cifrar?
- [ ] ¿Ningún log incluye email/contraseña/tokens en claro?
- [ ] ¿Existe manejo de refresh token automático ante expiración del access token?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../decisions/jwt-vs-session.md`](../decisions/jwt-vs-session.md), `CLAUDE.md` sección 12.

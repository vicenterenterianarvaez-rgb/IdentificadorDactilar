package com.example.identificadordactilar;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {
    private TextView txtEstado;
    private ImageView imgHuella;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Conectar los componentes del diseño.
        txtEstado = findViewById(R.id.txtEstado);
        imgHuella = findViewById(R.id.imgHuella);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Respuestas de la autenticación.
        biometricPrompt = new BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        txtEstado.setText(
                                "¡Escaneo de huella dactilar exitoso! "
                                        + "Iniciando sesión…"
                        );
                        imgHuella.setImageResource(R.drawable.ic_correcto);

                        Toast.makeText(
                                MainActivity.this,
                                "Autenticación exitosa",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Abrir Resultado solo después del éxito.
                        Intent intent = new Intent(
                                MainActivity.this,
                                Resultado.class
                        );
                        startActivity(intent);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();

                        mostrarError(
                                "Escaneo fallido, huella dactilar no registrada"
                        );

                        Toast.makeText(
                                MainActivity.this,
                                "Escaneo fallido, huella dactilar no registrada",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                                || errorCode == BiometricPrompt.ERROR_USER_CANCELED
                                || errorCode == BiometricPrompt.ERROR_CANCELED) {

                            imgHuella.setImageResource(R.drawable.huella);
                            txtEstado.setText(
                                    "Autenticación cancelada. "
                                            + "Pulsa Ingresar para intentarlo de nuevo."
                            );
                        } else {
                            mostrarError(
                                    "No se pudo completar la autenticación: "
                                            + errString
                            );
                        }
                    }
                }
        );

        // Configurar el cuadro de autenticación del sistema.
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Identificador Dactilar")
                .setSubtitle("Coloca tu dedo en el sensor del teléfono")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
                .setNegativeButtonText("Cancelar")
                .build();

        // El botón ahora inicia la autenticación.
        findViewById(R.id.scanButton).setOnClickListener(
                v -> iniciarAutenticacion()
        );
    }

    private void iniciarAutenticacion() {
        BiometricManager manager = BiometricManager.from(this);

        int disponibilidad = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        switch (disponibilidad) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                imgHuella.setImageResource(R.drawable.huella);
                txtEstado.setText(
                        "Coloca tu dedo en el sensor\npara iniciar sesión."
                );
                biometricPrompt.authenticate(promptInfo);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                mostrarError(
                        "Primero registra una huella en los ajustes del teléfono."
                );
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                mostrarError(
                        "Este dispositivo no tiene un sensor biométrico compatible."
                );
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                mostrarError(
                        "El sensor no está disponible. Inténtalo más tarde."
                );
                break;

            default:
                mostrarError(
                        "La autenticación biométrica no está disponible. "
                                + "Revisa la configuración del teléfono."
                );
                break;
        }
    }

    private void mostrarError(String mensaje) {
        txtEstado.setText(mensaje);
        imgHuella.setImageResource(R.drawable.ic_error);
    }

    // Restaurar la pantalla al regresar desde Resultado.
    @Override
    protected void onRestart() {
        super.onRestart();

        imgHuella.setImageResource(R.drawable.huella);
        txtEstado.setText(
                "Coloca tu dedo en el sensor\npara iniciar sesión."
        );
    }
}
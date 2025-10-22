package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.AuthLogger;
import edu.pucmm.eict.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.jasypt.util.text.BasicTextEncryptor;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class AuthController {

    private final UserService userService;
    private static final String ENCRYPTION_KEY = "MiClaveSecretaMuyFuerte";

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/login", ctx -> ctx.render("login.html"));
        app.get("/register", ctx -> ctx.render("register.html"));

        app.post("/login", this::handleLogin);
        app.post("/register", this::handleRegister);

        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.removeCookie("remember-me");
            ctx.redirect("/");
        });
    }

    private void handleLogin(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        System.out.println("Username recibido: '" + username + "'");


        Usuario usuario = userService.findUsuario(username);
        if (usuario != null && usuario.getPassword().equals(password)) {
            ctx.sessionAttribute("usuario", usuario);

            try {
                AuthLogger authLogger = new AuthLogger();
                authLogger.logAuthEvent(username);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Verificar si el usuario marcó "Recuérdame"
            String remember = ctx.formParam("remember");
            if (remember != null && remember.equals("on")) {
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(ENCRYPTION_KEY);
                String encryptedData = textEncryptor.encrypt(username);
                // Crear cookie "remember-me" con duración de 7 días (604800 segundos)
                ctx.cookie("remember-me", encryptedData, 604800);
            }

            ctx.redirect(usuario.isAdministrador() ? "/dashboard" : "/");
        } else {
            ctx.attribute("error", "Credenciales incorrectas.");
            ctx.render("login.html");
        }
    }

    private void handleRegister(Context ctx) {
        String username = ctx.formParam("username");
        if (userService.findUsuario(username) != null) {
            ctx.attribute("error", "El usuario ya existe.");
            ctx.render("register.html");
            return;
        }
        String nombre = ctx.formParam("nombre");
        String password = ctx.formParam("password");
        List<String> roles = ctx.formParams("roles");
        boolean esAutor = roles.contains("autor");
        boolean esAdmin = roles.contains("admin");

        // Procesar la foto, si se subió alguna
        String fotoBase64 = null;
        String fotoContentType = null;
        List<UploadedFile> uploadedFiles = ctx.uploadedFiles("foto");
        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            UploadedFile uploadedFile = uploadedFiles.get(0);
            try {
                byte[] bytes = uploadedFile.content().readAllBytes();
                if (bytes.length > 0) {
                    fotoBase64 = Base64.getEncoder().encodeToString(bytes);
                    fotoContentType = uploadedFile.contentType();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Si no se subió ninguna foto, usar la imagen predeterminada de internet
        if (fotoBase64 == null) {
            try {
                java.net.URL defaultUrl = new java.net.URL("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png");
                try (java.io.InputStream is = defaultUrl.openStream();
                     java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    byte[] defaultBytes = baos.toByteArray();
                    fotoBase64 = Base64.getEncoder().encodeToString(defaultBytes);
                    fotoContentType = "image/png";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Usuario usuario = new Usuario(username, nombre, password, esAdmin, esAutor);
        // Asigna la foto predeterminada si se obtuvo (o la foto subida)
        if (fotoBase64 != null) {
            usuario.setFoto(fotoBase64);
            usuario.setFotoContentType(fotoContentType);
        }
        userService.crearUsuario(usuario);
        ctx.sessionAttribute("mensaje", "Usuario registrado exitosamente.");
        ctx.redirect("/");
    }
}

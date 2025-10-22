package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Base64;

public class FotoController {

    private final UserService userService;
    public static final String FOTO_POR_DEFECTO = "public/noPFP.webp";

    public FotoController(UserService userService) {
        this.userService = userService;
    }

    public void registerRoutes(Javalin app) {
        app.post("/actualizar-foto", this::actualizarFoto);
        app.get("/profile-photo", this::getProfilePhoto);
    }

    private void actualizarFoto(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");

        if (usuario == null) {
            ctx.status(401).result("No autorizado");
            return;
        }

        var uploadedFiles = ctx.uploadedFiles("foto");
        if (uploadedFiles == null || uploadedFiles.isEmpty()) {
            ctx.status(400).result("No se subió ninguna foto.");
            return;
        }

        var uploadedFile = uploadedFiles.get(0);
        try {
            byte[] bytes = uploadedFile.content().readAllBytes();
            if (bytes.length > 0) { // Se verifica que se haya subido contenido
                String fotoBase64 = Base64.getEncoder().encodeToString(bytes);
                String fotoContentType = uploadedFile.contentType();
                usuario.setFoto(fotoBase64);
                usuario.setFotoContentType(fotoContentType);
                userService.actualizarUsuario(usuario);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al procesar la imagen.");
            return;
        }

        ctx.redirect("/profile");
    }

    // Nuevo método para servir la foto de perfil del usuario logueado
    private void getProfilePhoto(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            ctx.status(401).result("No autorizado");
            return;
        }
        String fotoBase64 = usuario.getFoto();
        String contentType = usuario.getFotoContentType();
        if (fotoBase64 == null || fotoBase64.isEmpty() || contentType == null || contentType.isEmpty()) {
            // Si no hay foto, cargar la imagen predeterminada desde la URL
            try (java.io.InputStream in = new java.net.URL("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png").openStream()) {
                byte[] defaultImageBytes = in.readAllBytes();
                ctx.contentType("image/png");
                ctx.result(defaultImageBytes);
            } catch (java.io.IOException e) {
                e.printStackTrace();
                ctx.status(500).result("Error al cargar la imagen predeterminada.");
            }
            return;
        }
        try {
            byte[] imageBytes = Base64.getDecoder().decode(fotoBase64);
            ctx.contentType(contentType);
            ctx.result(imageBytes);
        } catch (IllegalArgumentException e) {
            ctx.status(500).result("Error al decodificar la imagen.");
        }
    }
}

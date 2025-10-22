package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuariosController {

    private UserService userService = new UserService();

    public void registerRoutes(Javalin app) {
        // Rutas protegidas: solo administradores pueden acceder
        app.before("/usuarios", ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuario");
            if (usuario == null || !usuario.isAdministrador()) {
                ctx.redirect("/dashboard");
            }
        });
        app.before("/form-usuario", ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuario");
            if (usuario == null || !usuario.isAdministrador()) {
                ctx.redirect("/dashboard");
            }
        });

        app.get("/usuarios", this::handleUsuarios);
        app.get("/delete-usuario", this::handleDeleteUsuario);
        app.get("/form-usuario", this::handleFormUsuario);
        app.post("/update-usuario", this::handleUpdateUsuario);
    }

    private void handleUsuarios(Context ctx) {
        List<Usuario> usuarios = userService.findAllUsuarios();
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", ctx.sessionAttribute("usuario")); // Usuario logueado (administrador)
        model.put("usuarios", usuarios);
        ctx.render("usuarios.html", model);
    }

    private void handleDeleteUsuario(Context ctx) {
        String username = ctx.queryParam("username");
        if (username == null || username.trim().isEmpty()) {
            ctx.attribute("error", "No se proporcionó el nombre de usuario.");
            handleUsuarios(ctx);
            return;
        }

        Usuario usuarioLogueado = ctx.sessionAttribute("usuario");
        if (usuarioLogueado != null && usuarioLogueado.getUsername().equals(username)) {
            // En lugar de redireccionar o usar result(), se añade el error y se re-renderiza la vista
            ctx.attribute("error", "No puedes eliminar tu propio usuario.");
            handleUsuarios(ctx);
            return;
        }

        boolean deleted = userService.eliminarUsuario(username);
        if (deleted) {
            ctx.redirect("/usuarios");
        } else {
            ctx.attribute("error", "Usuario no encontrado o no se pudo eliminar.");
            handleUsuarios(ctx);
        }
    }

    private void handleFormUsuario(Context ctx) {
        String username = ctx.queryParam("username");
        if (username == null || username.trim().isEmpty()) {
            ctx.status(400).result("No se proporcionó el nombre de usuario.");
            return;
        }
        Usuario userToEdit = userService.findUsuario(username);
        if (userToEdit == null) {
            ctx.status(404).result("Usuario no encontrado.");
            return;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", ctx.sessionAttribute("usuario")); // Usuario logueado (administrador)
        model.put("userToEdit", userToEdit);
        ctx.render("register.html", model);
    }

    private void handleUpdateUsuario(Context ctx) {
        // Obtenemos el username (readonly)
        String username = ctx.formParam("username");
        if (username == null || username.trim().isEmpty()) {
            ctx.status(400).result("No se proporcionó el nombre de usuario.");
            return;
        }
        Usuario user = userService.findUsuario(username);
        if (user == null) {
            ctx.status(404).result("Usuario no encontrado.");
            return;
        }
        // Actualizar nombre
        String nombre = ctx.formParam("nombre");
        user.setNombre(nombre);

        // Actualizar contraseña si se proporcionó (en modo edición, puede ser opcional)
        String password = ctx.formParam("password");
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }

        // Actualizar foto, si se subió
        List<UploadedFile> uploadedFiles = ctx.uploadedFiles("foto");
        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            UploadedFile file = uploadedFiles.get(0);
            try {
                byte[] bytes = file.content().readAllBytes();
                if (bytes.length > 0) { // Verificar si se subió contenido
                    String fotoBase64 = Base64.getEncoder().encodeToString(bytes);
                    String fotoContentType = file.contentType();
                    user.setFoto(fotoBase64);
                    user.setFotoContentType(fotoContentType);
                }
            } catch (IOException e) {
                e.printStackTrace();
                ctx.status(500).result("Error al procesar la imagen.");
                return;
            }
        }

        // Actualizar roles
        List<String> roles = ctx.formParams("roles");
        // Se asume que si no se marca, se pone false.
        user.setAutor(roles != null && roles.contains("autor"));
        user.setAdministrador(roles != null && roles.contains("admin"));

        // Actualizamos en la base de datos
        userService.editarUsuario(user);

        // Redireccionamos al dashboard de usuarios para mostrar los cambios
        ctx.redirect("/usuarios");
    }
}

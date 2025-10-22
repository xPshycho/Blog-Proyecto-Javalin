package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Usuario;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class ProfileController {

    public void registerRoutes(Javalin app) {
        // Middleware para asegurar autenticación en /profile
        app.before("/profile", ctx -> {
            if (ctx.sessionAttribute("usuario") == null) {
                ctx.redirect("/login");
            }
        });
        app.get("/profile", this::handleProfile);
    }

    private void handleProfile(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            ctx.redirect("/login");
            return;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        ctx.render("profile.html", model);
    }
}

package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.ArticuloService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    private ArticuloService articuloService = new ArticuloService();

    public void registerRoutes(Javalin app) {
        // Middleware para asegurar autenticación en /dashboard
        app.before("/dashboard", ctx -> {
            if (ctx.sessionAttribute("usuario") == null) {
                ctx.redirect("/login");
            }
        });
        app.get("/dashboard", this::handleDashboard);
    }

    private void handleDashboard(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            ctx.redirect("/login");
            return;
        }
        List<Articulo> articulos;
        if (usuario.isAdministrador()) {
            articulos = articuloService.findAllArticulos();
        } else {
            // Filtrar artículos para que muestre solo los del usuario actual
            articulos = articuloService.findAllArticulos().stream()
                    .filter(articulo -> articulo.getAutor().getUsername().equals(usuario.getUsername()))
                    .collect(Collectors.toList());
        }
        articulos.sort(Comparator.comparing(Articulo::getFecha, Comparator.reverseOrder()));

        HashMap<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("articulos", articulos);
        ctx.render("dashboard.html", model);
    }
}

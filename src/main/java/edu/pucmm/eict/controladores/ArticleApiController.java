package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.ArticuloService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;

public class ArticleApiController {

    private ArticuloService articuloService = new ArticuloService();
    private final int pageSize = 5; // Tamaño de página

    public void registerRoutes(Javalin app) {
        // Endpoint unificado para el listado paginado
        app.get("/api/articles", this::handleGetArticlesPaginated);
        // Endpoint para obtener el total de artículos según contexto
        app.get("/api/articles/count", this::handleGetArticlesCount);
    }

    private void handleGetArticlesPaginated(Context ctx) {
        int page = 1;
        // Obtener el parámetro "page"
        String pageParam = ctx.queryParam("page");
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                ctx.status(400).result("Número de página inválido.");
                return;
            }
        }

        boolean dashboard = "true".equalsIgnoreCase(ctx.queryParam("dashboard"));
        List<Articulo> articulos;

        if (dashboard) {
            Usuario usuario = ctx.sessionAttribute("usuario");
            if (usuario == null) {
                ctx.status(401).result("No autorizado.");
                return;
            }
            if (usuario.isAdministrador()) {
                articulos = articuloService.findArticulosPaginated(page, pageSize);
            } else {
                articulos = articuloService.findArticulosByAutorPaginated(usuario, page, pageSize);
            }
        } else {
            articulos = articuloService.findArticulosPaginated(page, pageSize);
        }
        ctx.json(articulos);
    }

    private void handleGetArticlesCount(Context ctx) {
        boolean dashboard = "true".equalsIgnoreCase(ctx.queryParam("dashboard"));
        long count;

        if (dashboard) {
            Usuario usuario = ctx.sessionAttribute("usuario");
            if (usuario == null) {
                ctx.status(401).result("No autorizado.");
                return;
            }
            if (usuario.isAdministrador()) {
                count = articuloService.countAllArticulos();
            } else {
                count = articuloService.countArticulosByAutor(usuario);
            }
        } else {
            count = articuloService.countAllArticulos();
        }
        ctx.json(count);
    }
}

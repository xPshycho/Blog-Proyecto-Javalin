package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Etiqueta;
import edu.pucmm.eict.services.ArticuloService;
import edu.pucmm.eict.services.EtiquetaService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {

    private ArticuloService articuloService = new ArticuloService();
    private EtiquetaService etiquetaService = new EtiquetaService();

    public void registerRoutes(Javalin app) {
        app.get("/", this::handleHome);
        app.get("/about", ctx -> ctx.render("about.html"));
    }

    private void handleHome(Context ctx) {
        int page = 1;
        String pageParam = ctx.queryParam("page");
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        int pageSize = 5;
        List<Articulo> articulos = articuloService.findArticulosPaginated(page, pageSize);
        long totalArticulos = articuloService.countAllArticulos();
        int totalPages = (int) Math.ceil((double) totalArticulos / pageSize);
        totalPages = totalPages < 1 ? 1 : totalPages;

        // Agregamos todas las etiquetas disponibles al modelo
        List<Etiqueta> allTags = etiquetaService.findAllEtiquetas();

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", ctx.sessionAttribute("usuario"));
        model.put("articulos", articulos);
        model.put("currentPage", page);
        model.put("totalPages", totalPages);
        model.put("allTags", allTags);
        ctx.render("index.html", model);
    }
}

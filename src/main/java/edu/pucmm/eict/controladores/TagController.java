package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Etiqueta;
import edu.pucmm.eict.services.ArticuloService;
import edu.pucmm.eict.services.EtiquetaService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagController {

    private ArticuloService articuloService = new ArticuloService();
    private EtiquetaService etiquetaService = new EtiquetaService();
    private final int pageSize = 5;

    public void registerRoutes(Javalin app) {
        app.get("/tag", this::handleTag);
    }

    private void handleTag(Context ctx) {
        // Primero, tratar de leer el parámetro "names" (para múltiples etiquetas)
        String namesParam = ctx.queryParam("names");
        List<String> selectedTags = new ArrayList<>();
        if (namesParam != null && !namesParam.trim().isEmpty()) {
            selectedTags = Arrays.asList(namesParam.split("\\s*,\\s*"));
        } else {
            // Si no existe "names", se revisa "name" para una única etiqueta
            String nameParam = ctx.queryParam("name");
            if (nameParam != null && !nameParam.trim().isEmpty()) {
                selectedTags.add(nameParam);
            }
        }

        // Precalcular la cadena separada por comas de las etiquetas seleccionadas
        String selectedTagsStr = String.join(",", selectedTags);

        // Procesar la paginación
        int page = 1;
        String pageParam = ctx.queryParam("page");
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        List<Articulo> articulos;
        long totalArticulos;
        if (selectedTags.isEmpty()) {
            // Si no hay etiquetas seleccionadas, mostrar todos los artículos
            articulos = articuloService.findArticulosPaginated(page, pageSize);
            totalArticulos = articuloService.countAllArticulos();
        } else {
            // Filtrar artículos que contengan todas las etiquetas seleccionadas
            articulos = articuloService.findArticulosByEtiquetasPaginated(selectedTags, page, pageSize);
            totalArticulos = articuloService.countArticulosByEtiquetas(selectedTags);
        }
        int totalPages = (int) Math.ceil((double) totalArticulos / pageSize);
        totalPages = totalPages < 1 ? 1 : totalPages;

        List<Etiqueta> allTags = etiquetaService.findAllEtiquetas();

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", ctx.sessionAttribute("usuario"));
        model.put("articulos", articulos);
        model.put("selectedTags", selectedTags);
        model.put("selectedTagsStr", selectedTagsStr);
        model.put("currentPage", page);
        model.put("totalPages", totalPages);
        model.put("allTags", allTags);
        ctx.render("tag.html", model);
    }
}

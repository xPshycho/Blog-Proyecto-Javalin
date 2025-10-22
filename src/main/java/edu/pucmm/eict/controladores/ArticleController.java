package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Etiqueta;
import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.ArticuloService;
import edu.pucmm.eict.services.EtiquetaService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.*;
import java.util.stream.Collectors;

public class ArticleController {

    private ArticuloService articuloService = new ArticuloService();
    private EtiquetaService etiquetaService = new EtiquetaService();

    public void registerRoutes(Javalin app) {
        app.get("/article", this::handleArticle);
        app.get("/form-articulo", this::handleArticleForm);
        app.post("/crear-articulo", this::handleCreateArticle);
        app.get("/delete-article", this::handleDeleteArticle);
    }

    private void handleArticle(Context ctx) {
        String articleId = ctx.queryParam("id");
        if (articleId == null) {
            ctx.status(400).result("ID del artículo no proporcionado.");
            return;
        }
        try {
            long id = Long.parseLong(articleId);
            Articulo articulo = articuloService.findArticulo(id);
            if (articulo == null) {
                ctx.status(404).result("Artículo no encontrado.");
                return;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("articulo", articulo);
            model.put("usuario", ctx.sessionAttribute("usuario"));
            ctx.render("article.html", model);
        } catch (NumberFormatException e) {
            ctx.status(400).result("ID inválido.");
        }
    }

    private void handleArticleForm(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            ctx.redirect("/login");
            return;
        }
        String idParam = ctx.queryParam("id");
        if (idParam != null) {
            long id = Long.parseLong(idParam);
            Articulo articulo = articuloService.findArticulo(id);
            if (articulo == null) {
                ctx.status(404).result("Artículo no encontrado.");
                return;
            }
            if (!usuario.isAdministrador() && !articulo.getAutor().equals(usuario)) {
                ctx.status(403).result("No tienes permisos para editar este artículo.");
                return;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("articulo", articulo);
            if (articulo.getEtiquetaList() != null && !articulo.getEtiquetaList().isEmpty()) {
                String etiquetasStr = articulo.getEtiquetaList().stream()
                        .map(Etiqueta::getEtiqueta)
                        .collect(Collectors.joining(", "));
                model.put("etiquetas", etiquetasStr);
            }
            ctx.render("form-articulo.html", model);
        } else {
            ctx.render("form-articulo.html");
        }
    }

    private void handleCreateArticle(Context ctx) {
        String idStr = ctx.formParam("id");
        String titulo = ctx.formParam("titulo");
        String contenido = ctx.formParam("contenido");
        String etiquetasStr = ctx.formParam("etiquetas");
        Usuario usuario = ctx.sessionAttribute("usuario");

        if (usuario == null || !usuario.isAutor()) {
            ctx.redirect("/login");
            return;
        }

        Articulo articulo;
        if (idStr != null && !idStr.trim().isEmpty()) {
            long id = Long.parseLong(idStr);
            articulo = articuloService.findArticulo(id);
            if (articulo == null) {
                ctx.status(404).result("Artículo no encontrado.");
                return;
            }
            if (!usuario.isAdministrador() && !articulo.getAutor().equals(usuario)) {
                ctx.status(403).result("No tienes permisos para editar este artículo.");
                return;
            }
            articulo.setTitulo(titulo);
            articulo.setCuerpo(contenido);
            articulo.setFecha(new Date());
            articuloService.editarArticulo(articulo);
        } else {
            articulo = new Articulo();
            articulo.setTitulo(titulo);
            articulo.setCuerpo(contenido);
            articulo.setFecha(new Date());
            articulo.setAutor(usuario);
            articuloService.crearArticulo(articulo);
        }

        Set<Etiqueta> etiquetasSet = new HashSet<>();
        if (etiquetasStr != null && !etiquetasStr.trim().isEmpty()) {
            String[] etiquetasArray = etiquetasStr.split(",");
            for (String et : etiquetasArray) {
                String tag = et.trim().toLowerCase();
                if (!tag.isEmpty()) {
                    Etiqueta etiquetaExistente = etiquetaService.findEtiquetaPorNombre(tag);
                    if (etiquetaExistente == null) {
                        Etiqueta nuevaEtiqueta = new Etiqueta();
                        nuevaEtiqueta.setEtiqueta(tag);
                        etiquetaService.crearEtiqueta(nuevaEtiqueta);
                        etiquetasSet.add(nuevaEtiqueta);
                    } else {
                        etiquetasSet.add(etiquetaExistente);
                    }
                }
            }
        }

        // Convertir el Set a List correctamente
        articulo.setEtiquetaList(new ArrayList<>(etiquetasSet));
        articuloService.editarArticulo(articulo);

        ctx.redirect(usuario.isAdministrador() ? "/dashboard" : "/");
    }


    private void handleDeleteArticle(Context ctx) {
        String idStr = ctx.queryParam("id");
        if (idStr == null) {
            ctx.status(400).result("No se proporcionó ID.");
            return;
        }
        long id = Long.parseLong(idStr);
        Articulo articulo = articuloService.findArticulo(id);
        if (articulo == null) {
            ctx.status(404).result("Artículo no encontrado.");
            return;
        }
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null || (!usuario.isAdministrador() && !articulo.getAutor().equals(usuario))) {
            ctx.status(403).result("No tienes permisos para borrar este artículo.");
            return;
        }
        articuloService.eliminarArticulo(id);
        ctx.redirect("/dashboard");
    }
}

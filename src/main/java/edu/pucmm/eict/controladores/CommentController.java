package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Comentario;
import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.ArticuloService;
import edu.pucmm.eict.services.ComentarioService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class CommentController {

    private ArticuloService articuloService = new ArticuloService();
    private ComentarioService comentarioService = new ComentarioService();

    public void registerRoutes(Javalin app) {
        app.post("/comment", this::handleAddComment);
        app.get("/delete-comment", this::handleDeleteComment);
    }

    private void handleAddComment(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            ctx.redirect("/login");
            return;
        }
        String comentarioTexto = ctx.formParam("comentario");
        String articleIdStr = ctx.formParam("articleId");
        if (articleIdStr == null || comentarioTexto == null || comentarioTexto.trim().isEmpty()) {
            ctx.redirect(ctx.req().getHeader("referer"));
            return;
        }
        long articleId = Long.parseLong(articleIdStr);
        Articulo articulo = articuloService.findArticulo(articleId);
        if (articulo == null) {
            ctx.status(404).result("Artículo no encontrado.");
            return;
        }

        // Crear el comentario y asociarlo al artículo y al usuario
        Comentario comentario = new Comentario();
        comentario.setComentario(comentarioTexto);
        comentario.setAutor(usuario);
        comentario.setArticulo(articulo);

        // Eliminamos la llamada a comentarioService.crearComentario(comentario)
        // y solo agregamos el comentario a la colección del artículo
        if (articulo.getComentarioList() == null) {
            articulo.setComentarioList(new LinkedHashSet<>());
        }
        articulo.getComentarioList().add(comentario);

        // Actualizamos el artículo para que, gracias a la cascada, se persista el comentario
        articuloService.editarArticulo(articulo);

        ctx.redirect("/article?id=" + articleId);
    }


    private void handleDeleteComment(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null || !(usuario.isAdministrador() || usuario.isAutor())) {
            ctx.status(403).result("No tienes permisos para borrar comentarios.");
            return;
        }
        String commentIdStr = ctx.queryParam("id");
        String articleIdStr = ctx.queryParam("articleId");
        if (commentIdStr == null || articleIdStr == null) {
            ctx.status(400).result("Faltan parámetros.");
            return;
        }
        long commentId = Long.parseLong(commentIdStr);
        long articleId = Long.parseLong(articleIdStr);
        Comentario comentario = comentarioService.findComentario(commentId);
        if (comentario == null) {
            ctx.status(404).result("Comentario no encontrado.");
            return;
        }
        comentarioService.eliminarComentario(commentId);
        // Actualizamos el artículo para eliminar el comentario de su lista
        Articulo articulo = articuloService.findArticulo(articleId);
        if (articulo != null && articulo.getComentarioList() != null) {
            articulo.getComentarioList().removeIf(c -> c.getId() == commentId);
            articuloService.editarArticulo(articulo);
        }
        ctx.redirect("/article?id=" + articleId);
    }
}

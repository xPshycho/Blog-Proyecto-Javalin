package edu.pucmm.eict;

import edu.pucmm.eict.controladores.*;
import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.DatabaseService;
import edu.pucmm.eict.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.h2.tools.Server;
import org.jasypt.util.text.BasicTextEncryptor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Inicia el servidor H2 en modo TCP antes de que se cree el EntityManagerFactory
        try {
            Server h2Server = Server.createTcpServer("-tcpAllowOthers", "-ifNotExists").start();
            System.out.println("Servidor H2 iniciado en: " + h2Server.getURL());
        } catch (SQLException e) {
            System.err.println("Error al iniciar el servidor H2: " + e.getMessage());
        }

        DatabaseService databaseService = DatabaseService.getInstance();
        databaseService.inicializarDatos();

        // Configuración de Thymeleaf
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateEngine.setTemplateResolver(templateResolver);


        UserService userService = new UserService();

        // Crear y configurar la aplicación Javalin
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.fileRenderer(new JavalinThymeleaf(templateEngine));
        }).start(7000);

        // Middleware para inyectar el usuario en cada petición
        app.before(ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuario");
            ctx.attribute("usuario", usuario);
        });

        // Middleware para el manejo de "remember-me"
        app.before(ctx -> {
            if (ctx.sessionAttribute("usuario") == null) {
                String cookieValue = ctx.cookie("remember-me");
                if (cookieValue != null) {
                    try {
                        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                        textEncryptor.setPassword("MiClaveSecretaMuyFuerte");
                        String decryptedUsername = textEncryptor.decrypt(cookieValue);
                        Usuario rememberedUser = userService.findUsuario(decryptedUsername);
                        if (rememberedUser != null) {
                            ctx.sessionAttribute("usuario", rememberedUser);
                            ctx.attribute("usuario", rememberedUser);
                        }
                    } catch (Exception e) {
                        ctx.removeCookie("remember-me");
                    }
                }
            }
        });

        // Registrar las rutas de los módulos
        new HomeController().registerRoutes(app);
        new AuthController(userService).registerRoutes(app);
        new DashboardController().registerRoutes(app);
        new ProfileController().registerRoutes(app);
        new ArticleController().registerRoutes(app);
        new CommentController().registerRoutes(app);
        new TagController().registerRoutes(app);
        new FotoController(userService).registerRoutes(app);
        new UsuariosController().registerRoutes(app);
        new ArticleApiController().registerRoutes(app);
        new ChatController().registerRoutes(app);
        new ChatWebSocketHandler().registerRoutes(app);
        new DashboardWebSocketHandler().registerRoutes(app);


        System.out.println("Aplicacion corriendo en http://localhost:7000");
    }
}

# Aplicación Web con Javalin

Este proyecto es una aplicación web desarrollada en Java utilizando el framework Javalin para el backend, Thymeleaf para las vistas, y una base de datos H2 embebida. Forma parte de las prácticas de Programación Web en el curso de Ingeniería en Computación y Telecomunicaciones (EICT) de la Pontificia Universidad Católica Madre y Maestra (PUCMM).

## Descripción

La aplicación permite la gestión de usuarios, artículos, comentarios, etiquetas, fotos y un sistema de chat en tiempo real. Incluye autenticación de usuarios con soporte para "remember-me", y utiliza WebSockets para funcionalidades interactivas como el chat y el dashboard.

## Características

- **Autenticación de Usuarios**: Registro, login y logout con encriptación de contraseñas y cookies persistentes.
- **Gestión de Artículos**: Crear, editar y visualizar artículos.
- **Comentarios**: Agregar comentarios a artículos.
- **Etiquetas (Tags)**: Asignar etiquetas a artículos.
- **Fotos**: Subir y gestionar fotos de usuarios.
- **Chat en Tiempo Real**: Comunicación vía WebSockets.
- **Dashboard**: Panel de control con actualizaciones en vivo.
- **API REST**: Endpoints para artículos y otras entidades.
- **Base de Datos H2**: Servidor TCP para persistencia de datos.

## Tecnologías Utilizadas

- **Java 11+**
- **Javalin**: Framework web ligero.
- **Thymeleaf**: Motor de plantillas para vistas HTML.
- **H2 Database**: Base de datos embebida.
- **Jasypt**: Para encriptación de datos sensibles.
- **WebSockets**: Para chat y dashboard en tiempo real.
- **Gradle**: Para gestión de dependencias y construcción del proyecto.

## Requisitos Previos

- JDK 11 o superior instalado.
- Gradle instalado (para compilar y ejecutar).

## Instalación

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/practica-5.git
   cd practica-5
   ```

2. Compila el proyecto con Gradle:
   ```bash
   gradle build
   ```

3. Ejecuta la aplicación:
   ```bash
   gradle run
   ```

La aplicación estará disponible en `http://localhost:7000`.

## Uso

- Accede a la página principal en `/`.
- Regístrate o inicia sesión para acceder a funcionalidades protegidas.
- Navega por artículos, agrega comentarios, sube fotos, etc.
- Usa el chat para comunicación en tiempo real.

## Estructura del Proyecto

- [`src/main/java/edu/pucmm/eict`](src/main/java/edu/pucmm/eict ): Código fuente Java.
  - `Main.java`: Punto de entrada de la aplicación.
  - `controladores/`: Controladores para rutas web.
  - `modelos/`: Entidades del modelo de datos.
  - `services/`: Servicios para lógica de negocio y base de datos.
- [`src/main/resources`](src/main/resources ): Recursos estáticos y plantillas.
  - `templates/`: Vistas Thymeleaf.
  - `public/`: Archivos estáticos (CSS, JS, imágenes).

## Contribución

1. Haz un fork del proyecto.
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`).
3. Commit tus cambios (`git commit -am 'Agrega nueva funcionalidad'`).
4. Push a la rama (`git push origin feature/nueva-funcionalidad`).
5. Abre un Pull Request.

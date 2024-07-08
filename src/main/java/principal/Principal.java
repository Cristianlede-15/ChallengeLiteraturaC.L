package principal;

import model.*;
import repository.IAutorRepository;
import repository.ILibroRepository;
import service.ConsumoApi;
import service.ConvierteDatos;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private final ILibroRepository repositoryLibro;
    private final IAutorRepository repositoryAutor;

    public Principal(ILibroRepository repositoryLibro, IAutorRepository repositoryAutor) {
        this.repositoryLibro = repositoryLibro;
        this.repositoryAutor = repositoryAutor;
    }

    public void muestraElMenu() {
        int opcion = -1;
        while (opcion != 0) {
            String menu = """
                    1 - Buscar libro por su título
                    2 - Mostrar lista de libros registrados
                    3 - Mostrar lista de autores registrados
                    4 - Mostrar lista de autores vivos en un año específico
                    5 - Mostrar libros por idiomas 
                    0 - Salir
                    """;
            System.out.println(menu);
            while (!teclado.hasNextInt()) {
                System.out.println("Selección inválida, por favor ingrese un número que corresponda a una opción disponible en el menú...");
                teclado.nextLine();
            }
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> buscarLibroTitulo();
                case 2 -> buscarLibroRegistro();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivos();
                case 5 -> listarLibrosIdiomas();
                case 0 -> System.out.println("Cerrando la aplicación...");
                default -> System.out.println("Opción inválida, por favor ingrese un número que esté disponible en el menú...");
            }
        }
    }

    private DatosBusqueda getDatosLibro() {
        System.out.println("Por favor, escribe el título del libro que deseas buscar.");
        String nombreLibro = teclado.nextLine();
        String json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "+"));
        return conversor.obtenerDatos(json, DatosBusqueda.class);
    }

    private void buscarLibroTitulo() {
        DatosBusqueda datosBusqueda = getDatosLibro();
        if (datosBusqueda == null || datosBusqueda.resultado().isEmpty()) {
            System.out.println("El libro no se encuentra...");
            return;
        }

        DatosLibros primerLibro = datosBusqueda.resultado().getFirst();
        Libro libro = new Libro(primerLibro);
        System.out.println("__| Libro: |__");
        System.out.println(libro);
        System.out.println("-_-_-_-_-_-_-_-");

        Optional<Libro> libroExistenteOptional = repositoryLibro.findByTitulo(libro.getTitulo());
        if (libroExistenteOptional.isPresent()) {
            System.out.println("\nEl libro ya se encuentra\n");
            return;
        }

        if (primerLibro.autor().isEmpty()) {
            System.out.println("No tiene autor");
            return;
        }

        DatosAutor datosAutor = primerLibro.autor().getFirst();
        Autor autor = new Autor(datosAutor);
        Optional<Autor> autorOptional = repositoryAutor.findByNombre(autor.getNombre());

        Autor autorExistente = autorOptional.orElseGet(() -> repositoryAutor.save(autor));
        libro.setAutor(autorExistente);
        repositoryLibro.save(libro);

        System.out.printf("""
                ---------- Libro ----------
                Título: %s
                Autor: %s
                Idioma: %s
                Número de Descargas: %d
                ---------------------------
                """, libro.getTitulo(), autor.getNombre(), libro.getLenguaje(), libro.getNumeroDescargas());
    }

    private void buscarLibroRegistro() {
        List<Libro> libros = repositoryLibro.findAll();

        if (libros.isEmpty()) {
            System.out.println("Sin libros registrados...");
            return;
        }

        System.out.println(" ___| Libros Registrados |___");
        libros.forEach(System.out::println);
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-");
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = repositoryAutor.findAll();

        if (autores.isEmpty()) {
            System.out.println("Sin autores registrados...");
            return;
        }

        System.out.println("___| Autores Registrados |___");
        autores.forEach(System.out::println);
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-");
    }

    private void listarAutoresVivos() {
        System.out.println("Ingresa en año: ");
        while (!teclado.hasNextInt()) {
            System.out.println("Ingrese un año valido...");
            teclado.nextLine();
        }
        int año = teclado.nextInt();
        teclado.nextLine();

        List<Autor> autores = repositoryAutor.findAutoresVivosEnaño(año);

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + año);
        } else {
            System.out.println("___| Autores Vivos en el Año " + año + " |___");
            autores.forEach(System.out::println);
            System.out.println("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-");
        }
    }

    private void listarLibrosIdiomas() {
        System.out.println("Selecciona el idioma: ");
        while (true) {
            String opciones = """
                    1. en - Inglés
                    2. es - Español
                    3. fr - Francés
                    4. pt - Portugués
                    0. Volver a las opciones anteriores
                    """;
            System.out.println(opciones);
            while (!teclado.hasNextInt()) {
                System.out.println("Ingrese un numero que se encuentre en el menu...");
                teclado.nextLine();
            }
            int opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> mostrarLibrosPorIdioma(Idioma.en);
                case 2 -> mostrarLibrosPorIdioma(Idioma.es);
                case 3 -> mostrarLibrosPorIdioma(Idioma.fr);
                case 4 -> mostrarLibrosPorIdioma(Idioma.pt);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Opción incorrecta...");
            }
        }
    }

    private void mostrarLibrosPorIdioma(Idioma idioma) {
        List<Libro> librosPorIdioma = repositoryLibro.findByLenguaje(idioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No hay libros en " + idioma.getIdiomaEspanol());
        } else {
            System.out.printf("___|- Libros en %s |____ %n", idioma.getIdiomaEspanol());
            librosPorIdioma.forEach(System.out::println);
            System.out.println("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-");
        }
    }
}
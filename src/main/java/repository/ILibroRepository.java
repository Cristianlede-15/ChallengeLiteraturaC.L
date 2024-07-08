package repository;

import model.Idioma;
import model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILibroRepository extends JpaRepository<Libro, Long> {
    // Método para buscar libros por título exacto
    Optional<Libro> findByTitulo(String titulo);

    List<Libro> findByLenguaje(Idioma lenguaje);
}
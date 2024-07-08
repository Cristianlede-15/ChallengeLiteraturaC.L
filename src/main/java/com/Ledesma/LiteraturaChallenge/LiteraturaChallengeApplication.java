package com.Ledesma.LiteraturaChallenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import principal.Principal;
import repository.IAutorRepository;
import repository.ILibroRepository;

@SpringBootApplication
public class LiteraturaChallengeApplication implements CommandLineRunner {
	@Autowired
	private IAutorRepository autorRepository;

	@Autowired
	private ILibroRepository libroRepository;

	public static void main(String[] args) {
		SpringApplication.run(LiteraturaChallengeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(libroRepository, autorRepository);
		principal.muestraElMenu();
	}
}
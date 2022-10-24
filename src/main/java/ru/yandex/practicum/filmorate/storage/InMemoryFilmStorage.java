package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage{

    private final HashMap<Integer, Film> films = new HashMap<>();

    private static int generateId = 1;

    private static Integer getNextId(){
        return generateId++;
    }

    @Override
    public Film add(Film film) {
        film.setFilmId(getNextId());
        films.put(film.getFilmId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getFilmId(), film);
        return film;
    }

    @Override
    public List<Film> findAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Integer filmId) {
        return films.get(filmId);
    }

    @Override
    public HashMap<Integer, Film> getFilms() {
        return films;
    }


}

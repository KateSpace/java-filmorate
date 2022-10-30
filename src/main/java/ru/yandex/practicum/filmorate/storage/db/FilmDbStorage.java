package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.DAO.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        int filmId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
        film.setId(filmId);
        return film;
    }

    @Override
    public void update(Film film) {
        String sql1 = "UPDATE FILMS SET FILM_NAME" + " = ?, DESCRIPTION = ?, RELEASE_DATE" +
                " =?, DURATION = ?, RATE = ?, MPA = ? " +
                "WHERE FILM_ID = ?";
        jdbcTemplate.update(sql1, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getRate(), film.getMpa().getId(), film.getId());
    }

    @Override
    public List<Film> findAllFilms() {
        String sql = "SELECT * FROM FILMS AS F, MPA AS M WHERE F.MPA = M.MPA_ID";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        for (Film film : films) {
            film.setGenres(getGenresSetForParticularFilm(film.getId()));
        }
        return films;
    }

    @Override
    public Film getFilmById(Integer filmId) {
        String sql2 ="SELECT * FROM FILMS AS F, MPA AS M WHERE F.MPA = M.MPA_ID AND F.FILM_ID = ?";
        Film film =jdbcTemplate.queryForObject(sql2, (rs, rowNum) -> makeFilm(rs), filmId);
        assert film != null;
        film.setGenres(getGenresSetForParticularFilm(filmId));
        return film;
    }

    @Override
    public List<Film> getPopular(Integer count) {
        String sql = "SELECT * FROM FILMS AS F " +
                "LEFT JOIN MPA AS M on M.MPA_ID = F.MPA " +
                "LEFT JOIN USER_LIKES AS UL on F.FILM_ID = UL.FILM_ID " +
                "GROUP BY F.FILM_ID ORDER BY COUNT(UL.FILM_ID) DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
        for (Film film : films) {
            film.setGenres(getGenresSetForParticularFilm(film.getId()));
        }
        return films;
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("film_id");
        String name = rs.getString("film_name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int rate = rs.getInt("rate");
        MPA mpa = new MPA(rs.getInt("mpa"), rs.getString("mpa_name"));
        return new Film(id, name, description, releaseDate, duration, rate, mpa);
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        int id = rs.getInt("genre_id");
        String name = rs.getString("genre_name");
        return new Genre(id, name);
    }

    private Set<Genre> getGenresSetForParticularFilm(Integer filmId) {
        String sql = "SELECT G.GENRE_ID, G.GENRE_NAME FROM FILM_GENRES AS FG, GENRES AS G " +
                "WHERE FG.GENRE_ID = G.GENRE_ID AND FILM_ID = ?";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs), filmId));
    }
}

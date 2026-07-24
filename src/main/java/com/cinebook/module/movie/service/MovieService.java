package com.cinebook.module.movie.service;

import com.cinebook.common.util.CursorCodec;
import com.cinebook.module.movie.repository.GenreRepository;
import com.cinebook.module.movie.repository.MovieGenreRepository;
import com.cinebook.module.movie.repository.MovieQueryRepository;
import com.cinebook.module.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieQueryRepository movieQueryRepository;
    private final GenreRepository genreRepository;
    private final CursorCodec cursorCodec;
}

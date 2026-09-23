package com.luismiguel.java.url_shortener.application.shortURL;

import com.luismiguel.java.url_shortener.domain.shortURL.ShortURL;
import com.luismiguel.java.url_shortener.infrastructure.codeGenerator.CodeGenerator;
import com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL.ShortURLNotFoundException;
import com.luismiguel.java.url_shortener.infrastructure.persistence.shortURL.ShortURLRepository;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.ShortenedURLResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShortURLServiceImpl implements ShortURLService {
    private final ShortURLRepository shortURLRepository;
    private final CodeGenerator codeGenerator;

    @Override
    @Transactional
    public ShortenedURLResponseDTO shortenURL(String url) {
        ShortURL shortURL = ShortURL.create(url);
        ShortURL savedShortURL = shortURLRepository.save(shortURL);
        String shortCode = codeGenerator.generate(savedShortURL.getId());
        savedShortURL.assignShortCode(shortCode);
        shortURLRepository.save(savedShortURL);

        return new ShortenedURLResponseDTO(
                shortCode,
                url
        );
    }

    @Override
    public String getOriginalURL(String shortCode) {
        ShortURL shortURL = shortURLRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortURLNotFoundException(shortCode));

        return shortURL.getOriginalUrl();
    }
}

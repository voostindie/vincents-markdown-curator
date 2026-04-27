package nl.ulso.vmc.personal.writing;

import java.util.stream.Stream;

public interface ArticleRepository
{
    Stream<Article> articles();
}

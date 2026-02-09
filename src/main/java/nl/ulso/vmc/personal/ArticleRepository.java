package nl.ulso.vmc.personal;

import java.util.stream.Stream;

public interface ArticleRepository
{
    Stream<Article> articles();
}

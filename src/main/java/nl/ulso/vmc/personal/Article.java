package nl.ulso.vmc.personal;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public record Article(String title, Optional<LocalDate> date)
    implements Comparable<Article>
{
    public String link()
    {
        return "[[" + title + "]]";
    }

    public String dateLink()
    {
        return date.map(d -> "[[" + d + "]]").orElse("");
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Article article = (Article) o;
        return Objects.equals(title, article.title) &&
               Objects.equals(date, article.date);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(title);
    }

    @Override
    public int compareTo(Article o)
    {
        if (date.isPresent() && o.date.isPresent())
        {
            return date.get().compareTo(o.date.get());
        }
        else if (date.isPresent())
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }
}

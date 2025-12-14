package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.markdown_curator.vault.Document;

import java.math.BigDecimal;

public record TariffGroup(Document document, BigDecimal tariff)
{
    String name()
    {
        return document.name();
    }

    String link()
    {
        return document.link();
    }
}

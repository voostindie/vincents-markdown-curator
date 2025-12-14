package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.markdown_curator.vault.Document;

import java.math.BigDecimal;

public record TrainingGroup(Document document, TariffGroup tariffGroup, BigDecimal practicesPerWeek)
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

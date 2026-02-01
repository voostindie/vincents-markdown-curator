package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.curator.vault.Document;

import java.math.BigDecimal;

/**
 * Represents a training group in a specific season.
 *
 * @param document         Document representing the training group.
 * @param tariffGroup      Tariff group that applies to the training group in a specific season.
 * @param practicesPerWeek The number of practices per week for the training group in a specific
 *                         season.
 */
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

    BigDecimal computeTariff()
    {
        return tariffGroup.tariff().multiply(practicesPerWeek);
    }
}

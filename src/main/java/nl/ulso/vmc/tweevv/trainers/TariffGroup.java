package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.curator.vault.Document;

import java.math.BigDecimal;

/// Represents a tariff group that can be applied to a training group in a season; the tariff
/// applies to 1 practice week and should therefore be multiplied by the number of practices a
/// training group has to get the tariff for that group.
///
/// @param document Document representing the tariff group.
/// @param tariff   The tariff that applies to this group in a specific season.
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

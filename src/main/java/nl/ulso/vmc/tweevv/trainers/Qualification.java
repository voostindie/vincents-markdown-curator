package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.curator.vault.Document;

import java.math.BigDecimal;

/// Represents a qualification a trainer can have in a specific season; the allowance is rewarded to
/// the trainer no matter what.
///
/// @param document  Document representing the qualification.
/// @param allowance The allowance, in Euros, for the qualification in the active season.
public record Qualification(Document document, BigDecimal allowance)
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

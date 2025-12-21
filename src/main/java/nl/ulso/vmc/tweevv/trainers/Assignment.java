package nl.ulso.vmc.tweevv.trainers;

import java.math.BigDecimal;

/**
 * Assignment of a {@link Trainer} to a {@link TrainingGroup} in a specific season.
 *
 * @param trainingGroup Group the trainer is assigned to.
 * @param factor        Assignment factor; 1.0 if the trainer is doing it alone.
 */
public record Assignment(TrainingGroup trainingGroup, BigDecimal factor)
{
    BigDecimal computeCompensation()
    {
        return trainingGroup.computeTariff().multiply(factor);
    }
}

package nl.ulso.vmc.tweevv.trainers;

import java.math.BigDecimal;

/// Assignment of a [Trainer] to a [TrainingGroup] in a specific season.
///
/// @param trainingGroup Group the trainer is assigned to.
/// @param factor        Assignment factor; should be 1.0 if the trainer is the sole assignee,
///                      otherwise it should be a fraction, e.g. 0.5 for 50%.
public record Assignment(TrainingGroup trainingGroup, BigDecimal factor)
{
    BigDecimal computeCompensation()
    {
        return trainingGroup.computeTariff().multiply(factor);
    }
}

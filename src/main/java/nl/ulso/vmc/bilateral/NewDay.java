package nl.ulso.vmc.bilateral;

import nl.ulso.curator.change.Change;

/// Event that is triggered once a day, as close to midnight as possible, when the machine is
/// active.
record NewDay()
{
    static Change<NewDay> NEW_DAY = Change.create(new NewDay(), NewDay.class);
}

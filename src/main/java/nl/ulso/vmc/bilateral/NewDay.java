package nl.ulso.vmc.bilateral;

import nl.ulso.curator.change.Change;

import static nl.ulso.curator.change.Change.create;

/// Event that is triggered once a day, as close to midnight as possible, when the machine is
/// active.
record NewDay()
{
    static final Change<NewDay> NEW_DAY = create(new NewDay(), NewDay.class);

    @Override
    public String toString()
    {
        return "⏰";
    }
}

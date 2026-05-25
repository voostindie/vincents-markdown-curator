package nl.ulso.vmc.bilateral;

import nl.ulso.curator.change.Change;

record Morning()
{
    static Change<Morning> MORNING = Change.create(new Morning(), Morning.class);
}

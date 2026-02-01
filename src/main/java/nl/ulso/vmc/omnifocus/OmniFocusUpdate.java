package nl.ulso.vmc.omnifocus;

import nl.ulso.curator.changelog.Change;
import nl.ulso.curator.changelog.ChangeProcessor;

import static nl.ulso.curator.changelog.Change.update;

/// Represents a change in OmniFocus.
///
/// The [OmniFocusRepository] produces an event of this type whenever a relevant change in OmniFocus
/// is detected. This repository is not a [ChangeProcessor] however, instead it runs autonomously on
/// its own scheduled thread.
///
/// The [OmniFocusAttributeProducer] triggers on changes of this type and produces attribute values
/// for all available projects.
///
/// The [OmniFocusInitializer] ensures the projects are loaded from OmniFocus at applications
/// startup.
record OmniFocusUpdate()
{
    static final Change<?> OMNIFOCUS_CHANGE =
        update(new OmniFocusUpdate(), OmniFocusUpdate.class);

    @Override
    public String toString()
    {
        return "OmniFocus";
    }
}

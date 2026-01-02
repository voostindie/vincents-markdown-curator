package nl.ulso.vmc.omnifocus;

import nl.ulso.markdown_curator.*;

import static nl.ulso.markdown_curator.Change.update;

/// Represents a change in OmniFocus.
///
/// The [OmniFocusRepository] produces an event of this type whenever a relevant change in OmniFocus
/// is detected. This repository is not a [ChangeProcessor] however, instead it runs autonomously on
/// its own scheduled thread. This class is registered as an [ExternalChangeObjectType] to the
/// system.
///
/// The [OmniFocusAttributeProducer] triggers on changes of this type and produces attribute values
/// for all available projects.
record OmniFocusUpdate()
{
    static final Change<?> OMNIFOCUS_CHANGE =
        update(new OmniFocusUpdate(), OmniFocusUpdate.class);
}

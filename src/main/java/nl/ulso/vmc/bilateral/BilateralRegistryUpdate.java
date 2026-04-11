package nl.ulso.vmc.bilateral;

import nl.ulso.curator.change.Change;

record BilateralRegistryUpdate()
{
    static final Change<?> BILATERAL_REGISTRY_UPDATE =
        Change.update(new BilateralRegistryUpdate(), BilateralRegistryUpdate.class);
}

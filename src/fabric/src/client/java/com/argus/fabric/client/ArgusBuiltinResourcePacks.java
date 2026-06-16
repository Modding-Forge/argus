package com.argus.fabric.client;

import com.argus.Constants;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers Argus-provided optional client resource packs.
 *
 * <p>Like Continuity, Argus keeps built-in pack content as ordinary files under
 * {@code src/fabric/src/client/resources/resourcepacks/default_ctm_glass} and only
 * registers that folder through Fabric's resource-loader API. Pack content is
 * never assembled from Java strings.
 *
 * <p>Threading: called once from the Fabric client entrypoint during client
 * bootstrap.
 *
 * <p>Compatibility: the pack is optional and uses normal resource-pack stack
 * ordering. Users can enable, disable, or override it exactly like any other
 * built-in client resource pack.
 */
public final class ArgusBuiltinResourcePacks {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Constants.MOD_ID + "/builtin-packs");

    private static final Identifier DEFAULT =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "default");
    private static final String DEFAULT_SUBPATH = "resourcepacks/default_ctm_glass";

    private ArgusBuiltinResourcePacks() {
    }

    /**
     * Registers all built-in Argus client resource packs.
     *
     * <p>Side effects: adds pack metadata to Fabric's resource-pack registry.
     * Missing mod metadata is logged and treated as non-fatal.
     */
    @SuppressWarnings("deprecation")
    public static void register() {
        ModContainer container = FabricLoader.getInstance()
                .getModContainer(Constants.MOD_ID)
                .orElse(null);
        if (container == null) {
            LOGGER.warn("[{}] cannot register built-in packs: "
                            + "mod container not found",
                    Constants.MOD_NAME);
            return;
        }

        boolean registered = ResourceManagerHelper.registerBuiltinResourcePack(
                DEFAULT,
                DEFAULT_SUBPATH,
                container,
                false);
        if (registered) {
            LOGGER.info("[{}] registered built-in resource pack {}",
                    Constants.MOD_NAME, DEFAULT);
        } else {
            LOGGER.warn("[{}] built-in resource pack {} was not registered",
                    Constants.MOD_NAME, DEFAULT);
        }
    }
}

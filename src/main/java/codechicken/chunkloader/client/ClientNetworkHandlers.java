package codechicken.chunkloader.client;

import codechicken.chunkloader.gui.GuiChunkLoader;
import codechicken.chunkloader.tile.TileChunkLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-side packet handler bodies. These touch client-only classes (Minecraft, Screen), so they must
 * live in a class a dedicated server never loads: on 26.3 the verifier loads classes referenced by
 * subtype checks while verifying the whole class, and {@code @OnlyIn} members are no longer stripped.
 * ChickenChunksNetwork reaches them through explicit lambdas, which only resolve this class when run.
 */
public class ClientNetworkHandlers {

    public static void handleOpenLoaderGui(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        if (ctx.player().level().getBlockEntity(packet.readBlockPos()) instanceof TileChunkLoader tile) {
            Minecraft.getInstance().gui.setScreen(new GuiChunkLoader(tile));
        }
    }

    public static void handleAddGuiWarning(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        if (Minecraft.getInstance().gui.screen() instanceof GuiChunkLoader gui) {
            gui.addWarning(packet.cc$readWithRegistryCodec(ComponentSerialization.STREAM_CODEC));
        }
    }
}

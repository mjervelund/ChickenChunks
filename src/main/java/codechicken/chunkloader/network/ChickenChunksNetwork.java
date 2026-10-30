package codechicken.chunkloader.network;

import codechicken.chunkloader.api.ChunkLoaderShape;
import codechicken.chunkloader.client.ClientNetworkHandlers;
import codechicken.chunkloader.tile.TileChunkLoader;
import codechicken.chunkloader.tile.TileChunkLoaderBase;
import codechicken.lib.packet.StreamNetworkChannel;
import codechicken.lib.packet.StreamNetworkChannel.ClientPacketHandle;
import codechicken.lib.packet.StreamNetworkChannel.ServerPacketHandle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static codechicken.chunkloader.ChickenChunks.MOD_ID;

/**
 * Created by covers1624 on 1/11/19.
 */
public class ChickenChunksNetwork {

    public static final StreamNetworkChannel CHANNEL = new StreamNetworkChannel(MOD_ID);

    // The GUI handlers live in client.ClientNetworkHandlers behind explicit lambdas, so a dedicated
    // server never loads a Screen subclass (see that class's doc comment).
    public static final ClientPacketHandle OPEN_LOADER_GUI = CHANNEL.playToClient("open_loader", (packet, ctx) -> ClientNetworkHandlers.handleOpenLoaderGui(packet, ctx));
    public static final ClientPacketHandle TILE_UPDATE = CHANNEL.playToClient("tile_update", ChickenChunksNetwork::handleTileUpdate);
    public static final ClientPacketHandle ADD_GUI_WARNING = CHANNEL.playToClient("gui_warning", (packet, ctx) -> ClientNetworkHandlers.handleAddGuiWarning(packet, ctx));

    public static final ServerPacketHandle SET_SHAPE = CHANNEL.playToServer("set_shape", ChickenChunksNetwork::handleSetShape);

    public static void init(IEventBus modBus, ModContainer container) {
        CHANNEL.init(modBus, container);
    }

    public static void sendTileUpdate(TileChunkLoaderBase tile) {
        if (tile.world().isClientSide()) return;

        var packet = TILE_UPDATE.toClient(tile);
        packet.writeBlockPos(tile.getBlockPos());
        tile.writeToPacket(packet);
        packet.sendToChunk(tile);
    }

    private static void handleTileUpdate(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        if (ctx.player().level().getBlockEntity(packet.readBlockPos()) instanceof TileChunkLoaderBase chunkLoader) {
            chunkLoader.readFromPacket(packet);
        }
    }

    public static void sendAddGuiWarning(ServerPlayer player, Component component) {
        var packet = ADD_GUI_WARNING.toClient(player);
        packet.cc$writeWithRegistryCodec(ComponentSerialization.STREAM_CODEC, component);
        packet.sendToPlayer(player);
    }

    public static void sendSetShape(TileChunkLoader tile, ChunkLoaderShape shape, int radius) {
        if (radius < 0 || radius > 255) return;

        var packet = SET_SHAPE.toServer();
        packet.writeBlockPos(tile.getBlockPos());
        packet.writeEnum(shape);
        packet.writeShort(radius);
        packet.sendToServer();
    }

    private static void handleSetShape(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        if (ctx.player().level().getBlockEntity(packet.readBlockPos()) instanceof TileChunkLoader tile) {
            tile.setShapeAndRadius((ServerPlayer) ctx.player(), packet.readEnum(ChunkLoaderShape.class), packet.readShort());
        }
    }
}

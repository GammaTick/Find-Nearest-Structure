package net.findneareststructure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.structure.Structure;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FindNearestStructureCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("findneareststructure").requires(source -> source.hasPermissionLevel(2))
                .executes(context -> findNearestStructure(context, 50))
                .then(argument("radius", IntegerArgumentType.integer(0))
                        .executes(context -> findNearestStructure(context, IntegerArgumentType.getInteger(context, "radius"))))));
    }

    private static int findNearestStructure(CommandContext<ServerCommandSource> context, int radius) {
        BlockPos pos = locateNearestStructure(context, context.getSource().getWorld(), radius);
        if (pos == null) {
            context.getSource().sendError(Text.of("Could not find a structure in the given radius"));
            return 0;
        }
            BlockPos startPos = context.getSource().getEntity().getBlockPos();
            MutableText coordinates = Texts.bracketed(Text.translatable("%s, ~, %s", pos.getX(), pos.getZ()).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + startPos.getY() + " " + pos.getZ()))));
            context.getSource().sendFeedback(() -> Text.translatable("The nearest structure is at %s (%s blocks away)", coordinates, distanceBetween(startPos, new BlockPos(pos.getX(), startPos.getY(), pos.getZ()))), false);
        return 1;
    }

    private static BlockPos locateNearestStructure(CommandContext<ServerCommandSource> context, World world, int radius) {
        double d = Double.MAX_VALUE;
        ChunkPos startChunkPos = context.getSource().getEntity().getChunkPos();
        ChunkPos currentChunkPos;
        BlockPos pos = null;
        for (int i = 0; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = -radius; k <= radius; ++k) {
                    currentChunkPos = new ChunkPos(startChunkPos.x + j, startChunkPos.z + k);
                    if (chunkHasStructure(currentChunkPos, world)) {
                        double f = startChunkPos.getSquaredDistance(currentChunkPos);
                        if (!(f < d)) {
                            continue;
                        }
                        d = f;
                        pos = world.getChunk(currentChunkPos.x, currentChunkPos.z).getPos().getStartPos();
                    }
                }
            }
        }
        return pos;
    }

    private static boolean chunkHasStructure(ChunkPos chunkpos, World world) {
        Chunk chunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
        Map<Structure, StructureStart> structure = chunk.getStructureStarts();
        for (Map.Entry<Structure, StructureStart> entry : structure.entrySet()) {
            StructureStart structureStart = entry.getValue();
            if (structureStart != null && structureStart.hasChildren()) {
                return true;
            }
        }
        return false;
    }

    private static int distanceBetween(BlockPos pos1, BlockPos pos2) {
        int x = Math.abs(pos1.getX() - pos2.getX());
        int y = Math.abs(pos1.getY() - pos2.getY());
        int z = Math.abs(pos1.getZ() - pos2.getZ());
        return x + y + z;
    }
}

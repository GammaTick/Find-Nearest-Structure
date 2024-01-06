package net.findneareststructure;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

import static net.minecraft.server.command.CommandManager.literal;

public class FindNearestStructureCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("findneareststructure").requires(source -> source.hasPermissionLevel(2))
                .executes(FindNearestStructureCommand::findNearestStructure)));
    }

    private static int findNearestStructure(CommandContext<ServerCommandSource> context) {
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        Pair<BlockPos, RegistryEntry<Structure>> pair = locateStructure(context.getSource().getWorld(), BlockPos.ofFloored(context.getSource().getPosition()), 100);
        stopwatch.stop();
        if (pair == null) {
            return 0;
        }
        return 1;
    }

    public static Pair<BlockPos, RegistryEntry<Structure>> locateStructure(ServerWorld world, BlockPos center, int radius) {
        Pair<BlockPos, RegistryEntry<Structure>> pair = null;

        return pair;
    }
}

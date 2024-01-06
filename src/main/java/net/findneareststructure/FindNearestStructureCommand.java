package net.findneareststructure;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.gen.structure.Structure;

import static net.minecraft.server.command.CommandManager.literal;

public class FindNearestStructureCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("findneareststructure").requires(source -> source.hasPermissionLevel(2))
                .executes(FindNearestStructureCommand::findNearestStructure)));
    }

    private static int findNearestStructure(CommandContext<ServerCommandSource> context) {
        Registry<Structure> allStructuresRegistry = context.getSource().getWorld().getRegistryManager().get(RegistryKeys.STRUCTURE);
        for (Structure structure: allStructuresRegistry) {
            context.getSource().sendFeedback(() -> Text.of(String.valueOf(structure.getStructureSpawns())), false);
        }
        return 1;
    }
}

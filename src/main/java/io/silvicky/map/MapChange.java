package io.silvicky.map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.MapUtility;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.datafixer.fix.MapIdFix;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.item.map.MapBannerMarker;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.IdCountsState;

import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.component.DataComponentTypes.MAP_ID;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MapChange {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(
                literal("mapchange")
                        .then(argument("ID", IntegerArgumentType.integer())
                                .executes(context -> mapChange(context.getSource(),IntegerArgumentType.getInteger(context,"ID")))));
    }
    public static int mapChange(ServerCommandSource source, int id)
    {
        IdCountsState idCountsState= source.getServer().getOverworld().getPersistentStateManager().getOrCreate(IdCountsState.getPersistentStateType(), "idcounts");
        NbtCompound nbtCompound=new NbtCompound();
        idCountsState.writeNbt(nbtCompound,source.getRegistryManager());
        int maxId=nbtCompound.getInt("map");
        if(id<0||id>maxId)
        {
            source.sendFeedback(()-> Text.literal("ERR:Invalid ID!"),false);
            return Command.SINGLE_SUCCESS;
        }
        PlayerInventory inventory=source.getPlayer().getInventory();
        if(inventory==null)
        {
            source.sendFeedback(()-> Text.literal("ERR:Empty inventory!"),false);
            return Command.SINGLE_SUCCESS;
        }
        ItemStack itemStack=inventory.getMainHandStack();
        if(itemStack!=null)
        {
            if(itemStack.getCount()>1)
            {
                source.sendFeedback(()-> Text.literal("ERR:Please hold only one!"),false);
                return Command.SINGLE_SUCCESS;
            }
            String curItem=itemStack.getItem().getTranslationKey();
            if(curItem.equals("item.minecraft.filled_map"))
            {
                itemStack.applyChanges(ComponentChanges.builder().remove(MAP_ID).add(Component.of(MAP_ID,new MapIdComponent(id))).build());
                source.getPlayer().getInventory().setStack(inventory.selectedSlot,itemStack);
            }
            else if(curItem.equals("item.minecraft.map"))
            {

                ItemStack itemStack1=new ItemStack(Items.FILLED_MAP);
                itemStack1.applyChanges(ComponentChanges.builder().add(Component.of(MAP_ID,new MapIdComponent(id))).build());
                source.getPlayer().getInventory().setStack(inventory.selectedSlot,itemStack1);
            }
            else
            {
                source.sendFeedback(()-> Text.literal("ERR:Only maps are allowed!"),false);
                return Command.SINGLE_SUCCESS;
            }
        }
        else
        {
            source.sendFeedback(()-> Text.literal("ERR:Only maps are allowed!"),false);
            return Command.SINGLE_SUCCESS;
        }
        return Command.SINGLE_SUCCESS;
    }
}

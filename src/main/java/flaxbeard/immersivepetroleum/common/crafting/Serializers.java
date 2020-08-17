package flaxbeard.immersivepetroleum.common.crafting;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.common.crafting.serializers.DistillationRecipeSerializer;
import flaxbeard.immersivepetroleum.common.crafting.serializers.ReservoirTypeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Serializers{
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, ImmersivePetroleum.MODID);
	
	public static void construct(){
		DistillationRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("distillation", DistillationRecipeSerializer::new);
		PumpjackHandler.ReservoirType.SERIALIZER = RECIPE_SERIALIZERS.register("reservoirs", ReservoirTypeSerializer::new);
		SchematicCraftingHandler.SERIALIZER = RECIPE_SERIALIZERS.register("schematic_crafting", () -> new SpecialRecipeSerializer<>(SchematicCraftingHandler::new));
	}
}

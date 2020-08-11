package flaxbeard.immersivepetroleum.common.crafting;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.crafting.serializers.DistillationRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Serializers{
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, ImmersivePetroleum.MODID);
	
	static{
		DistillationRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("distillation", DistillationRecipeSerializer::new);
		SchematicCraftingHandler.SERIALIZER = RECIPE_SERIALIZERS.register("schematic_crafting", () -> new SpecialRecipeSerializer<>(SchematicCraftingHandler::new));
	}
}

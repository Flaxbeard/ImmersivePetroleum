package flaxbeard.immersivepetroleum.client.model;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.models.ModelCoresample;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_Dummy;

public class ModelCoresampleExtended extends ModelCoresample
{
	
	static HashMap<String, ModelCoresample> modelCache = new HashMap();

	
	@Override
	public ItemOverrideList getOverrides()
	{
		return overrideList;
	}

	private static final String original = "original";
	ItemOverrideList overrideList = new ItemOverrideList(new ArrayList())
	{
		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
		{
			boolean hasOil = ItemNBTHelper.hasKey(stack, "oil") && ItemNBTHelper.getInt(stack, "oil") > 0;

			if(ItemNBTHelper.hasKey(stack, "mineral"))
			{
				String name = ItemNBTHelper.getString(stack, "mineral");
				String indexName = hasOil ? name + "_oil" : name;
				if(name != null && !name.isEmpty())
				{
					if(!modelCache.containsKey(indexName))
						for(MineralMix mix : ExcavatorHandler.mineralList.keySet())
							if(name.equals(mix.name))
							{
								if (hasOil)
								{
									String[] newOres = new String[mix.ores.length + 1];
									float[] newChances = new float[mix.chances.length + 1];
									newOres[mix.ores.length] = "obsidian";
									newChances[mix.ores.length] = 0.4f;
									for (int i = 0; i < mix.ores.length; i++)
									{
										newOres[i] = mix.ores[i];
										newChances[i] = mix.chances[i];
									}
									MineralMix mix2 = new MineralMix(mix.name, mix.failChance, newOres, newChances);
									mix2.recalculateChances();
									mix2.oreOutput[mix2.oreOutput.length - 1] = new ItemStack(IPContent.blockDummy, 1, BlockTypes_Dummy.OIL_DEPOSIT.getMeta());
									
									modelCache.put(indexName, new ModelCoresample(mix2));

								}
								else
								{
									modelCache.put(indexName, new ModelCoresample(mix));
								}
							}
					IBakedModel model = modelCache.get(indexName);
					if(model != null)
						return model;
				}
			}
			
			if (hasOil)
			{
				if (!modelCache.containsKey(original))
				{
					MineralMix mix = new MineralMix(original, 1, new String[] { "obsidian", "stone" }, new float[] { 0.6F, 0.4F } );
					mix.recalculateChances();
					mix.oreOutput[0] = new ItemStack(IPContent.blockDummy, 1, BlockTypes_Dummy.OIL_DEPOSIT.getMeta());
					modelCache.put(original, new ModelCoresample(mix));
				}
				
				IBakedModel model = modelCache.get(original);
				if(model != null)
					return model;
			} 

	
			return originalModel;
		}
	};

}

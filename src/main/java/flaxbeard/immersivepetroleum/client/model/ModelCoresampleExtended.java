package flaxbeard.immersivepetroleum.client.model;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelCoresample;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.Lists;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_Dummy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.*;

public class ModelCoresampleExtended extends ModelCoresample
{
	private Fluid fluid;

	Set<BakedQuad> bakedQuads;
	static List<BakedQuad> emptyQuads = Lists.newArrayList();
	MineralMix mineral;

	public ModelCoresampleExtended(MineralMix mineral, Fluid fluid)
	{
		super(mineral);
		this.mineral = mineral;
		this.fluid = fluid;
	}

	public ModelCoresampleExtended(MineralMix mineral)
	{
		this(mineral, null);
	}

	public ModelCoresampleExtended()
	{
		this(null);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState blockState, @Nullable EnumFacing side, long rand)
	{
		if (bakedQuads == null)
		{
			try
			{
				bakedQuads = Collections.synchronizedSet(new LinkedHashSet<BakedQuad>());
				float width = .25f;
				float depth = .25f;
				float wOff = (1 - width) / 2;
				float dOff = (1 - depth) / 2;

				float fWidth = .24f;
				float fDepth = .24f;
				float fWOff = (1 - fWidth) / 2;
				float fDOff = (1 - fDepth) / 2;

				int pixelLength = 0;

				HashMap<TextureAtlasSprite, Integer> textureOre = new HashMap();
				if (mineral != null && mineral.oreOutput != null)
				{
					for (int i = 0; i < mineral.oreOutput.size(); i++)
					{
						if (!mineral.oreOutput.get(i).isEmpty())
						{
							int weight = Math.max(2, Math.round(16 * mineral.recalculatedChances[i]));
							Block b = Block.getBlockFromItem(mineral.oreOutput.get(i).getItem());
							IBlockState state = b != null ? b.getStateFromMeta(mineral.oreOutput.get(i).getMetadata()) : Blocks.STONE.getDefaultState();

							IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
							if (b == IPContent.blockDummy)
							{
								textureOre.put(null, weight);
							}
							else if (model != null && model.getParticleTexture() != null)
							{
								textureOre.put(model.getParticleTexture(), weight);
							}
							pixelLength += weight;
						}
					}
				}
				else
					pixelLength = 16;
				TextureAtlasSprite textureStone = ClientUtils.getSprite(new ResourceLocation("blocks/stone"));

				Vector2f[] stoneUVs = {
						new Vector2f(textureStone.getInterpolatedU(16 * wOff), textureStone.getInterpolatedV(16 * dOff)),
						new Vector2f(textureStone.getInterpolatedU(16 * wOff), textureStone.getInterpolatedV(16 * (dOff + depth))),
						new Vector2f(textureStone.getInterpolatedU(16 * (wOff + width)), textureStone.getInterpolatedV(16 * (dOff + depth))),
						new Vector2f(textureStone.getInterpolatedU(16 * (wOff + width)), textureStone.getInterpolatedV(16 * dOff))};

				putVertexDataSpr(new Vector3f(0, -1, 0), new Vector3f[]{new Vector3f(wOff, 0, dOff), new Vector3f(wOff + width, 0, dOff), new Vector3f(wOff + width, 0, dOff + depth), new Vector3f(wOff, 0, dOff + depth)}, stoneUVs, textureStone);
				putVertexDataSpr(new Vector3f(0, 1, 0), new Vector3f[]{new Vector3f(wOff, 1, dOff), new Vector3f(wOff, 1, dOff + depth), new Vector3f(wOff + width, 1, dOff + depth), new Vector3f(wOff + width, 1, dOff)}, stoneUVs, textureStone);
				if (textureOre.isEmpty())
				{
					Vector2f[][] uvs = new Vector2f[4][];
					for (int j = 0; j < 4; j++)
					{
						uvs[j] = new Vector2f[]{
								new Vector2f(textureStone.getInterpolatedU(j * 4), textureStone.getInterpolatedV(0)),
								new Vector2f(textureStone.getInterpolatedU(j * 4), textureStone.getInterpolatedV(16)),
								new Vector2f(textureStone.getInterpolatedU((j + 1) * 4), textureStone.getInterpolatedV(16)),
								new Vector2f(textureStone.getInterpolatedU((j + 1) * 4), textureStone.getInterpolatedV(0))};
					}

					putVertexDataSpr(new Vector3f(0, 0, -1), new Vector3f[]{new Vector3f(wOff, 0, dOff), new Vector3f(wOff, 1, dOff), new Vector3f(wOff + width, 1, dOff), new Vector3f(wOff + width, 0, dOff)}, uvs[0], textureStone);
					putVertexDataSpr(new Vector3f(0, 0, 1), new Vector3f[]{new Vector3f(wOff + width, 0, dOff + depth), new Vector3f(wOff + width, 1, dOff + depth), new Vector3f(wOff, 1, dOff + depth), new Vector3f(wOff, 0, dOff + depth)}, uvs[2], textureStone);
					putVertexDataSpr(new Vector3f(-1, 0, 0), new Vector3f[]{new Vector3f(wOff, 0, dOff + depth), new Vector3f(wOff, 1, dOff + depth), new Vector3f(wOff, 1, dOff), new Vector3f(wOff, 0, dOff)}, uvs[3], textureStone);
					putVertexDataSpr(new Vector3f(1, 0, 0), new Vector3f[]{new Vector3f(wOff + width, 0, dOff), new Vector3f(wOff + width, 1, dOff), new Vector3f(wOff + width, 1, dOff + depth), new Vector3f(wOff + width, 0, dOff + depth)}, uvs[1], textureStone);
				}
				else
				{
					float h = 0;
					for (TextureAtlasSprite sprite : textureOre.keySet())
					{
						int weight = textureOre.get(sprite);
						int v = weight > 8 ? 16 - weight : 8;

						if (sprite == null)
						{
							TextureAtlasSprite fSprite = null;

							if (fluid != null)
							{
								fSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
							}

							if (fSprite != null)
							{
								Vector2f[][] uvs = new Vector2f[4][];
								for (int j = 0; j < 4; j++)
								{
									uvs[j] = new Vector2f[]{
											new Vector2f(fSprite.getInterpolatedU(j * 4), fSprite.getInterpolatedV(v)),
											new Vector2f(fSprite.getInterpolatedU(j * 4), fSprite.getInterpolatedV(v + weight)),
											new Vector2f(fSprite.getInterpolatedU((j + 1) * 4), fSprite.getInterpolatedV(v + weight)),
											new Vector2f(fSprite.getInterpolatedU((j + 1) * 4), fSprite.getInterpolatedV(v))};
								}

								float h1 = weight / (float) pixelLength;
								putVertexDataSpr(new Vector3f(0, 0, -1), new Vector3f[]{new Vector3f(fWOff, h, fDOff), new Vector3f(fWOff, h + h1, fDOff), new Vector3f(fWOff + fWidth, h + h1, fDOff), new Vector3f(fWOff + fWidth, h, fDOff)}, uvs[0], fSprite);
								putVertexDataSpr(new Vector3f(0, 0, 1), new Vector3f[]{new Vector3f(fWOff + fWidth, h, fDOff + fDepth), new Vector3f(fWOff + fWidth, h + h1, fDOff + fDepth), new Vector3f(fWOff, h + h1, fDOff + fDepth), new Vector3f(fWOff, h, fDOff + fDepth)}, uvs[2], fSprite);
								putVertexDataSpr(new Vector3f(-1, 0, 0), new Vector3f[]{new Vector3f(fWOff, h, fDOff + fDepth), new Vector3f(fWOff, h + h1, fDOff + fDepth), new Vector3f(fWOff, h + h1, fDOff), new Vector3f(fWOff, h, fDOff)}, uvs[3], fSprite);
								putVertexDataSpr(new Vector3f(1, 0, 0), new Vector3f[]{new Vector3f(fWOff + fWidth, h, fDOff), new Vector3f(fWOff + fWidth, h + h1, fDOff), new Vector3f(fWOff + fWidth, h + h1, fDOff + fDepth), new Vector3f(fWOff + fWidth, h, fDOff + fDepth)}, uvs[1], fSprite);
							}

							IBlockState state = IPContent.blockDummy.getStateFromMeta(BlockTypes_Dummy.OIL_DEPOSIT.getMeta());
							IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
							sprite = model.getParticleTexture();

						}
						Vector2f[][] uvs = new Vector2f[4][];
						for (int j = 0; j < 4; j++)
						{
							uvs[j] = new Vector2f[]{
									new Vector2f(sprite.getInterpolatedU(j * 4), sprite.getInterpolatedV(v)),
									new Vector2f(sprite.getInterpolatedU(j * 4), sprite.getInterpolatedV(v + weight)),
									new Vector2f(sprite.getInterpolatedU((j + 1) * 4), sprite.getInterpolatedV(v + weight)),
									new Vector2f(sprite.getInterpolatedU((j + 1) * 4), sprite.getInterpolatedV(v))};
						}

						float h1 = weight / (float) pixelLength;
						putVertexDataSpr(new Vector3f(0, 0, -1), new Vector3f[]{new Vector3f(wOff, h, dOff), new Vector3f(wOff, h + h1, dOff), new Vector3f(wOff + width, h + h1, dOff), new Vector3f(wOff + width, h, dOff)}, uvs[0], sprite);
						putVertexDataSpr(new Vector3f(0, 0, 1), new Vector3f[]{new Vector3f(wOff + width, h, dOff + depth), new Vector3f(wOff + width, h + h1, dOff + depth), new Vector3f(wOff, h + h1, dOff + depth), new Vector3f(wOff, h, dOff + depth)}, uvs[2], sprite);
						putVertexDataSpr(new Vector3f(-1, 0, 0), new Vector3f[]{new Vector3f(wOff, h, dOff + depth), new Vector3f(wOff, h + h1, dOff + depth), new Vector3f(wOff, h + h1, dOff), new Vector3f(wOff, h, dOff)}, uvs[3], sprite);
						putVertexDataSpr(new Vector3f(1, 0, 0), new Vector3f[]{new Vector3f(wOff + width, h, dOff), new Vector3f(wOff + width, h + h1, dOff), new Vector3f(wOff + width, h + h1, dOff + depth), new Vector3f(wOff + width, h, dOff + depth)}, uvs[1], sprite);
						h += h1;
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (bakedQuads != null && !bakedQuads.isEmpty())
		{
			List<BakedQuad> quadList = Collections.synchronizedList(Lists.newArrayList(bakedQuads));
			return quadList;
		}
		return emptyQuads;
	}

	protected final void putVertexDataSpr(Vector3f normal, Vector3f[] vertices, Vector2f[] uvs, TextureAtlasSprite sprite)
	{
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
		builder.setQuadOrientation(EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z));
		builder.setTexture(sprite);
//		builder.setQuadColored();
		for (int i = 0; i < vertices.length; i++)
		{
			builder.put(0, vertices[i].x, vertices[i].y, vertices[i].z, 1);//Pos
			float d = LightUtil.diffuseLight(normal.x, normal.y, normal.z);
			builder.put(1, d, d, d, 1);//Colour
			builder.put(2, uvs[i].x, uvs[i].y, 0, 1);//UV
			builder.put(3, normal.x, normal.y, normal.z, 0);//Normal
			builder.put(4);//padding
		}
		bakedQuads.add(builder.build());
	}

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
			String resName = ItemNBTHelper.hasKey(stack, "resType") ? ItemNBTHelper.getString(stack, "resType") : null;
			if (ItemNBTHelper.hasKey(stack, "oil") && resName == null && ItemNBTHelper.getInt(stack, "oil") > 0)
			{
				resName = "oil";
			}

			if (ItemNBTHelper.hasKey(stack, "mineral"))
			{
				String name = ItemNBTHelper.getString(stack, "mineral");
				String indexName = resName == null ? name : name + "_" + resName;
				if (name != null && !name.isEmpty())
				{
					if (!modelCache.containsKey(indexName))
					{
						outer:
						for (MineralMix mix : ExcavatorHandler.mineralList.keySet())
						{
							if (name.equals(mix.name))
							{
								if (resName != null)
								{
									for (ReservoirType type : PumpjackHandler.reservoirList.keySet())
									{
										if (resName.equals(type.name))
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
											mix2.oreOutput.set(mix2.oreOutput.size() - 1, new ItemStack(IPContent.blockDummy, 1, BlockTypes_Dummy.OIL_DEPOSIT.getMeta()));

											Fluid fluid = type.getFluid();
											modelCache.put(indexName, new ModelCoresampleExtended(mix2, fluid));
											break outer;
										}
									}

									modelCache.put(indexName, new ModelCoresample(mix));
								}
								else
								{
									modelCache.put(indexName, new ModelCoresample(mix));
								}
							}
						}
					}
					IBakedModel model = modelCache.get(indexName);
					if (model != null)
						return model;
				}
			}

			if (resName != null)
			{
				if (!modelCache.containsKey("_" + resName))
				{
					for (ReservoirType type : PumpjackHandler.reservoirList.keySet())
					{
						if (resName.equals(type.name))
						{
							MineralMix mix = new MineralMix(resName, 1, new String[]{"obsidian"}, new float[]{1F});
							mix.recalculateChances();
							mix.oreOutput.set(0, new ItemStack(IPContent.blockDummy, 1, BlockTypes_Dummy.OIL_DEPOSIT.getMeta()));
							Fluid fluid = FluidRegistry.getFluid(type.fluid);
							modelCache.put("_" + resName, new ModelCoresampleExtended(mix, fluid));
						}
					}
				}

				IBakedModel model = modelCache.get("_" + resName);
				if (model != null)
					return model;
			}


			return originalModel;
		}
	};

}

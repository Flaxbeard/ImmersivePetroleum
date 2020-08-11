package flaxbeard.immersivepetroleum.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelCoresample;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

public class ModelCoresampleExtended extends ModelCoresample
{
	private Fluid fluid;

	Set<BakedQuad> bakedQuads;
	static List<BakedQuad> emptyQuads = Lists.newArrayList();
	MineralMix mineral;

	public ModelCoresampleExtended(@Nullable MineralMix mineral, VertexFormat format, Fluid fluid){
		super(mineral, format);
		this.mineral = mineral;
		this.fluid = fluid;
	}
	
	@Override
	public List<BakedQuad> getQuads(BlockState coreState, Direction side, Random rand, IModelData extraData){
		if(bakedQuads == null){
			try{
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
				
				HashMap<TextureAtlasSprite, Integer> textureOre = new HashMap<>();
				if(mineral != null && mineral.outputs != null){
					for(int i = 0;i < mineral.outputs.length;i++){
						if(mineral.outputs[i]!=null){
							int weight = Math.max(2, Math.round(16 * mineral.failChance));
							Block b = Block.getBlockFromItem(mineral.outputs[i].getStack().getItem());
							BlockState state = b != null ? b.getDefaultState() : Blocks.STONE.getDefaultState();
							
							IForgeBakedModel model =  (IForgeBakedModel)Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
							if(b == IPContent.dummyBlockOilOre){
								textureOre.put(null, weight);
							}else if(model != null && model.getParticleTexture(null) != null){
								textureOre.put(model.getParticleTexture(null), weight);
							}
							pixelLength += weight;
						}
					}
				}else
					pixelLength = 16;
				TextureAtlasSprite textureStone = ClientUtils.getSprite(new ResourceLocation("blocks/stone"));
				
				Vector2f[] stoneUVs = {new Vector2f(textureStone.getInterpolatedU(16 * wOff), textureStone.getInterpolatedV(16 * dOff)), new Vector2f(textureStone.getInterpolatedU(16 * wOff), textureStone.getInterpolatedV(16 * (dOff + depth))), new Vector2f(textureStone.getInterpolatedU(16 * (wOff + width)), textureStone.getInterpolatedV(16 * (dOff + depth))), new Vector2f(textureStone.getInterpolatedU(16 * (wOff + width)), textureStone.getInterpolatedV(16 * dOff))};
				
				putVertexDataSpr(new Vector3f(0, -1, 0), new Vector3f[]{new Vector3f(wOff, 0, dOff), new Vector3f(wOff + width, 0, dOff), new Vector3f(wOff + width, 0, dOff + depth), new Vector3f(wOff, 0, dOff + depth)}, stoneUVs, textureStone);
				putVertexDataSpr(new Vector3f(0, 1, 0), new Vector3f[]{new Vector3f(wOff, 1, dOff), new Vector3f(wOff, 1, dOff + depth), new Vector3f(wOff + width, 1, dOff + depth), new Vector3f(wOff + width, 1, dOff)}, stoneUVs, textureStone);
				if(textureOre.isEmpty()){
					Vector2f[][] uvs = new Vector2f[4][];
					for(int j = 0;j < 4;j++){
						uvs[j] = new Vector2f[]{new Vector2f(textureStone.getInterpolatedU(j * 4), textureStone.getInterpolatedV(0)), new Vector2f(textureStone.getInterpolatedU(j * 4), textureStone.getInterpolatedV(16)), new Vector2f(textureStone.getInterpolatedU((j + 1) * 4), textureStone.getInterpolatedV(16)), new Vector2f(textureStone.getInterpolatedU((j + 1) * 4), textureStone.getInterpolatedV(0))};
					}
					
					putVertexDataSpr(new Vector3f(0, 0, -1), new Vector3f[]{new Vector3f(wOff, 0, dOff), new Vector3f(wOff, 1, dOff), new Vector3f(wOff + width, 1, dOff), new Vector3f(wOff + width, 0, dOff)}, uvs[0], textureStone);
					putVertexDataSpr(new Vector3f(0, 0, 1), new Vector3f[]{new Vector3f(wOff + width, 0, dOff + depth), new Vector3f(wOff + width, 1, dOff + depth), new Vector3f(wOff, 1, dOff + depth), new Vector3f(wOff, 0, dOff + depth)}, uvs[2], textureStone);
					putVertexDataSpr(new Vector3f(-1, 0, 0), new Vector3f[]{new Vector3f(wOff, 0, dOff + depth), new Vector3f(wOff, 1, dOff + depth), new Vector3f(wOff, 1, dOff), new Vector3f(wOff, 0, dOff)}, uvs[3], textureStone);
					putVertexDataSpr(new Vector3f(1, 0, 0), new Vector3f[]{new Vector3f(wOff + width, 0, dOff), new Vector3f(wOff + width, 1, dOff), new Vector3f(wOff + width, 1, dOff + depth), new Vector3f(wOff + width, 0, dOff + depth)}, uvs[1], textureStone);
				}else{
					float h = 0;
					for(TextureAtlasSprite sprite:textureOre.keySet()){
						int weight = textureOre.get(sprite);
						int v = weight > 8 ? 16 - weight : 8;
						
						if(sprite == null){
							TextureAtlasSprite fSprite = null;
							
							if(fluid != null){
								fSprite = Minecraft.getInstance().getTextureMap().getSprite(fluid.getRegistryName());
							}
							
							if(fSprite != null){
								Vector2f[][] uvs = new Vector2f[4][];
								for(int j = 0;j < 4;j++){
									uvs[j] = new Vector2f[]{new Vector2f(fSprite.getInterpolatedU(j * 4), fSprite.getInterpolatedV(v)), new Vector2f(fSprite.getInterpolatedU(j * 4), fSprite.getInterpolatedV(v + weight)), new Vector2f(fSprite.getInterpolatedU((j + 1) * 4), fSprite.getInterpolatedV(v + weight)), new Vector2f(fSprite.getInterpolatedU((j + 1) * 4), fSprite.getInterpolatedV(v))};
								}
								
								float h1 = weight / (float) pixelLength;
								putVertexDataSpr(new Vector3f(0, 0, -1), new Vector3f[]{new Vector3f(fWOff, h, fDOff), new Vector3f(fWOff, h + h1, fDOff), new Vector3f(fWOff + fWidth, h + h1, fDOff), new Vector3f(fWOff + fWidth, h, fDOff)}, uvs[0], fSprite);
								putVertexDataSpr(new Vector3f(0, 0, 1), new Vector3f[]{new Vector3f(fWOff + fWidth, h, fDOff + fDepth), new Vector3f(fWOff + fWidth, h + h1, fDOff + fDepth), new Vector3f(fWOff, h + h1, fDOff + fDepth), new Vector3f(fWOff, h, fDOff + fDepth)}, uvs[2], fSprite);
								putVertexDataSpr(new Vector3f(-1, 0, 0), new Vector3f[]{new Vector3f(fWOff, h, fDOff + fDepth), new Vector3f(fWOff, h + h1, fDOff + fDepth), new Vector3f(fWOff, h + h1, fDOff), new Vector3f(fWOff, h, fDOff)}, uvs[3], fSprite);
								putVertexDataSpr(new Vector3f(1, 0, 0), new Vector3f[]{new Vector3f(fWOff + fWidth, h, fDOff), new Vector3f(fWOff + fWidth, h + h1, fDOff), new Vector3f(fWOff + fWidth, h + h1, fDOff + fDepth), new Vector3f(fWOff + fWidth, h, fDOff + fDepth)}, uvs[1], fSprite);
							}
							
							// BlockTypes_Dummy.OIL_DEPOSIT
							BlockState state = IPContent.dummyBlockOilOre.getDefaultState();
							IForgeBakedModel model = (IForgeBakedModel)Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
							sprite = model.getParticleTexture(null);
							
						}
						Vector2f[][] uvs = new Vector2f[4][];
						for(int j = 0;j < 4;j++){
							uvs[j] = new Vector2f[]{new Vector2f(sprite.getInterpolatedU(j * 4), sprite.getInterpolatedV(v)), new Vector2f(sprite.getInterpolatedU(j * 4), sprite.getInterpolatedV(v + weight)), new Vector2f(sprite.getInterpolatedU((j + 1) * 4), sprite.getInterpolatedV(v + weight)), new Vector2f(sprite.getInterpolatedU((j + 1) * 4), sprite.getInterpolatedV(v))};
						}
						
						float h1 = weight / (float) pixelLength;
						putVertexDataSpr(new Vector3f(0, 0, -1), new Vector3f[]{new Vector3f(wOff, h, dOff), new Vector3f(wOff, h + h1, dOff), new Vector3f(wOff + width, h + h1, dOff), new Vector3f(wOff + width, h, dOff)}, uvs[0], sprite);
						putVertexDataSpr(new Vector3f(0, 0, 1), new Vector3f[]{new Vector3f(wOff + width, h, dOff + depth), new Vector3f(wOff + width, h + h1, dOff + depth), new Vector3f(wOff, h + h1, dOff + depth), new Vector3f(wOff, h, dOff + depth)}, uvs[2], sprite);
						putVertexDataSpr(new Vector3f(-1, 0, 0), new Vector3f[]{new Vector3f(wOff, h, dOff + depth), new Vector3f(wOff, h + h1, dOff + depth), new Vector3f(wOff, h + h1, dOff), new Vector3f(wOff, h, dOff)}, uvs[3], sprite);
						putVertexDataSpr(new Vector3f(1, 0, 0), new Vector3f[]{new Vector3f(wOff + width, h, dOff), new Vector3f(wOff + width, h + h1, dOff), new Vector3f(wOff + width, h + h1, dOff + depth), new Vector3f(wOff + width, h, dOff + depth)}, uvs[1], sprite);
						h += h1;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(bakedQuads != null && !bakedQuads.isEmpty()){
			List<BakedQuad> quadList = Collections.synchronizedList(Lists.newArrayList(bakedQuads));
			return quadList;
		}
		return emptyQuads;
	}

	protected final void putVertexDataSpr(Vector3f normal, Vector3f[] vertices, Vector2f[] uvs, TextureAtlasSprite sprite)
	{
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
		builder.setQuadOrientation(Direction.getFacingFromVector(normal.getX(), normal.getY(), normal.getZ()));
		builder.setTexture(sprite);
//		builder.setQuadColored();
		for (int i = 0; i < vertices.length; i++)
		{
			builder.put(0, vertices[i].getX(), vertices[i].getY(), vertices[i].getZ(), 1);//Pos
			float d = LightUtil.diffuseLight(normal.getX(), normal.getY(), normal.getZ());
			builder.put(1, d, d, d, 1);//Colour
			builder.put(2, uvs[i].getX(), uvs[i].getY(), 0, 1);//UV
			builder.put(3, normal.getX(), normal.getY(), normal.getZ(), 0);//Normal
			builder.put(4);//padding
		}
		bakedQuads.add(builder.build());
	}

	static HashMap<String, ModelCoresample> modelCache = new HashMap<>();


	@Override
	public ItemOverrideList getOverrides(){
		return overrideList;
	}
	
	ItemOverrideList overrideList = new ItemOverrideList(){
		@Override
		public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity){
			String resName = ItemNBTHelper.hasKey(stack, "resType") ? ItemNBTHelper.getString(stack, "resType") : null;
			if(ItemNBTHelper.hasKey(stack, "oil") && resName == null && ItemNBTHelper.getInt(stack, "oil") > 0){
				resName = "oil";
			}
			
			if(ItemNBTHelper.hasKey(stack, "mineral")){
				String name = ItemNBTHelper.getString(stack, "mineral");
				String indexName = resName == null ? name : name + "_" + resName;
				if(name != null && !name.isEmpty()){
					if(!modelCache.containsKey(indexName)){
						outer:
						for(MineralMix mix:ExcavatorHandler.mineralList.values()){
							if(name.equals(mix.getPlainName())){
								if(resName != null){
									for(ReservoirType type:PumpjackHandler.reservoirList.keySet()){
										if(resName.equals(type.name)){
											List<StackWithChance> outputs=new ArrayList<>(Arrays.asList(mix.outputs));
											outputs.add(new StackWithChance(new ItemStack(IPContent.dummyBlockOilOre), 0.04F));
											StackWithChance[] outputNew=outputs.toArray(new StackWithChance[0]);
											DimensionType[] copy=mix.dimensions.toArray(new DimensionType[0]);
											MineralMix mix2=new MineralMix(mix.getId(), outputNew, mix.weight, mix.failChance, copy, mix.background);
											modelCache.put(indexName, new ModelCoresampleExtended(mix2, new VertexFormat(), type.getFluid()));
											
//											mix.outputs.add(new ExcavatorHandler.OreOutput(IPContent.dummyBlockOilOre.getRegistryName(), 0.4F));
											
//											MineralMix mix2 = new MineralMix(mix.name, mix.failChance, newOres, newChances);
//											mix2.recalculateChances();
//											mix2.outputs.set(mix2.outputs.size() - 1, new ItemStack(IPContent.blockDummy, 1));
											
//											Fluid fluid = type.getFluid();
//											modelCache.put(indexName, new ModelCoresampleExtended(mix, fluid));
											break outer;
										}
									}
									
//									modelCache.put(indexName, new ModelCoresample(mix));
								}else{
//									modelCache.put(indexName, new ModelCoresample(mix));
								}
							}
						}
					}
					
					IBakedModel model = modelCache.get(indexName);
					if(model != null) return model;
				}
			}
			
			if(resName != null){
				if(!modelCache.containsKey("_" + resName)){
					for(ReservoirType type:PumpjackHandler.reservoirList.keySet()){
						if(resName.equals(type.name)){
//							MineralMix mix = new MineralMix(resName, 1, Arrays.asList(new ExcavatorHandler.OreOutput(IPContent.dummyBlockOilOre.getRegistryName(), 1F)));
//							mix.outputs.set(0, new ItemStack(IPContent.blockDummy, 1, EnumDummyType.OIL_DEPOSIT.getMeta()));
//							Fluid fluid = type.getFluid();
//							modelCache.put("_" + resName, new ModelCoresampleExtended(mix, null, fluid));
						}
					}
				}
				
				//IBakedModel model = modelCache.get("_" + resName);
				//if(model != null) return model;
			}
			
			return originalModel;
		}
	};
}

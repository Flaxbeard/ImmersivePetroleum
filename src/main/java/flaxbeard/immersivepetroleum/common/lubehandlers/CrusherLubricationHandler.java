package flaxbeard.immersivepetroleum.common.lubehandlers;

import java.util.Iterator;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class CrusherLubricationHandler implements ILubricationHandler<CrusherTileEntity>{
	private static Vector3i size = new Vector3i(3, 3, 5);
	
	@Override
	public Vector3i getStructureDimensions(){
		return size;
	}
	
	@Override
	public boolean isMachineEnabled(World world, CrusherTileEntity mbte){
		return mbte.shouldRenderAsActive();
	}
	
	@Override
	public TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity lubricator, Direction facing){
		BlockPos target = lubricator.getPos().offset(facing);
		TileEntity te = world.getTileEntity(target);
		
		if(te instanceof CrusherTileEntity){
			CrusherTileEntity master = ((CrusherTileEntity) te).master();
			
			if(master != null && master.getFacing().getOpposite() == facing){
				return master;
			}
		}
		
		return null;
	}
	
	@Override
	public void lubricate(World world, int ticks, CrusherTileEntity mbte){
		lubricate(world, ticks, mbte, null);
	}
	
	@Override
	public void lubricate(World world, int ticks, CrusherTileEntity mbte, FluidStack lubrication){
		if(!world.isRemote){
			Iterator<MultiblockProcess<CrusherRecipe>> processIterator = mbte.processQueue.iterator();
			MultiblockProcess<CrusherRecipe> process = processIterator.next();
			
			if(ticks % 4 == 0){
				int consume = mbte.energyStorage.extractEnergy(process.energyPerTick, true);
				if(consume >= process.energyPerTick){
					mbte.energyStorage.extractEnergy(process.energyPerTick, false);
					
					if(process.processTick < process.maxTicks)
						process.processTick++;
					
					if(process.processTick >= process.maxTicks && mbte.processQueue.size() > 1){
						process = processIterator.next();
						
						if(process.processTick < process.maxTicks)
							process.processTick++;
					}
				}
			}
		}else{
			mbte.animation_barrelRotation += 18f / 4f;
			mbte.animation_barrelRotation %= 360f;
		}
	}
	
	@Override
	public void spawnLubricantParticles(World world, AutoLubricatorTileEntity lubricator, Direction facing, CrusherTileEntity mbte){
		Direction f = mbte.getIsMirrored() ? facing : facing.getOpposite();
		
		float location = world.rand.nextFloat();
		
		boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getIsMirrored();
		float xO = 2.5F;
		float zO = -0.1F;
		float yO = 1.3F;
		
		if(location > .5F){
			xO = 1.0F;
			yO = 3.0F;
			zO = 1.65F;
		}
		
		if(facing.getAxisDirection() == AxisDirection.NEGATIVE)
			xO = -xO + 1;
		if(!flip)
			zO = -zO + 1;
		
		float x = lubricator.getPos().getX() + (f.getAxis() == Axis.X ? xO : zO);
		float y = lubricator.getPos().getY() + yO;
		float z = lubricator.getPos().getZ() + (f.getAxis() == Axis.X ? zO : xO);
		
		for(int i = 0;i < 3;i++){
			float r1 = (world.rand.nextFloat() - .5F) * 2F;
			float r2 = (world.rand.nextFloat() - .5F) * 2F;
			float r3 = world.rand.nextFloat();
			BlockState n = Fluids.lubricant.block.getDefaultState();
			world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
		}
	}
	
	@Override
	public Tuple<BlockPos, Direction> getGhostBlockPosition(World world, CrusherTileEntity mbte){
		if(!mbte.isDummy()){
			BlockPos pos = mbte.getPos().offset(mbte.getFacing(), 2);
			Direction f = mbte.getFacing().getOpposite();
			return new Tuple<BlockPos, Direction>(pos, f);
		}
		return null;
	}
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(ImmersivePetroleum.MODID, "textures/models/lube_pipe.png");
	private static Supplier<IPModel> pipes;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(AutoLubricatorTileEntity lubricator, CrusherTileEntity mbte, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
		matrix.translate(0, -1, 0);
		Vector3i offset = mbte.getPos().subtract(lubricator.getPos());
		matrix.translate(offset.getX(), offset.getY(), offset.getZ());
		
		Direction rotation = mbte.getFacing();
		switch(rotation){
			case NORTH:{
				matrix.rotate(new Quaternion(0, 90F, 0, true));
				matrix.translate(-1, 0, 0);
				break;
			}
			case SOUTH:{
				matrix.rotate(new Quaternion(0, 270F, 0, true));
				matrix.translate(0, 0, -1);
				break;
			}
			case EAST:{
				matrix.translate(0, 0, 0);
				break;
			}
			case WEST:{
				matrix.rotate(new Quaternion(0, 180F, 0, true));
				matrix.translate(-1, 0, -1);
				break;
			}
			default:
				break;
		}
		
		if(pipes == null)
			pipes = IPModels.getSupplier(ModelLubricantPipes.Crusher.ID);
		
		IPModel model;
		if((model = pipes.get()) != null){
			model.render(matrix, buffer.getBuffer(model.getRenderType(TEXTURE)), combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}

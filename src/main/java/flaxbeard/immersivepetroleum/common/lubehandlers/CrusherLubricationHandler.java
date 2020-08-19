package flaxbeard.immersivepetroleum.common.lubehandlers;

import java.util.Iterator;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorNewTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrusherLubricationHandler implements ILubricationHandler<CrusherTileEntity>{
	
	@Override
	public TileEntity isPlacedCorrectly(World world, TileEntity tile, Direction facing){
		
		BlockPos target = tile.getPos().offset(facing, 2).up();
		TileEntity te = world.getTileEntity(target);
		
		if(te instanceof CrusherTileEntity){
			CrusherTileEntity master = ((CrusherTileEntity) te).master();
			
			Direction f = facing;
			if(master.getFacing().getOpposite() == f){
				
				return master;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isMachineEnabled(World world, CrusherTileEntity mbte){
		return mbte.shouldRenderAsActive();
	}
	
	@Override
	public void lubricate(World world, int ticks, CrusherTileEntity mbte){
		Iterator<MultiblockProcess<CrusherRecipe>> processIterator = mbte.processQueue.iterator();
		MultiblockProcess<CrusherRecipe> process = processIterator.next();
		
		if(!world.isRemote){
			if(ticks % 4 == 0){
				int consume = mbte.energyStorage.extractEnergy(process.energyPerTick, true);
				if(consume >= process.energyPerTick){
					mbte.energyStorage.extractEnergy(process.energyPerTick, false);
					if(process.processTick < process.maxTicks) process.processTick++;
					if(process.processTick >= process.maxTicks && mbte.processQueue.size() > 1){
						process = processIterator.next();
						if(process.processTick < process.maxTicks) process.processTick++;
					}
				}
			}
		}else{
			mbte.animation_barrelRotation += 18f / 4f;
			mbte.animation_barrelRotation %= 360f;
		}
	}
	
	@Override
	public void spawnLubricantParticles(World world, TileEntity tile, Direction facing, CrusherTileEntity mbte){
		
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
		
		if(facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
		if(!flip) zO = -zO + 1;
		
		float x = tile.getPos().getX() + (f.getAxis() == Axis.X ? xO : zO);
		float y = tile.getPos().getY() + yO;
		float z = tile.getPos().getZ() + (f.getAxis() == Axis.X ? zO : xO);
		
		for(int i = 0;i < 3;i++){
			float r1 = (world.rand.nextFloat() - .5F) * 2F;
			float r2 = (world.rand.nextFloat() - .5F) * 2F;
			float r3 = world.rand.nextFloat();
			BlockState n = Fluids.fluidLubricant.block.getDefaultState();
			world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
		}
	}
	
	private static Object excavator;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(World world, AutoLubricatorNewTileEntity tile, Direction facing, CrusherTileEntity mbte){
		if(excavator == null){
			excavator = new ModelLubricantPipes.Crusher(false);
		}
		
		GlStateManager.translatef(0, -1, 0);
		Vec3i offset = mbte.getPos().subtract(tile.getPos());
		GlStateManager.translatef(offset.getX(), offset.getY(), offset.getZ());
		
		Direction rotation = mbte.getFacing();
		if(rotation == Direction.NORTH){
			GlStateManager.rotatef(90F, 0, 1, 0);
			GlStateManager.translatef(-1, 0, 0);
		}else if(rotation == Direction.WEST){
			GlStateManager.rotatef(180F, 0, 1, 0);
			GlStateManager.translatef(-1, 0, -1);
			
		}else if(rotation == Direction.SOUTH){
			GlStateManager.rotatef(270F, 0, 1, 0);
			GlStateManager.translatef(0, 0, -1);
			
		}
		
		ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
		((ModelLubricantPipes.Crusher) excavator).render(0.0625F);
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
	
	private static Vec3i size;
	@Override
	public Vec3i getStructureDimensions(){
		if(size==null)
			size=new Vec3i(3,3,5);
		return size;
	}
}
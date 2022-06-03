package flaxbeard.immersivepetroleum.common.lubehandlers;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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

public class PumpjackLubricationHandler implements ILubricationHandler<PumpjackTileEntity>{
	private static Vector3i size = new Vector3i(4, 6, 3);
	
	@Override
	public Vector3i getStructureDimensions(){
		return size;
	}
	
	@Override
	public boolean isMachineEnabled(World world, PumpjackTileEntity mbte){
		return mbte.wasActive;
	}
	
	@Override
	public TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity lubricator, Direction facing){
		BlockPos target = lubricator.getPos().offset(facing);
		TileEntity te = world.getTileEntity(target);
		
		if(te instanceof PumpjackTileEntity){
			PumpjackTileEntity master = ((PumpjackTileEntity) te).master();
			if(master != null){
				Direction f = master.getIsMirrored() ? facing : facing.getOpposite();
				if(master.getFacing().rotateY() == f){
					return master;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void lubricate(World world, int ticks, PumpjackTileEntity mbte) {
		lubricate(world, ticks, mbte, null);
	}
	
	@Override
	public void lubricate(World world, int ticks, PumpjackTileEntity mbte, FluidStack lubrication){
		if(!world.isRemote){
			if(ticks % 4 == 0){
				mbte.tick();
			}
		}else{
			mbte.activeTicks += 1F / 4F;
		}
	}
	
	@Override
	public void spawnLubricantParticles(World world, AutoLubricatorTileEntity lubricator, Direction facing, PumpjackTileEntity mbte){
		Direction f = mbte.getIsMirrored() ? facing : facing.getOpposite();
		float location = world.rand.nextFloat();
		
		boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getIsMirrored();
		float xO = 2.5F;
		float zO = -.15F;
		float yO = 2.25F;
		
		if(location > .5F){
			xO = 1.7F;
			yO = 2.9F;
			zO = -1.5F;
			
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
			//BlockState n = Fluids.lubricant.block.getDefaultState();
			//world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
			
			// Because making your own particles is so convoluted and confusing
			world.addParticle(ParticleTypes.DRIPPING_HONEY, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
		}
	}
	
	@Override
	public Tuple<BlockPos, Direction> getGhostBlockPosition(World world, PumpjackTileEntity mbte){
		if(!mbte.isDummy()){
			Direction mbFacing = mbte.getFacing().getOpposite();
			BlockPos pos = mbte.getPos()
					.offset(Direction.UP)
					.offset(mbFacing, 4)
					.offset(mbte.getIsMirrored() ? mbFacing.rotateY() : mbFacing.rotateYCCW(), 2);
			
			Direction f = (mbte.getIsMirrored() ? mbte.getFacing().getOpposite() : mbte.getFacing()).rotateYCCW();
			return new Tuple<BlockPos, Direction>(pos, f);
		}
		return null;
	}
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(ImmersivePetroleum.MODID, "textures/models/lube_pipe.png");
	
	@OnlyIn(Dist.CLIENT)
	private static Supplier<IPModel> pipes_normal;
	
	@OnlyIn(Dist.CLIENT)
	private static Supplier<IPModel> pipes_mirrored;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(AutoLubricatorTileEntity lubricator, PumpjackTileEntity mbte, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
		matrix.translate(0, -1, 0);
		Vector3i offset = mbte.getPos().subtract(lubricator.getPos());
		matrix.translate(offset.getX(), offset.getY(), offset.getZ());
		
		Direction rotation = mbte.getFacing();
		switch(rotation){
			case NORTH:{
				matrix.rotate(new Quaternion(0, 90F, 0, true));
				matrix.translate(-6, 1, -1);
				break;
			}
			case SOUTH:{
				matrix.rotate(new Quaternion(0, 270F, 0, true));
				matrix.translate(-5, 1, -2);
				break;
			}
			case EAST:{
				matrix.translate(-5, 1, -1);
				break;
			}
			case WEST:{
				matrix.rotate(new Quaternion(0, 180F, 0, true));
				matrix.translate(-6, 1, -2);
				break;
			}
			default:
				break;
		}
		
		IPModel model;
		if(mbte.getIsMirrored()){
			if(pipes_mirrored == null)
				pipes_mirrored = IPModels.getSupplier(ModelLubricantPipes.Pumpjack.ID_MIRRORED);
			
			model = pipes_mirrored.get();
		}else{
			if(pipes_normal == null)
				pipes_normal = IPModels.getSupplier(ModelLubricantPipes.Pumpjack.ID_NORMAL);
			
			model = pipes_normal.get();
		}
		
		if(model != null){
			model.render(matrix, buffer.getBuffer(model.getRenderType(TEXTURE)), combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}

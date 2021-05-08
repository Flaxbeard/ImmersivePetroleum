package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class CokerUnitBlock extends IPMetalMultiblock<CokerUnitTileEntity>{
	public CokerUnitBlock(){
		super("cokerunit", () -> IPTileTypes.COKER.get());
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		if(!player.getHeldItem(hand).isEmpty()){
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof CokerUnitTileEntity){
				BlockPos tPos = ((CokerUnitTileEntity)te).posInMultiblock;
				Direction facing = ((CokerUnitTileEntity)te).getFacing();
				
				// Locations that don't require sneaking to avoid the GUI
				
				// Conveyor locations
				if(tPos.getY() == 0 && tPos.getZ() == 2 && hit.getFace() == Direction.UP){
					return ActionResultType.FAIL;
				}
				
				// All power input sockets
				if(CokerUnitTileEntity.Energy_IN.contains(tPos) && hit.getFace() == facing){
					return ActionResultType.FAIL;
				}
				
				// Redstone controller input
				if(CokerUnitTileEntity.Redstone_IN.contains(tPos) && hit.getFace() == facing.getOpposite()){
					return ActionResultType.FAIL;
				}
				
				// Fluid I/O Ports
				if(tPos.equals(CokerUnitTileEntity.Fluid_IN) || tPos.equals(CokerUnitTileEntity.Fluid_OUT)){
					return ActionResultType.FAIL;
				}
				
				// Item input port
				if(tPos.equals(CokerUnitTileEntity.Item_IN)){
					return ActionResultType.FAIL;
				}
			}
		}
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}
	
	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity){
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof CokerUnitTileEntity){
			return ((CokerUnitTileEntity) te).isLadder();
		}
		return false;
	}
}

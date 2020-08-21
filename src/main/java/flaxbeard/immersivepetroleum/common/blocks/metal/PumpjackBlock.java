package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class PumpjackBlock extends IPMetalMultiblock{
	public PumpjackBlock(){
		super("pumpjack", ()->PumpjackTileEntity.TYPE);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		TileEntity te=world.getTileEntity(pos);
		if(te instanceof PumpjackTileEntity){
			//BlockPos tPos=((PumpjackTileEntity)te).posInMultiblock;
		}
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}
}

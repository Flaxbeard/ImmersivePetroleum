package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class PumpjackBlock extends IPMetalMultiblock<PumpjackTileEntity>{
	public PumpjackBlock(){
		super("pumpjack", () -> PumpjackTileEntity.TYPE);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof PumpjackTileEntity){
			// BlockPos tPos=((PumpjackTileEntity)te).posInMultiblock;
		}
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}
}

package flaxbeard.immersivepetroleum.client.page;

import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.gui.GuiManual;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;

public class ManualPageBigMultiblock extends ManualPageMultiblock
{

	public ManualPageBigMultiblock(ManualInstance manual, IMultiblock multiblock)
	{
		super(manual, "blank_text", multiblock);
	}

	@Override
	public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		super.initPage(gui, x, y, pageButtons);
		float yOff = ReflectionHelper.getPrivateValue(ManualPageMultiblock.class, this, 6);
		ReflectionHelper.setPrivateValue(ManualPageMultiblock.class, this, yOff + 30, 6);
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{
		if ((clickX >= 40 && clickX < 144 && mx >= 20 && mx < 164) && (clickY >= 30 && clickY < 175 && my >= 30 && my < 225))
		{
			if (clickY >= 130 || my >= 180)
			{
				clickY -= 50;
				lastY -= 50;
				my -= 50;
			}


			super.mouseDragged(x, y, clickX, clickY, mx, my, lastX, lastY, button);
		}
	}

}

package pokefenn.totemic.world;

import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import pokefenn.totemic.ModVillagers;

public class ComponentTotemistPlot extends StructureVillagePieces.Village
{
    public ComponentTotemistPlot()
    {
    }

    public ComponentTotemistPlot(Start start, int type, Random rand, StructureBoundingBox bb, EnumFacing facing)
    {
        super(start, type);
        this.setCoordBaseMode(facing);
        this.boundingBox = bb;
    }

    public static ComponentTotemistPlot createPiece(StructureVillagePieces.Start startPiece, List<StructureComponent> pieces,
            Random random, int strucMinX, int strucMinY, int strucMinZ, EnumFacing facing, int type)
    {
        StructureBoundingBox bb = StructureBoundingBox.getComponentToAddBoundingBox(strucMinX, strucMinY, strucMinZ, 0, 0, 0, 9, 7, 12, facing);
        if(canVillageGoDeeper(bb) && StructureComponent.findIntersecting(pieces, bb) == null)
            return new ComponentTotemistPlot(startPiece, type, random, bb, facing);
        else
            return null;
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox bb)
    {
        if(averageGroundLvl < 0)
        {
            averageGroundLvl = getAverageGroundLevel(world, bb);
            if(averageGroundLvl < 0)
                return true;
            boundingBox.offset(0, averageGroundLvl - boundingBox.maxY + 7 - 1, 0);
        }

        IBlockState block = getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        setBlockState(world, block, 0, 0, 0, bb);
        setBlockState(world, block, 9, 0, 0, bb);
        setBlockState(world, block, 0, 7, 0, bb);
        setBlockState(world, block, 9, 7, 0, bb);
        setBlockState(world, block, 0, 0, 12, bb);
        setBlockState(world, block, 9, 0, 12, bb);
        setBlockState(world, block, 0, 7, 12, bb);
        setBlockState(world, block, 9, 7, 12, bb);

        spawnVillagers(world, bb, 5, 1, 6, 2);
        return true;
    }

    @Override
    protected VillagerProfession chooseForgeProfession(int count, VillagerProfession prof)
    {
        return ModVillagers.profTotemist;
    }

    public static class CreationHandler implements IVillageCreationHandler
    {
        @Override
        public PieceWeight getVillagePieceWeight(Random random, int size)
        {
            return new PieceWeight(ComponentTotemistPlot.class, 3, MathHelper.getInt(random, 2 + size, 5 + size*3));
        }

        @Override
        public Class<?> getComponentClass()
        {
            return ComponentTotemistPlot.class;
        }

        @Override
        public Village buildComponent(PieceWeight villagePiece, Start startPiece, List<StructureComponent> pieces,
                Random random, int strucMinX, int strucMinY, int strucMinZ, EnumFacing facing, int type)
        {
            return ComponentTotemistPlot.createPiece(startPiece, pieces, random, strucMinX, strucMinY, strucMinZ, facing, type);
        }
    }
}

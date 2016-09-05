package com.cout970.magneticraft.tilerenderer

import com.cout970.loader.api.ModelCacheFactory
import com.cout970.loader.api.model.ICachedModel
import com.cout970.loader.api.model.IModelFilter
import com.cout970.loader.api.model.IModelPart
import com.cout970.loader.api.model.IObjGroup
import com.cout970.magneticraft.tileentity.TileInserter
import com.cout970.magneticraft.util.resource
import com.google.common.base.Predicates
import net.minecraft.client.renderer.GlStateManager.*

/**
 * Created by cout970 on 25/08/2016.
 */
object TileInserterRenderer : TileEntityRenderer<TileInserter>() {

    lateinit var block: ICachedModel

    override fun renderTileEntityAt(te: TileInserter, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {

        pushMatrix()
        translate(x, y, z)
        block.render()
        popMatrix()
    }

    override fun onModelRegistryReload() {
        super.onModelRegistryReload()
        try {
            val model = TileIncendiaryGeneratorRenderer.getModelObj(resource("models/block/obj/inserter.obj"))
            val hasFan = object : IModelFilter {
                override fun apply(it: IModelPart?): Boolean = if (it is IObjGroup) it.getName().contains("fan") else false
            }
            block = ModelCacheFactory.createCachedModel(model.filter(Predicates.not(hasFan)), 1)
//            harm = ModelCacheFactory.createCachedModel(model.filter(hasFan), 1)
        }catch (e: Exception){}
    }
}